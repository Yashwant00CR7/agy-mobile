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
        return binDir.mkdirs() or
                homeDir.mkdirs() or
                tmpDir.mkdirs() or
                defaultWorkspaceDir.mkdirs()
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

        return env
    }

    val geminiDir: File = File(homeDir, ".gemini")

    fun getOauthCredsFile(): File = File(geminiDir, "oauth_creds.json")
    fun getGoogleAccountsFile(): File = File(geminiDir, "google_accounts.json")
    fun getSettingsFile(): File = File(geminiDir, "settings.json")

    fun isLoggedIn(): Boolean {
        val creds = getOauthCredsFile()
        return creds.exists() && creds.length() > 0
    }

    fun getAuthAccount(): String? {
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
            getOauthCredsFile().writeText(oauthJson.trim())

            // Default security settings
            val settings = getSettingsFile()
            if (!settings.exists()) {
                val settingsJson = org.json.JSONObject()
                val sec = org.json.JSONObject()
                val auth = org.json.JSONObject()
                auth.put("selectedType", "oauth-personal")
                sec.put("auth", auth)
                settingsJson.put("security", sec)
                settings.writeText(settingsJson.toString(2))
            }

            // Google accounts file
            val targetEmail = email ?: getAuthAccount()
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
        val accounts = getGoogleAccountsFile()
        if (accounts.exists()) {
            accounts.delete()
        }
        return success
    }
}
