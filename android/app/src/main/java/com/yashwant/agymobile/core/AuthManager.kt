package com.yashwant.agymobile.core

import com.yashwant.agymobile.model.AuthState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.util.regex.Pattern

class AuthManager(
    private val bootstrapManager: BootstrapManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _authState = MutableStateFlow(checkAuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var activeAuthProcess: Process? = null

    companion object {
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
        val state = AuthState(
            isAuthenticated = loggedIn,
            userEmail = account,
            isAuthenticating = activeAuthProcess != null,
            authUrl = _authState?.value?.authUrl
        )
        return state
    }

    fun refreshState() {
        _authState.value = checkAuthState()
    }

    fun startAuthSession(
        agyExecutable: File,
        envVars: Map<String, String>
    ): Job {
        cancelAuthSession()

        _authState.value = _authState.value.copy(
            isAuthenticating = true,
            authUrl = null,
            error = null
        )

        return scope.launch(dispatcher) {
            val args = listOf(
                agyExecutable.absolutePath,
                "-p", "ping",
                "--output-format", "stream-json"
            )

            val pb = ProcessBuilder(args)
            pb.environment().putAll(envVars)

            try {
                val proc = pb.start()
                activeAuthProcess = proc

                val errorReader = proc.errorStream.bufferedReader()
                val outReader = proc.inputStream.bufferedReader()

                // Concurrently drain stdout
                launch {
                    try {
                        while (isActive && outReader.readLine() != null) {
                            // Drain output
                        }
                    } catch (_: Exception) {}
                }

                // Read stderr looking for OAuth URL
                var line: String?
                while (isActive) {
                    line = errorReader.readLine() ?: break
                    val url = extractOAuthUrl(line)
                    if (url != null) {
                        _authState.value = _authState.value.copy(
                            isAuthenticating = true,
                            authUrl = url
                        )
                    }
                }

                val exitCode = proc.waitFor()
                if (exitCode == 0 || bootstrapManager.isLoggedIn()) {
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        userEmail = bootstrapManager.getAuthAccount(),
                        isAuthenticating = false,
                        authUrl = null,
                        error = null
                    )
                } else if (_authState.value.isAuthenticating) {
                    _authState.value = _authState.value.copy(
                        isAuthenticating = false,
                        error = "Authentication process exited with code $exitCode"
                    )
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _authState.value = _authState.value.copy(
                        isAuthenticating = false,
                        error = e.message ?: "Authentication error"
                    )
                }
            } finally {
                activeAuthProcess = null
            }
        }
    }

    suspend fun submitAuthCode(code: String): Boolean = withContext(dispatcher) {
        val proc = activeAuthProcess ?: return@withContext false
        val trimmed = code.trim()
        if (trimmed.isEmpty()) return@withContext false

        try {
            val writer = proc.outputStream.bufferedWriter()
            writer.write(trimmed)
            writer.newLine()
            writer.flush()

            // Wait briefly for credentials to be written
            for (i in 0 until 50) {
                delay(100)
                if (bootstrapManager.isLoggedIn()) {
                    _authState.value = AuthState(
                        isAuthenticated = true,
                        userEmail = bootstrapManager.getAuthAccount(),
                        isAuthenticating = false,
                        authUrl = null,
                        error = null
                    )
                    return@withContext true
                }
                if (!proc.isAlive) break
            }

            val success = bootstrapManager.isLoggedIn()
            if (success) {
                _authState.value = AuthState(
                    isAuthenticated = true,
                    userEmail = bootstrapManager.getAuthAccount(),
                    isAuthenticating = false,
                    authUrl = null,
                    error = null
                )
            } else {
                _authState.value = _authState.value.copy(
                    isAuthenticating = false,
                    error = "Failed to authenticate with authorization code"
                )
            }
            return@withContext success
        } catch (e: Exception) {
            _authState.value = _authState.value.copy(
                isAuthenticating = false,
                error = e.message ?: "Failed to write authorization code"
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
