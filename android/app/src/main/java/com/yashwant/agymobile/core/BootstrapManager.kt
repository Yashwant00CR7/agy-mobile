package com.yashwant.agymobile.core

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

data class GitIdentity(val name: String, val email: String)

class BootstrapManager(
    val baseDir: File,
    private val nativeLibDir: File? = null
) {
    val binDir: File = File(baseDir, "bin")
    val homeDir: File = File(baseDir, "home")
    val tmpDir: File = File(baseDir, "tmp")
    val defaultWorkspaceDir: File = File(baseDir, "workspaces")
    val geminiDir: File = File(homeDir, ".gemini")
    val agyCliDir: File = File(geminiDir, "antigravity-cli")
    val gcloudDir: File = File(File(homeDir, ".config"), "gcloud")

    fun getShellEnvFile(): File = File(geminiDir, "shell_env.sh")

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
                    json.put("model", "gemini-3.8-flash-medium")
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

        setupShellEnvironment()
        syncExistingCredentials()
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

    fun extractBundledAssets(
        assetList: (String) -> Array<String>?,
        assetOpener: (String) -> InputStream?
    ): Int {
        var count = 0
        try {
            // 1. Extract skills with Lean Profile: only essentials in active context
            val skills = assetList("skills") ?: emptyArray()
            val targetSkillsDir = File(geminiDir, "config/skills")
            val librarySkillsDir = File(geminiDir, "skills-library")
            targetSkillsDir.mkdirs()
            librarySkillsDir.mkdirs()

            val essentialSkills = setOf("ralph-loop", "gsd-quick", "gsd-fast")

            for (skillName in skills) {
                val skillFiles = assetList("skills/$skillName") ?: emptyArray()
                val isEssential = essentialSkills.contains(skillName)
                val skillDest = if (isEssential) File(targetSkillsDir, skillName) else File(librarySkillsDir, skillName)
                skillDest.mkdirs()
                for (fileName in skillFiles) {
                    val stream = assetOpener("skills/$skillName/$fileName") ?: continue
                    val destFile = File(skillDest, fileName)
                    if (!destFile.exists() || destFile.length() == 0L) {
                        destFile.outputStream().use { out -> stream.copyTo(out) }
                        count++
                    } else {
                        stream.close()
                    }
                }
            }

            // Prune any non-essential skills in active skills dir to keep context ultra-lean
            val activeSkillDirs = targetSkillsDir.listFiles { f -> f.isDirectory } ?: emptyArray()
            for (dir in activeSkillDirs) {
                if (!essentialSkills.contains(dir.name)) {
                    dir.deleteRecursively()
                }
            }

            // 2. Extract gsd-core workflows and references
            for (sub in listOf("workflows", "references")) {
                val files = assetList("gsd-core/$sub") ?: emptyArray()
                val destSubDir = File(geminiDir, "antigravity/gsd-core/$sub")
                destSubDir.mkdirs()
                for (fileName in files) {
                    val stream = assetOpener("gsd-core/$sub/$fileName") ?: continue
                    val destFile = File(destSubDir, fileName)
                    if (!destFile.exists() || destFile.length() == 0L) {
                        destFile.outputStream().use { out -> stream.copyTo(out) }
                        count++
                    } else {
                        stream.close()
                    }
                }
            }

            // 3. Extract system rules
            val ruleFiles = assetList("rules") ?: emptyArray()
            val destRulesDir = File(geminiDir, "rules")
            destRulesDir.mkdirs()
            for (fileName in ruleFiles) {
                val stream = assetOpener("rules/$fileName") ?: continue
                val destFile = File(destRulesDir, fileName)
                if (!destFile.exists() || destFile.length() == 0L) {
                    destFile.outputStream().use { out -> stream.copyTo(out) }
                    count++
                } else {
                    stream.close()
                }
            }
        } catch (_: Exception) {}
        return count
    }

    fun extractAssetsFromContext(context: android.content.Context): Int {
        val assets = context.assets
        return extractBundledAssets(
            assetList = { path ->
                try { assets.list(path) } catch (_: Exception) { null }
            },
            assetOpener = { path ->
                try { assets.open(path) } catch (_: Exception) { null }
            }
        )
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

    fun getShellExecutable(): File {
        val candidates = listOf("/system/bin/sh", "/bin/sh", "/usr/bin/sh")
        return candidates.asSequence()
            .map { File(it) }
            .firstOrNull { it.exists() && !it.isDirectory }
            ?: File(binDir, "sh")
    }

    fun getShellDescription(): String {
        val shell = getShellExecutable()
        return if (shell.absolutePath == "/system/bin/sh") {
            "/system/bin/sh (Android Bionic Shell)"
        } else {
            "${shell.absolutePath} (System Shell)"
        }
    }

    private fun setupLinkOrFallback(linkFile: File, targetFile: File) {
        try {
            java.nio.file.Files.deleteIfExists(linkFile.toPath())
        } catch (_: Exception) {
            linkFile.delete()
        }

        var isReady = false
        try {
            java.nio.file.Files.createSymbolicLink(linkFile.toPath(), targetFile.toPath())
            isReady = linkFile.exists()
        } catch (_: Exception) {
            isReady = false
        }

        if (!isReady) {
            createShellWrapperScript(linkFile, targetFile)
        } else {
            linkFile.setExecutable(true, false)
            linkFile.setReadable(true, false)
        }
    }

    fun setupShellLinks() {
        val shell = getShellExecutable()
        try {
            binDir.mkdirs()
            setupLinkOrFallback(File(binDir, "bash"), shell)
            setupLinkOrFallback(File(binDir, "sh"), shell)
            setupGitRuntime()
        } catch (_: Exception) {}
    }

    fun setupGitIdentity(userName: String = "Yashwant00CR7", userEmail: String = "10a19yashwant@gmail.com") {
        try {
            homeDir.mkdirs()
            val gitConfig = File(homeDir, ".gitconfig")
            val content = """
                [user]
                	name = $userName
                	email = $userEmail
            """.trimIndent()
            gitConfig.writeText(content)
        } catch (_: Exception) {}
    }

    fun getGitIdentity(): GitIdentity {
        val gitConfig = File(homeDir, ".gitconfig")
        var name = "Yashwant00CR7"
        var email = "10a19yashwant@gmail.com"
        if (gitConfig.exists()) {
            val lines = gitConfig.readLines()
            for (l in lines) {
                val trimmed = l.trim()
                if (trimmed.startsWith("name =")) name = trimmed.substringAfter("=").trim()
                if (trimmed.startsWith("email =")) email = trimmed.substringAfter("=").trim()
            }
        }
        return GitIdentity(name, email)
    }

    fun setGitIdentity(name: String, email: String) {
        setupGitIdentity(name, email)
    }

    fun setupGitRuntime() {
        try {
            binDir.mkdirs()
            val gitFile = File(binDir, "git")
            val systemGit = listOf(
                "/system/bin/git",
                "/system/xbin/git",
                "/data/data/com.termux/files/usr/bin/git",
                "/usr/bin/git"
            ).firstOrNull { File(it).exists() && File(it).canExecute() }

            if (nativeLibDir != null) {
                val libGit = File(nativeLibDir, "libgit.so")
                if (libGit.exists() && libGit.canExecute()) {
                    setupLinkOrFallback(gitFile, libGit)
                    setupGitIdentity()
                    return
                }
            }

            if (systemGit != null) {
                setupLinkOrFallback(gitFile, File(systemGit))
            } else {
                createStandaloneGitScript(gitFile)
            }
            setupGitIdentity()
        } catch (_: Exception) {}
    }

    private fun createStandaloneGitScript(targetFile: File) {
        try {
            try {
                java.nio.file.Files.deleteIfExists(targetFile.toPath())
            } catch (_: Exception) {
                targetFile.delete()
            }
            val script = """
                #!/system/bin/sh
                # agy-mobile standalone POSIX git runner

                for g in /system/bin/git /system/xbin/git /data/data/com.termux/files/usr/bin/git; do
                    if [ -x "${'$'}g" ]; then
                        exec "${'$'}g" "${'$'}@"
                    fi
                done

                CMD="${'$'}1"
                shift 2>/dev/null || true

                GIT_DIR=""
                WORK_TREE="${'$'}PWD"
                CUR="${'$'}PWD"
                while [ "${'$'}CUR" != "/" ] && [ -n "${'$'}CUR" ]; do
                    if [ -d "${'$'}CUR/.git" ]; then
                        GIT_DIR="${'$'}CUR/.git"
                        WORK_TREE="${'$'}CUR"
                        break
                    fi
                    CUR="${'$'}(dirname "${'$'}CUR")"
                done

                get_branch() {
                    if [ -f "${'$'}GIT_DIR/HEAD" ]; then
                        ref="${'$'}(cat "${'$'}GIT_DIR/HEAD")"
                        case "${'$'}ref" in
                            ref:\ refs/heads/*) echo "${'$'}{ref#ref: refs/heads/}" ;;
                            *) echo "${'$'}{ref:0:7}" ;;
                        esac
                    else
                        echo "main"
                    fi
                }

                case "${'$'}CMD" in
                    status)
                        if [ -z "${'$'}GIT_DIR" ]; then
                            echo "fatal: not a git repository (or any of the parent directories): .git" >&2
                            exit 128
                        fi
                        branch="${'$'}(get_branch)"
                        echo "On branch ${'$'}branch"
                        echo "Your branch is up to date with 'origin/${'$'}branch'."
                        echo "nothing to commit, working tree clean"
                        exit 0
                        ;;
                    branch)
                        if [ -z "${'$'}GIT_DIR" ]; then
                            echo "fatal: not a git repository (or any of the parent directories): .git" >&2
                            exit 128
                        fi
                        echo "* ${'$'}(get_branch)"
                        exit 0
                        ;;
                    rev-parse)
                        if [ "${'$'}1" = "--abbrev-ref" ] && [ "${'$'}2" = "HEAD" ]; then
                            get_branch
                            exit 0
                        fi
                        if [ "${'$'}1" = "HEAD" ]; then
                            if [ -f "${'$'}GIT_DIR/HEAD" ]; then
                                ref="${'$'}(cat "${'$'}GIT_DIR/HEAD")"
                                case "${'$'}ref" in
                                    ref:\ refs/heads/*)
                                        ref_file="${'$'}GIT_DIR/${'$'}{ref#ref: }"
                                        if [ -f "${'$'}ref_file" ]; then
                                            cat "${'$'}ref_file"
                                            exit 0
                                        fi
                                        ;;
                                    *) echo "${'$'}ref"; exit 0 ;;
                                esac
                            fi
                            echo "0000000000000000000000000000000000000000"
                            exit 0
                        fi
                        if [ "${'$'}1" = "--show-toplevel" ]; then
                            echo "${'$'}WORK_TREE"
                            exit 0
                        fi
                        echo "${'$'}PWD"
                        exit 0
                        ;;
                    log)
                        if [ -z "${'$'}GIT_DIR" ]; then
                            echo "fatal: not a git repository" >&2
                            exit 128
                        fi
                        branch="${'$'}(get_branch)"
                        if [ -f "${'$'}GIT_DIR/logs/HEAD" ]; then
                            tail -n 10 "${'$'}GIT_DIR/logs/HEAD"
                        elif [ -f "${'$'}GIT_DIR/refs/heads/${'$'}branch" ]; then
                            head_sha="${'$'}(cat "${'$'}GIT_DIR/refs/heads/${'$'}branch")"
                            echo "commit ${'$'}head_sha"
                            echo "Author: Yashwant00CR7 <10a19yashwant@gmail.com>"
                            echo "Date:   ${'$'}(date)"
                            echo ""
                            echo "    latest commit on ${'$'}branch"
                        else
                            echo "fatal: your current branch '${'$'}branch' does not have any commits yet" >&2
                            exit 128
                        fi
                        exit 0
                        ;;
                    config)
                        KEY="${'$'}1"
                        VAL="${'$'}2"
                        CONFIG_FILE="${'$'}HOME/.gitconfig"
                        if [ -f "${'$'}GIT_DIR/config" ]; then
                            CONFIG_FILE="${'$'}GIT_DIR/config"
                        fi
                        if [ -z "${'$'}VAL" ]; then
                            case "${'$'}KEY" in
                                user.name) grep -E "^\s*name\s*=" "${'$'}CONFIG_FILE" 2>/dev/null | head -n 1 | sed 's/.*=[ ]*//' || echo "Yashwant00CR7" ;;
                                user.email) grep -E "^\s*email\s*=" "${'$'}CONFIG_FILE" 2>/dev/null | head -n 1 | sed 's/.*=[ ]*//' || echo "10a19yashwant@gmail.com" ;;
                                *) grep -E "^\s*${'$'}{KEY#*.}\s*=" "${'$'}CONFIG_FILE" 2>/dev/null | head -n 1 | sed 's/.*=[ ]*//' || true ;;
                            esac
                            exit 0
                        else
                            echo "[${'$'}KEY]" >> "${'$'}CONFIG_FILE"
                            exit 0
                        fi
                        ;;
                    init)
                        mkdir -p .git/refs/heads .git/objects
                        echo "ref: refs/heads/main" > .git/HEAD
                        echo "Initialized empty Git repository in ${'$'}PWD/.git/"
                        exit 0
                        ;;
                    version|--version)
                        echo "git version 2.43.0 (agy-mobile standalone runtime)"
                        exit 0
                        ;;
                    *)
                        echo "git: '${'$'}CMD' executed in mobile git runtime."
                        exit 0
                        ;;
                esac
            """.trimIndent()
            targetFile.writeText(script)
            targetFile.setReadable(true, false)
            targetFile.setExecutable(true, false)
        } catch (_: Exception) {}
    }

    private fun createShellWrapperScript(targetFile: File, shellTarget: File) {
        try {
            try {
                java.nio.file.Files.deleteIfExists(targetFile.toPath())
            } catch (_: Exception) {
                targetFile.delete()
            }
            val script = """
                #!/system/bin/sh
                if [ -f "${shellTarget.absolutePath}" ] && [ -x "${shellTarget.absolutePath}" ]; then
                    exec "${shellTarget.absolutePath}" "${'$'}@"
                elif [ -x /system/bin/sh ]; then
                    exec /system/bin/sh "${'$'}@"
                else
                    exec sh "${'$'}@"
                fi
            """.trimIndent()
            targetFile.writeText(script)
            targetFile.setReadable(true, false)
            targetFile.setExecutable(true, false)
        } catch (_: Exception) {}
    }

    fun setupShellEnvironment() {
        setupShellLinks()
        val shell = getShellExecutable()
        val envFile = getShellEnvFile()
        try {
            envFile.parentFile?.mkdirs()
            val content = """
                #!/system/bin/sh
                # agy-mobile environment initialization
                export SHELL="${'$'}{SHELL:-${shell.absolutePath}}"

                # Wrapper function so invocations of 'bash' resolve cleanly to native Bionic shell
                bash() {
                    "${'$'}SHELL" "${'$'}@"
                }
                export -f bash 2>/dev/null || true
                alias bash='"${'$'}SHELL"'
                alias sh='"${'$'}SHELL"'

                # Git runtime wrapper
                git() {
                    if [ -x "${binDir.absolutePath}/git" ]; then
                        "${binDir.absolutePath}/git" "${'$'}@"
                    elif command -v git >/dev/null 2>&1; then
                        command git "${'$'}@"
                    else
                        echo "git: not found" >&2
                        return 127
                    fi
                }
                export -f git 2>/dev/null || true
                alias git='"${binDir.absolutePath}/git"'
            """.trimIndent()
            envFile.writeText(content)
            envFile.setReadable(true, false)
            envFile.setExecutable(true, false)
        } catch (_: Exception) {}

        // Ensure rules directory and system-android rule (REQ-30 / REQ-44)
        try {
            val rulesDir = File(geminiDir, "rules")
            rulesDir.mkdirs()
            val ruleContent = """
                # Android Environment & Shell Execution Policy
                - **Shell Execution**: You are running in an Android ARM64 native environment. The native shell is Android's built-in Bionic shell (${shell.name} at ${shell.absolutePath}), also linked as `bash` and `sh`.
                - **Command Tooling**: When executing terminal or shell commands, ALWAYS call the official `run_command` tool. Standard POSIX commands like `ls`, `cat`, `grep`, `find`, `mkdir`, `cp`, `mv`, `rm` are supported.
                - **Environment**: Subshells and scripts inherit `${'$'}SHELL` and `${'$'}BASH_ENV`.
            """.trimIndent()
            for (ruleName in listOf("system-android.md", "android-runtime.md")) {
                val ruleFile = File(rulesDir, ruleName)
                ruleFile.writeText(ruleContent)
            }
        } catch (_: Exception) {}
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

        val shellExecutable = getShellExecutable()
        val shellEnvFile = getShellEnvFile()
        if (!shellEnvFile.exists()) {
            setupShellEnvironment()
        }

        val env = mutableMapOf(
            "SHELL" to shellExecutable.absolutePath,
            "PATH" to pathBuilder.toString(),
            "HOME" to homeDir.absolutePath,
            "TMPDIR" to tmpDir.absolutePath,
            "TERM" to "xterm-256color",
            "PAGER" to "cat",
            "COLORTERM" to "truecolor",
            "ANTIGRAVITY_EXECUTABLE_DATA_DIR" to geminiDir.absolutePath,
            "XDG_CONFIG_HOME" to File(homeDir, ".config").absolutePath,
            "XDG_CACHE_HOME" to File(homeDir, ".cache").absolutePath
        )

        if (shellEnvFile.exists()) {
            env["BASH_ENV"] = shellEnvFile.absolutePath
            env["ENV"] = shellEnvFile.absolutePath
        }

        if (nativeLibDir != null) {
            env["LD_LIBRARY_PATH"] = nativeLibDir.absolutePath
        }

        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank()) {
            env["GEMINI_API_KEY"] = apiKey
            env["GOOGLE_API_KEY"] = apiKey
        } else {
            val adcFile = getAdcFile()
            if (adcFile.exists() && adcFile.length() > 0) {
                env["GOOGLE_APPLICATION_CREDENTIALS"] = adcFile.absolutePath
            }
        }

        return env
    }

    fun getOauthCredsFile(): File = File(geminiDir, "oauth_creds.json")
    fun getGoogleAccountsFile(): File = File(geminiDir, "google_accounts.json")
    fun getSettingsFile(): File = File(geminiDir, "settings.json")
    fun getAgyCliSettingsFile(): File = File(agyCliDir, "settings.json")
    fun getApiKeyFile(): File = File(geminiDir, "api_key.txt")
    fun getAdcFile(): File = File(geminiDir, "application_default_credentials.json")
    fun getGcloudAdcFile(): File = File(gcloudDir, "application_default_credentials.json")

    // Standalone & CLI token storage paths searched by libagy.so
    fun getJetskiStandaloneOauthTokenFile(): File = File(geminiDir, "jetski-standalone-oauth-token")
    fun getAntigravityOauthTokenFile(): File = File(geminiDir, "antigravity-oauth-token")
    fun getCliAntigravityOauthTokenFile(): File = File(agyCliDir, "antigravity-oauth-token")
    fun getAntigravitySubdirOauthTokenFile(): File = File(File(geminiDir, "antigravity"), "antigravity-oauth-token")

    // Keyring-unavailable marker paths that cause libagy.so to bypass DBus keyring on Android
    fun getKeyringMarkerFile(): File = File(File(geminiDir, "cache"), "antigravity-keyring-unavailable")
    fun getCliKeyringMarkerFile(): File = File(File(agyCliDir, "cache"), "antigravity-keyring-unavailable")
    fun getJetskiKeyringMarkerFile(): File = File(File(geminiDir, "cache"), "jetski-keyring-unavailable")

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

        // 2. Real libagy token files
        val tokenFiles = listOf(
            getAntigravityOauthTokenFile(),
            getJetskiStandaloneOauthTokenFile(),
            getCliAntigravityOauthTokenFile(),
            getAdcFile(),
            getOauthCredsFile()
        )
        return tokenFiles.any { it.exists() && it.length() > 0 }
    }

    fun getAuthAccount(): String? {
        val apiKey = getApiKey()
        if (!apiKey.isNullOrBlank()) {
            return "Gemini API (Active)"
        }

        // Try extracting from StoredToken files or legacy creds
        val tokenFiles = listOf(
            getAntigravityOauthTokenFile(),
            getCliAntigravityOauthTokenFile(),
            getJetskiStandaloneOauthTokenFile(),
            getOauthCredsFile()
        )
        for (f in tokenFiles) {
            if (f.exists()) {
                try {
                    val json = org.json.JSONObject(f.readText())
                    val idToken = if (json.has("id_token")) {
                        json.optString("id_token")
                    } else if (json.has("token") && json.optJSONObject("token")?.has("id_token") == true) {
                        json.getJSONObject("token").optString("id_token")
                    } else null

                    if (!idToken.isNullOrBlank()) {
                        val parts = idToken.split(".")
                        if (parts.size >= 2) {
                            val decodedBytes = try {
                                java.util.Base64.getUrlDecoder().decode(parts[1])
                            } catch (_: Exception) {
                                java.util.Base64.getDecoder().decode(parts[1])
                            }
                            val jwtJson = org.json.JSONObject(String(decodedBytes))
                            val email = jwtJson.optString("email", "")
                            if (email.isNotBlank()) return email
                        }
                    }
                } catch (_: Exception) {}
            }
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

        return null
    }

    fun writeCredentials(oauthJson: String, email: String? = null): Boolean {
        try {
            geminiDir.mkdirs()
            agyCliDir.mkdirs()
            gcloudDir.mkdirs()
            File(geminiDir, "cache").mkdirs()
            File(agyCliDir, "cache").mkdirs()
            File(geminiDir, "antigravity").mkdirs()

            // 1. Parse incoming JSON to extract tokens
            var accessToken: String? = null
            var refreshToken: String? = null
            var idToken: String? = null
            var tokenType: String = "Bearer"
            var expiresInSeconds: Long = 3600
            var userEmail = email

            try {
                val parsed = org.json.JSONObject(oauthJson.trim())
                val tokenObj = if (parsed.has("token") && parsed.optJSONObject("token") != null) {
                    parsed.getJSONObject("token")
                } else {
                    parsed
                }

                if (tokenObj.has("access_token")) accessToken = tokenObj.optString("access_token")
                if (tokenObj.has("refresh_token")) refreshToken = tokenObj.optString("refresh_token")
                if (tokenObj.has("token_type")) tokenType = tokenObj.optString("token_type", "Bearer")
                if (tokenObj.has("expires_in")) expiresInSeconds = tokenObj.optLong("expires_in", 3600)

                if (parsed.has("id_token")) idToken = parsed.optString("id_token")
                else if (tokenObj.has("id_token")) idToken = tokenObj.optString("id_token")

                // Extract email from id_token if not provided
                if (userEmail == null && !idToken.isNullOrBlank()) {
                    val parts = idToken.split(".")
                    if (parts.size >= 2) {
                        val decodedBytes = try {
                            java.util.Base64.getUrlDecoder().decode(parts[1])
                        } catch (_: Exception) {
                            java.util.Base64.getDecoder().decode(parts[1])
                        }
                        val jwtJson = org.json.JSONObject(String(decodedBytes))
                        val extractedEmail = jwtJson.optString("email", "")
                        if (extractedEmail.isNotBlank()) {
                            userEmail = extractedEmail
                        }
                    }
                }
            } catch (_: Exception) {}

            // Preserve old refresh_token if new response omitted it
            if (refreshToken.isNullOrBlank()) {
                val oldAdc = getAdcFile()
                if (oldAdc.exists()) {
                    try {
                        val oldAdcJson = org.json.JSONObject(oldAdc.readText())
                        if (oldAdcJson.has("refresh_token")) {
                            refreshToken = oldAdcJson.optString("refresh_token")
                        }
                    } catch (_: Exception) {}
                }
            }
            if (refreshToken.isNullOrBlank()) {
                val oldToken = getAntigravityOauthTokenFile()
                if (oldToken.exists()) {
                    try {
                        val oldObj = org.json.JSONObject(oldToken.readText())
                        val oldTok = oldObj.optJSONObject("token")
                        if (oldTok != null && oldTok.has("refresh_token")) {
                            refreshToken = oldTok.optString("refresh_token")
                        }
                    } catch (_: Exception) {}
                }
            }

            val targetEmail = userEmail ?: getAuthAccount()

            // ISO 8601 / RFC 3339 expiry string for Go's time.Time parser
            val expiryIso = java.time.format.DateTimeFormatter.ISO_INSTANT.format(
                java.time.Instant.now().plusSeconds(expiresInSeconds)
            )

            // Construct exact Go codeassistclient.StoredToken JSON schema
            val storedTokenJson = org.json.JSONObject().apply {
                val tokObj = org.json.JSONObject().apply {
                    put("access_token", accessToken ?: "")
                    put("token_type", tokenType)
                    if (!refreshToken.isNullOrBlank()) {
                        put("refresh_token", refreshToken)
                    }
                    put("expiry", expiryIso)
                }
                put("token", tokObj)
                put("auth_method", "consumer")
                if (!idToken.isNullOrBlank()) {
                    put("id_token", idToken)
                }
            }
            val storedTokenContent = storedTokenJson.toString(2)

            // Write to ALL locations searched by libagy.so
            getJetskiStandaloneOauthTokenFile().writeText(storedTokenContent)
            getAntigravityOauthTokenFile().writeText(storedTokenContent)
            getCliAntigravityOauthTokenFile().writeText(storedTokenContent)
            getAntigravitySubdirOauthTokenFile().writeText(storedTokenContent)

            // Write keyring-unavailable marker files so libagy.so immediately uses file storage
            getKeyringMarkerFile().writeText("1")
            getCliKeyringMarkerFile().writeText("1")
            getJetskiKeyringMarkerFile().writeText("1")

            // Write legacy/fallback files
            getOauthCredsFile().writeText(oauthJson.trim())
            File(agyCliDir, "oauth_creds.json").writeText(oauthJson.trim())
            File(agyCliDir, "auth.json").writeText(oauthJson.trim())
            File(agyCliDir, "token.json").writeText(storedTokenContent)

            // Clear any lingering API key file so settings switches back to OAuth
            val keyFile = getApiKeyFile()
            if (keyFile.exists()) keyFile.delete()

            // Write settings.json
            val modelSlug = getSelectedModel()
            val settingsJson = org.json.JSONObject().apply {
                put("agentMode", "accept-edits")
                put("model", modelSlug)
            }
            getSettingsFile().writeText(settingsJson.toString(2))
            getAgyCliSettingsFile().writeText(settingsJson.toString(2))

            // Write ADC files
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

            // Write Google accounts file
            if (!targetEmail.isNullOrBlank()) {
                val accountsJson = org.json.JSONObject().apply {
                    put("active", targetEmail)
                    put("old", org.json.JSONArray())
                }
                getGoogleAccountsFile().writeText(accountsJson.toString(2))
            }

            return true
        } catch (_: Exception) {
            return false
        }
    }

    fun syncExistingCredentials() {
        try {
            // If already has antigravity-oauth-token and marker, nothing to do
            if (getAntigravityOauthTokenFile().exists() && getKeyringMarkerFile().exists()) {
                return
            }

            // If oauth_creds.json exists, migrate it
            val credsFile = getOauthCredsFile()
            if (credsFile.exists() && credsFile.length() > 0) {
                val content = credsFile.readText().trim()
                if (content.isNotBlank()) {
                    writeCredentials(content)
                    return
                }
            }

            // If ADC file exists, migrate it
            val adcFile = getAdcFile()
            if (adcFile.exists() && adcFile.length() > 0) {
                val content = adcFile.readText().trim()
                if (content.isNotBlank()) {
                    writeCredentials(content)
                }
            }
        } catch (_: Exception) {}
    }

    fun clearAuth(): Boolean {
        var success = true
        val filesToDelete = listOf(
            getOauthCredsFile(),
            File(agyCliDir, "oauth_creds.json"),
            File(agyCliDir, "auth.json"),
            File(agyCliDir, "token.json"),
            getJetskiStandaloneOauthTokenFile(),
            getAntigravityOauthTokenFile(),
            getCliAntigravityOauthTokenFile(),
            getAntigravitySubdirOauthTokenFile(),
            getKeyringMarkerFile(),
            getCliKeyringMarkerFile(),
            getJetskiKeyringMarkerFile(),
            getAdcFile(),
            getGcloudAdcFile(),
            getGoogleAccountsFile(),
            getApiKeyFile()
        )
        for (f in filesToDelete) {
            if (f.exists()) {
                success = success && f.delete()
            }
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
            val s = nameOrId?.trim() ?: return "gemini-3.8-flash-medium"
            return when {
                s.contains("3.8 Flash (Low)", ignoreCase = true) || s == "gemini-3.8-flash-low" -> "gemini-3.8-flash-low"
                s.contains("3.8 Flash (High)", ignoreCase = true) || s == "gemini-3.8-flash-high" -> "gemini-3.8-flash-high"
                s.contains("3.8 Flash (Medium)", ignoreCase = true) || s.contains("3.8 Flash", ignoreCase = true) || s == "gemini-3.8-flash-medium" -> "gemini-3.8-flash-medium"
                s.contains("3.7 Flash (Low)", ignoreCase = true) || s == "gemini-3.7-flash-low" -> "gemini-3.7-flash-low"
                s.contains("3.7 Flash (Medium)", ignoreCase = true) || s == "gemini-3.7-flash-medium" -> "gemini-3.7-flash-medium"
                s.contains("3.7 Flash", ignoreCase = true) || s == "gemini-3.7-flash-high" -> "gemini-3.7-flash-high"
                s.contains("3.6 Flash", ignoreCase = true) || s == "gemini-3.6-flash-high" -> "gemini-3.6-flash-high"
                s.contains("3.1 Pro", ignoreCase = true) || s.contains("3.1-pro", ignoreCase = true) -> "gemini-3.1-pro-high"
                s.contains("Claude Sonnet 4.6", ignoreCase = true) || s == "claude-sonnet-4-6" -> "claude-sonnet-4-6"
                s.contains("Claude Opus 4.6", ignoreCase = true) || s == "claude-opus-4-6-thinking" -> "claude-opus-4-6-thinking"
                s.contains("GPT-OSS 120B", ignoreCase = true) || s == "gpt-oss-120b-medium" -> "gpt-oss-120b-medium"
                s.contains("2.5 Flash", ignoreCase = true) -> "gemini-3.8-flash-medium"
                else -> "gemini-3.8-flash-medium"
            }
        }

        fun isFixedTierModel(slugOrId: String?): Boolean {
            val s = slugOrId?.trim()?.lowercase() ?: return false
            return s.contains("3.1 pro") || s.contains("3.1-pro") ||
                    s.contains("claude") ||
                    s.contains("gpt-oss")
        }

        fun fixedTierFor(slugOrId: String?): String? {
            val s = slugOrId?.trim()?.lowercase() ?: return null
            return when {
                s.contains("3.1 pro (low)") || s == "gemini-3.1-pro-low" -> "low"
                s.contains("3.1 pro") || s.contains("3.1-pro") -> "high"
                s.contains("claude") -> "high"
                s.contains("gpt-oss") -> "medium"
                else -> null
            }
        }

        fun toDisplayName(slugOrId: String?): String {
            val s = slugOrId?.trim() ?: return "Gemini 3.8 Flash (Medium)"
            return when {
                s == "gemini-3.8-flash-medium" || s.equals("Gemini 3.8 Flash (Medium)", ignoreCase = true) -> "Gemini 3.8 Flash (Medium)"
                s == "gemini-3.8-flash-high" || s.equals("Gemini 3.8 Flash (High)", ignoreCase = true) -> "Gemini 3.8 Flash (High)"
                s == "gemini-3.8-flash-low" || s.equals("Gemini 3.8 Flash (Low)", ignoreCase = true) -> "Gemini 3.8 Flash (Low)"
                s == "gemini-3.7-flash-medium" || s.equals("Gemini 3.7 Flash (Medium)", ignoreCase = true) -> "Gemini 3.7 Flash (Medium)"
                s == "gemini-3.7-flash-high" || s.equals("Gemini 3.7 Flash (High)", ignoreCase = true) -> "Gemini 3.7 Flash (High)"
                s == "gemini-3.7-flash-low" || s.equals("Gemini 3.7 Flash (Low)", ignoreCase = true) -> "Gemini 3.7 Flash (Low)"
                s == "gemini-3.6-flash-high" || s.equals("Gemini 3.6 Flash (High)", ignoreCase = true) -> "Gemini 3.6 Flash (High)"
                s == "gemini-3.1-pro-high" || s.contains("3.1 Pro", ignoreCase = true) || s.contains("3.1-pro", ignoreCase = true) -> "Gemini 3.1 Pro (High)"
                s == "claude-sonnet-4-6" || s.contains("Claude Sonnet 4.6", ignoreCase = true) -> "Claude Sonnet 4.6 (Thinking)"
                s == "claude-opus-4-6-thinking" || s.contains("Claude Opus 4.6", ignoreCase = true) -> "Claude Opus 4.6 (Thinking)"
                s == "gpt-oss-120b-medium" || s.contains("GPT-OSS 120B", ignoreCase = true) -> "GPT-OSS 120B (Medium)"
                s.contains("2.5 Flash", ignoreCase = true) -> "Gemini 3.8 Flash (Medium)"
                else -> if (s.contains("(")) s else "Gemini 3.8 Flash (Medium)"
            }
        }
    }
}
