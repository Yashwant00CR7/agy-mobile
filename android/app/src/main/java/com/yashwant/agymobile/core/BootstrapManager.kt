package com.yashwant.agymobile.core

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class BootstrapManager(
    val baseDir: File,
    private val nativeLibDir: File? = null
) {
    val binDir: File = File(baseDir, "bin")
    val homeDir: File = File(baseDir, "home")
    val tmpDir: File = File(baseDir, "tmp")
    val defaultWorkspaceDir: File = File(baseDir, "workspaces")

    fun initializeDirectories(): Boolean {
        val created = binDir.mkdirs() or
                homeDir.mkdirs() or
                tmpDir.mkdirs() or
                defaultWorkspaceDir.mkdirs() or
                geminiDir.mkdirs() or
                agyCliDir.mkdirs()

        // Ensure default settings.json exists with standard plan model
        try {
            for (file in listOf(getSettingsFile(), getAgyCliSettingsFile())) {
                val json = if (file.exists()) {
                    try { org.json.JSONObject(file.readText()) } catch (_: Exception) { org.json.JSONObject() }
                } else org.json.JSONObject()

                if (!json.has("model")) {
                    json.put("model", "gemini-3.8-flash-high")
                }
                if (!json.has("agentMode")) {
                    json.put("agentMode", "accept-edits")
                }
                val key = getApiKey()
                if (key != null && !json.has("modelProvider")) {
                    json.put("modelProvider", "gemini")
                }
                file.parentFile?.mkdirs()
                file.writeText(json.toString(2))
            }
        } catch (_: Exception) {}
        return created
    }

    fun installBinary(name: String, inputStream: InputStream): File {
        initializeDirectories()
        val targetFile = File(binDir, name)
        FileOutputStream(targetFile).use { output ->
            inputStream.copyTo(output)
        }
        targetFile.setExecutable(true, false)
        targetFile.setReadable(true, false)
        return targetFile
    }

    fun setupAgyFromNativeLib(): File? {
        initializeDirectories()

        // 1. Clean up any stale copy in binDir that would fail with error=13 on Android 10+
        val staleInBin = File(binDir, "agy")
        if (staleInBin.exists()) {
            try {
                staleInBin.delete()
            } catch (_: Exception) {}
        }

        // 2. Return libagy.so directly from nativeLibraryDir.
        // On Android 10+ (API 29+), executing from nativeLibraryDir is permitted by SELinux (apk_data_file),
        // whereas executing copied binaries from app filesDir is strictly blocked by W^X restrictions (error=13).
        if (nativeLibDir != null) {
            val libAgy = File(nativeLibDir, "libagy.so")
            if (libAgy.exists() && libAgy.canExecute()) {
                return libAgy
            }
        }

        return null
    }

    fun getAgyExecutable(): File? {
        // ALWAYS prefer nativeLibDir because Android 10+ (API 29+) SELinux W^X
        // strictly blocks executing binaries from filesDir (error=13 Permission denied).
        // Only binaries inside nativeLibraryDir (apk_data_file context) can be executed.
        if (nativeLibDir != null) {
            val inLib = File(nativeLibDir, "libagy.so")
            if (inLib.exists() && inLib.canExecute()) return inLib
        }

        val inBin = File(binDir, "agy")
        if (inBin.exists() && inBin.canExecute()) return inBin

        return null
    }

    fun isAgyReady(): Boolean {
        return getAgyExecutable() != null
    }

    fun getEnvironmentVariables(): Map<String, String> {
        val pathBuilder = StringBuilder()
        if (nativeLibDir != null) {
            pathBuilder.append(nativeLibDir.absolutePath).append(":")
        }
        pathBuilder.append(binDir.absolutePath)
        pathBuilder.append(":/system/bin:/system/xbin")

        val currentPath = System.getenv("PATH")
        if (!currentPath.isNullOrBlank()) {
            pathBuilder.append(":").append(currentPath)
        }

        val env = mutableMapOf(
            "PATH" to pathBuilder.toString(),
            "HOME" to homeDir.absolutePath,
            "TMPDIR" to tmpDir.absolutePath,
            "TERM" to "xterm-256color",
            "PAGER" to "cat",
            "COLORTERM" to "truecolor"
        )

        if (nativeLibDir != null) {
            env["LD_LIBRARY_PATH"] = nativeLibDir.absolutePath
        }

        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank()) {
            env["GEMINI_API_KEY"] = apiKey
        } else {
            val adcFile = getAdcFile()
            if (adcFile.exists() && adcFile.length() > 0) {
                env["GOOGLE_APPLICATION_CREDENTIALS"] = adcFile.absolutePath
            }
        }

        return env
    }

    val geminiDir: File = File(homeDir, ".gemini")
    val agyCliDir: File = File(geminiDir, "antigravity-cli")
    val gcloudDir: File = File(File(homeDir, ".config"), "gcloud")

    fun getOauthCredsFile(): File = File(geminiDir, "oauth_creds.json")
    fun getGoogleAccountsFile(): File = File(geminiDir, "google_accounts.json")
    fun getSettingsFile(): File = File(geminiDir, "settings.json")
    fun getAgyCliSettingsFile(): File = File(agyCliDir, "settings.json")
    fun getApiKeyFile(): File = File(geminiDir, "api_key.txt")
    fun getAdcFile(): File = File(geminiDir, "application_default_credentials.json")
    fun getGcloudAdcFile(): File = File(gcloudDir, "application_default_credentials.json")

    fun getSelectedModel(): String {
        val cliSettings = getAgyCliSettingsFile()
        if (cliSettings.exists()) {
            try {
                val json = org.json.JSONObject(cliSettings.readText())
                val m = json.optString("model", "")
                if (m.isNotBlank()) return toCanonicalModelSlug(m)
            } catch (_: Exception) {}
        }
        return "gemini-3.8-flash-high"
    }

    fun getSelectedModelDisplayName(): String {
        return toDisplayName(getSelectedModel())
    }

    fun saveSelectedModel(modelDisplayNameOrId: String): Boolean {
        val slug = toCanonicalModelSlug(modelDisplayNameOrId)
        return try {
            geminiDir.mkdirs()
            agyCliDir.mkdirs()
            for (file in listOf(getSettingsFile(), getAgyCliSettingsFile())) {
                val json = if (file.exists()) {
                    try { org.json.JSONObject(file.readText()) } catch (_: Exception) { org.json.JSONObject() }
                } else org.json.JSONObject()

                json.put("model", slug)
                json.put("agentMode", "accept-edits")
                if (getApiKey() != null) {
                    json.put("modelProvider", "gemini")
                }
                file.parentFile?.mkdirs()
                file.writeText(json.toString(2))
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun getApiKey(): String? {
        val file = getApiKeyFile()
        if (file.exists()) {
            val key = file.readText().trim()
            if (key.isNotBlank()) return key
        }
        return null
    }

    fun saveApiKey(key: String): Boolean {
        return try {
            geminiDir.mkdirs()
            agyCliDir.mkdirs()
            getApiKeyFile().writeText(key.trim())

            val modelSlug = getSelectedModel()
            for (file in listOf(getSettingsFile(), getAgyCliSettingsFile())) {
                val json = if (file.exists()) {
                    try { org.json.JSONObject(file.readText()) } catch (_: Exception) { org.json.JSONObject() }
                } else org.json.JSONObject()

                json.put("modelProvider", "gemini")
                json.put("model", modelSlug)
                json.put("agentMode", "accept-edits")
                file.parentFile?.mkdirs()
                file.writeText(json.toString(2))
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun isLoggedIn(): Boolean {
        // 1. Direct Gemini API Key mode
        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank()) return true

        // 2. Google ADC mode
        val adc = getAdcFile()
        if (adc.exists() && adc.length() > 0) return true

        // 3. Google OAuth creds mode
        val creds = getOauthCredsFile()
        return creds.exists() && creds.length() > 0
    }

    fun getAuthAccount(): String? {
        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank()) {
            return "Gemini API (Active)"
        }

        val adcFile = getAdcFile()
        if (adcFile.exists()) {
            try {
                val json = org.json.JSONObject(adcFile.readText())
                val account = json.optString("account", "")
                if (account.isNotBlank()) return account
            } catch (_: Exception) {}
        }

        val accountsFile = getGoogleAccountsFile()
        if (accountsFile.exists()) {
            try {
                val json = org.json.JSONObject(accountsFile.readText())
                val active = json.optString("active", "")
                if (active.isNotBlank()) return active
            } catch (_: Exception) {}
        }

        val credsFile = getOauthCredsFile()
        if (credsFile.exists()) {
            try {
                val json = org.json.JSONObject(credsFile.readText())
                if (json.has("id_token")) {
                    val idToken = json.getString("id_token")
                    val parts = idToken.split(".")
                    if (parts.size >= 2) {
                        val base64Part = parts[1]
                        val decodedBytes = try {
                            java.util.Base64.getUrlDecoder().decode(base64Part)
                        } catch (_: Exception) {
                            java.util.Base64.getDecoder().decode(base64Part)
                        }
                        val payload = String(decodedBytes)
                        val jwtJson = org.json.JSONObject(payload)
                        val email = jwtJson.optString("email", "")
                        if (email.isNotBlank()) return email
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    fun writeCredentials(oauthJson: String, email: String? = null): Boolean {
        try {
            geminiDir.mkdirs()
            agyCliDir.mkdirs()
            gcloudDir.mkdirs()
            getOauthCredsFile().writeText(oauthJson.trim())

            // Clear any lingering API key file so settings.json switches back to OAuth
            val keyFile = getApiKeyFile()
            if (keyFile.exists()) keyFile.delete()

            // To switch back to signing in, configure OAuth in settings.json in both locations
            val settingsJson = org.json.JSONObject()
            val sec = org.json.JSONObject()
            val auth = org.json.JSONObject()
            auth.put("selectedType", "oauth-personal")
            sec.put("auth", auth)
            settingsJson.put("security", sec)
            settingsJson.put("model", "Gemini 3.8 Flash (High)")
            settingsJson.put("agentMode", "accept-edits")
            getSettingsFile().writeText(settingsJson.toString(2))
            getAgyCliSettingsFile().writeText(settingsJson.toString(2))

            // Parse OAuth tokens to construct standard Google ADC format for libagy.so
            var refreshToken: String? = null
            var userEmail = email
            try {
                val parsed = org.json.JSONObject(oauthJson)
                if (parsed.has("refresh_token") && parsed.getString("refresh_token").isNotBlank()) {
                    refreshToken = parsed.getString("refresh_token")
                }
                if (userEmail == null && parsed.has("id_token")) {
                    val idToken = parsed.getString("id_token")
                    val parts = idToken.split(".")
                    if (parts.size >= 2) {
                        val decodedBytes = try {
                            java.util.Base64.getUrlDecoder().decode(parts[1])
                        } catch (_: Exception) {
                            java.util.Base64.getDecoder().decode(parts[1])
                        }
                        val jwtJson = org.json.JSONObject(String(decodedBytes))
                        userEmail = if (jwtJson.has("email")) jwtJson.getString("email") else null
                    }
                }
            } catch (_: Exception) {}

            // Preserve old refresh_token if new response omitted it
            if (refreshToken == null && getAdcFile().exists()) {
                try {
                    val oldAdc = org.json.JSONObject(getAdcFile().readText())
                    if (oldAdc.has("refresh_token")) {
                        refreshToken = oldAdc.getString("refresh_token")
                    }
                } catch (_: Exception) {}
            }

            val targetEmail = userEmail ?: getAuthAccount()

            if (!refreshToken.isNullOrBlank()) {
                val adcJson = org.json.JSONObject().apply {
                    put("client_id", AuthManager.CLIENT_ID)
                    put("client_secret", AuthManager.CLIENT_SECRET)
                    put("refresh_token", refreshToken)
                    put("type", "authorized_user")
                    if (!targetEmail.isNullOrBlank()) {
                        put("account", targetEmail)
                    }
                }
                val adcContent = adcJson.toString(2)
                getAdcFile().writeText(adcContent)
                getGcloudAdcFile().writeText(adcContent)
            }

            // Google accounts file
            if (!targetEmail.isNullOrBlank()) {
                val accountsJson = org.json.JSONObject()
                accountsJson.put("active", targetEmail)
                accountsJson.put("old", org.json.JSONArray())
                getGoogleAccountsFile().writeText(accountsJson.toString(2))
            }

            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun clearAuth(): Boolean {
        var success = true
        val creds = getOauthCredsFile()
        if (creds.exists()) {
            success = success && creds.delete()
        }
        val adc = getAdcFile()
        if (adc.exists()) {
            success = success && adc.delete()
        }
        val gcloudAdc = getGcloudAdcFile()
        if (gcloudAdc.exists()) {
            success = success && gcloudAdc.delete()
        }
        val accounts = getGoogleAccountsFile()
        if (accounts.exists()) {
            accounts.delete()
        }
        val keyFile = getApiKeyFile()
        if (keyFile.exists()) {
            keyFile.delete()
        }
        val settings = getSettingsFile()
        if (settings.exists()) {
            try {
                settings.writeText("{}")
            } catch (_: Exception) {}
        }
        val cliSettings = getAgyCliSettingsFile()
        if (cliSettings.exists()) {
            try {
                cliSettings.writeText("{}")
            } catch (_: Exception) {}
        }
        return success
    }

    companion object {
        fun toCanonicalModelSlug(nameOrId: String?): String {
            val s = nameOrId?.trim() ?: return "gemini-3.8-flash-high"
            return when {
                s.contains("3.8 Flash (Medium)", ignoreCase = true) || s == "gemini-3.8-flash-medium" -> "gemini-3.8-flash-medium"
                s.contains("3.8 Flash (Low)", ignoreCase = true) || s == "gemini-3.8-flash-low" -> "gemini-3.8-flash-low"
                s.contains("3.7 Flash (High)", ignoreCase = true) || s == "gemini-3.7-flash-high" -> "gemini-3.7-flash-high"
                s.contains("3.7 Flash (Medium)", ignoreCase = true) || s == "gemini-3.7-flash-medium" -> "gemini-3.7-flash-medium"
                s.contains("3.7 Flash (Low)", ignoreCase = true) || s == "gemini-3.7-flash-low" -> "gemini-3.7-flash-low"
                s.contains("3.6 Flash (High)", ignoreCase = true) || s == "gemini-3.6-flash-high" -> "gemini-3.6-flash-high"
                s.contains("3.1 Pro (High)", ignoreCase = true) || s == "gemini-3.1-pro-high" -> "gemini-3.1-pro-high"
                s.contains("Claude Sonnet 4.6", ignoreCase = true) || s == "claude-sonnet-4-6" -> "claude-sonnet-4-6"
                s.contains("Claude Opus 4.6", ignoreCase = true) || s == "claude-opus-4-6-thinking" -> "claude-opus-4-6-thinking"
                s.contains("GPT-OSS 120B", ignoreCase = true) || s == "gpt-oss-120b-medium" -> "gpt-oss-120b-medium"
                else -> "gemini-3.8-flash-high"
            }
        }

        fun toDisplayName(slugOrId: String?): String {
            val s = slugOrId?.trim() ?: return "Gemini 3.8 Flash (High)"
            return when (s) {
                "gemini-3.8-flash-high" -> "Gemini 3.8 Flash (High)"
                "gemini-3.8-flash-medium" -> "Gemini 3.8 Flash (Medium)"
                "gemini-3.8-flash-low" -> "Gemini 3.8 Flash (Low)"
                "gemini-3.7-flash-high" -> "Gemini 3.7 Flash (High)"
                "gemini-3.7-flash-medium" -> "Gemini 3.7 Flash (Medium)"
                "gemini-3.7-flash-low" -> "Gemini 3.7 Flash (Low)"
                "gemini-3.6-flash-high" -> "Gemini 3.6 Flash (High)"
                "gemini-3.1-pro-high" -> "Gemini 3.1 Pro (High)"
                "claude-sonnet-4-6" -> "Claude Sonnet 4.6 (Thinking)"
                "claude-opus-4-6-thinking" -> "Claude Opus 4.6 (Thinking)"
                "gpt-oss-120b-medium" -> "GPT-OSS 120B (Medium)"
                else -> if (s.contains("(")) s else "Gemini 3.8 Flash (High)"
            }
        }
    }
}
