package com.yashwant.agymobile.core

import com.yashwant.agymobile.model.AuthState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.regex.Pattern

class AuthManager(
    private val bootstrapManager: BootstrapManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _authState = MutableStateFlow(checkAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var activeCodeVerifier: String? = null
    private var activeState: String? = null
    private var activeAuthProcess: Process? = null

    companion object {
        const val CLIENT_ID = "1071006060591-tmhssin2h21lcre235vtolojh4g403ep.apps.googleusercontent.com"
        const val CLIENT_SECRET = "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"
        const val REDIRECT_URI = "https://antigravity.google/oauth-callback"
        const val OAUTH_SCOPES = "https://www.googleapis.com/auth/cloud-platform https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/cclog https://www.googleapis.com/auth/experimentsandconfigs https://www.googleapis.com/auth/aicode openid"

        private val OAUTH_URL_PATTERN = Pattern.compile("https://accounts\\.google\\.com/o/oauth2/auth[^\\s\"'>]+")

        fun extractOAuthUrl(text: String): String? {
            val matcher = OAUTH_URL_PATTERN.matcher(text)
            return if (matcher.find()) {
                matcher.group()
            } else null
        }
    }

    fun checkAuthState(): AuthState {
        val loggedIn = bootstrapManager.isLoggedIn()
        val account = if (loggedIn) bootstrapManager.getAuthAccount() else null
        return AuthState(
            isAuthenticated = loggedIn,
            userEmail = account,
            isAuthenticating = activeCodeVerifier != null || activeAuthProcess != null,
            authUrl = _authState?.value?.authUrl
        )
    }

    fun refreshState() {
        _authState.value = checkAuthState()
    }

    fun setExternalAuthUrl(url: String) {
        _authState.value = _authState.value.copy(
            isAuthenticating = true,
            authUrl = url,
            error = null
        )
    }

    /**
     * Generates an official Google OAuth 2.0 PKCE authorization URL.
     * Generates a cryptographically secure code_verifier, code_challenge (S256), and state,
     * allowing in-phone authentication via Chrome Custom Tabs without depending on CLI TTY prompts.
     */
    fun generateAuthUrl(): String {
        val verifierBytes = ByteArray(32)
        SecureRandom().nextBytes(verifierBytes)
        val verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes)
        activeCodeVerifier = verifier

        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(verifier.toByteArray(Charsets.US_ASCII))
        val challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(digest)

        val stateBytes = ByteArray(16)
        SecureRandom().nextBytes(stateBytes)
        val state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes)
        activeState = state

        val encodedScope = URLEncoder.encode(OAUTH_SCOPES, "UTF-8")
        val encodedRedirect = URLEncoder.encode(REDIRECT_URI, "UTF-8")

        val url = "https://accounts.google.com/o/oauth2/auth?" +
                "client_id=$CLIENT_ID" +
                "&redirect_uri=$encodedRedirect" +
                "&response_type=code" +
                "&scope=$encodedScope" +
                "&access_type=offline" +
                "&code_challenge_method=S256" +
                "&code_challenge=$challenge" +
                "&state=$state" +
                "&prompt=consent"

        _authState.value = _authState.value.copy(
            isAuthenticating = true,
            authUrl = url,
            error = null
        )
        return url
    }

    fun startAuthSession(
        agyExecutable: File? = null,
        envVars: Map<String, String>? = null
    ): Job {
        cancelAuthSession()
        return scope.launch(dispatcher) {
            generateAuthUrl()
        }
    }

    /**
     * Exchanges an authorization code for OAuth tokens using Google's token endpoint.
     * Handles both PKCE code_verifier and client credentials, writing output to ~/.gemini/oauth_creds.json.
     */
    suspend fun submitAuthCode(
        code: String,
        agyExecutable: File? = null,
        envVars: Map<String, String>? = null
    ): Boolean = withContext(dispatcher) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return@withContext false

        // Extract code if user pasted a full callback URL (https://antigravity.google/oauth-callback?code=...)
        val codeParam = Regex("[?&]code=([^&]+)").find(trimmed)?.groupValues?.get(1)
        val cleanCode = codeParam?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8")
            } catch (_: Exception) {
                it
            }
        } ?: trimmed

        _authState.value = _authState.value.copy(isAuthenticating = true, error = null)

        try {
            val verifier = activeCodeVerifier
            val postParams = mutableMapOf(
                "code" to cleanCode,
                "client_id" to CLIENT_ID,
                "client_secret" to CLIENT_SECRET,
                "redirect_uri" to REDIRECT_URI,
                "grant_type" to "authorization_code"
            )
            if (!verifier.isNullOrBlank()) {
                postParams["code_verifier"] = verifier
            }

            val url = URL("https://oauth2.googleapis.com/token")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val body = postParams.map { (k, v) ->
                URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
            }.joinToString("&")

            conn.outputStream.use { os ->
                os.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseCode = conn.responseCode
            if (responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)

                var refreshToken = json.optString("refresh_token")
                if (refreshToken.isBlank()) {
                    val oldAdc = bootstrapManager.getAdcFile()
                    if (oldAdc.exists()) {
                        try {
                            refreshToken = JSONObject(oldAdc.readText()).optString("refresh_token")
                        } catch (_: Exception) {}
                    }
                }

                val credsJson = JSONObject().apply {
                    put("access_token", json.optString("access_token"))
                    if (refreshToken.isNotBlank()) {
                        put("refresh_token", refreshToken)
                    }
                    put("scope", json.optString("scope", OAUTH_SCOPES))
                    put("token_type", json.optString("token_type", "Bearer"))
                    put("id_token", json.optString("id_token"))
                    val expiresIn = json.optLong("expires_in", 3600)
                    put("expiry_date", System.currentTimeMillis() + (expiresIn * 1000))
                }

                val saved = bootstrapManager.writeCredentials(credsJson.toString(2))
                if (saved && bootstrapManager.isLoggedIn()) {
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        userEmail = bootstrapManager.getAuthAccount(),
                        isAuthenticating = false,
                        authUrl = null,
                        error = null
                    )
                    activeCodeVerifier = null
                    activeState = null
                    return@withContext true
                } else {
                    _authState.value = _authState.value.copy(
                        isAuthenticating = false,
                        error = "Failed to save OAuth credentials"
                    )
                    return@withContext false
                }
            } else {
                val errorStream = conn.errorStream?.bufferedReader()?.readText()
                val errorMsg = if (!errorStream.isNullOrBlank()) {
                    try {
                        val errObj = JSONObject(errorStream)
                        errObj.optString("error_description", errObj.optString("error", errorStream))
                    } catch (_: Exception) {
                        errorStream
                    }
                } else {
                    "HTTP $responseCode from Google OAuth"
                }

                _authState.value = _authState.value.copy(
                    isAuthenticating = false,
                    error = "Sign-In Error: $errorMsg"
                )
                return@withContext false
            }
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isAuthenticating = false,
                error = e.message ?: "Network error during authentication"
            )
            return@withContext false
        }
    }

    fun importCredentials(jsonContent: String): Boolean {
        val trimmed = jsonContent.trim()
        if (trimmed.isEmpty()) return false

        val success = bootstrapManager.writeCredentials(trimmed)
        if (success && bootstrapManager.isLoggedIn()) {
            _authState.value = AuthState(
                isAuthenticated = true,
                userEmail = bootstrapManager.getAuthAccount(),
                isAuthenticating = false,
                authUrl = null,
                error = null
            )
            return true
        } else {
            _authState.value = _authState.value.copy(
                error = "Invalid or unparseable credentials JSON"
            )
            return false
        }
    }

    fun saveApiKey(apiKey: String): Boolean {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) return false
        val success = bootstrapManager.saveApiKey(trimmed)
        if (success) {
            _authState.value = AuthState(
                isAuthenticated = true,
                userEmail = "Gemini API Key",
                isAuthenticating = false,
                authUrl = null,
                error = null
            )
            return true
        } else {
            _authState.value = _authState.value.copy(
                error = "Failed to save Gemini API key"
            )
            return false
        }
    }

    fun signOut(): Boolean {
        cancelAuthSession()
        val cleared = bootstrapManager.clearAuth()
        _authState.value = AuthState(
            isAuthenticated = false,
            userEmail = null,
            isAuthenticating = false,
            authUrl = null,
            error = null
        )
        return cleared
    }

    fun cancelAuthSession() {
        activeCodeVerifier = null
        activeState = null
        activeAuthProcess?.let {
            try {
                it.destroyForcibly()
            } catch (_: Exception) {}
        }
        activeAuthProcess = null
        if (_authState.value.isAuthenticating) {
            _authState.value = _authState.value.copy(
                isAuthenticating = false,
                authUrl = null
            )
        }
    }
}
