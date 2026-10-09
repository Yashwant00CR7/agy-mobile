package com.yashwant.agymobile.core

import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class BootstrapManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testDirectoryInitialization() {
        val root = tempFolder.newFolder("app_files")
        val manager = BootstrapManager(root)

        assertTrue(manager.initializeDirectories())
        assertTrue(manager.binDir.exists())
        assertTrue(manager.homeDir.exists())
        assertTrue(manager.tmpDir.exists())
        assertTrue(manager.defaultWorkspaceDir.exists())
    }

    @Test
    fun testInstallBinary() {
        val root = tempFolder.newFolder("app_files")
        val manager = BootstrapManager(root)

        val dummyContent = "echo 'dummy binary'".toByteArray()
        val installed = manager.installBinary("custom-tool", ByteArrayInputStream(dummyContent))

        assertTrue(installed.exists())
        assertTrue(installed.canExecute())
        assertEquals(dummyContent.size.toLong(), installed.length())
    }

    @Test
    fun testSetupAgyFromNativeLib() {
        val root = tempFolder.newFolder("app_files")
        val nativeDir = tempFolder.newFolder("native_lib")
        val libAgy = File(nativeDir, "libagy.so")
        libAgy.writeText("#!/bin/sh\necho agy v1.3.0")
        libAgy.setExecutable(true)

        val manager = BootstrapManager(root, nativeDir)
        val agyFile = manager.setupAgyFromNativeLib()

        assertNotNull(agyFile)
        assertTrue(manager.isAgyReady())
        assertEquals("libagy.so", agyFile!!.name)
        assertEquals(libAgy.absolutePath, agyFile.absolutePath)
    }

    @Test
    fun testEnvironmentVariables() {
        val root = tempFolder.newFolder("app_files")
        val manager = BootstrapManager(root)

        val env = manager.getEnvironmentVariables()
        assertTrue(env.containsKey("PATH"))
        assertTrue(env["PATH"]!!.contains(manager.binDir.absolutePath))
        assertEquals(manager.homeDir.absolutePath, env["HOME"])
        assertEquals(manager.tmpDir.absolutePath, env["TMPDIR"])
        assertEquals("cat", env["PAGER"])

        val managerWithNative = BootstrapManager(root, tempFolder.newFolder("lib_dir"))
        val envNative = managerWithNative.getEnvironmentVariables()
        assertTrue(envNative.containsKey("LD_LIBRARY_PATH"))
        assertTrue(envNative["PATH"]!!.contains("lib_dir"))
    }

    @Test
    fun testCredentialManagement() {
        val root = tempFolder.newFolder("app_files_creds")
        val manager = BootstrapManager(root)

        assertFalse(manager.isLoggedIn())
        assertNull(manager.getAuthAccount())

        val sampleCreds = """
            {
              "access_token": "ya29.sample",
              "refresh_token": "1//sample_refresh",
              "token_type": "Bearer"
            }
        """.trimIndent()

        assertTrue(manager.writeCredentials(sampleCreds, "user@example.com"))
        assertTrue(manager.isLoggedIn())
        assertEquals("user@example.com", manager.getAuthAccount())
        assertTrue(manager.getSettingsFile().exists())
        assertTrue(manager.getAgyCliSettingsFile().exists())
        assertTrue(manager.getAdcFile().exists())
        assertTrue(manager.getGcloudAdcFile().exists())
        assertTrue(manager.getAntigravityOauthTokenFile().exists())
        assertTrue(manager.getJetskiStandaloneOauthTokenFile().exists())
        assertTrue(manager.getCliAntigravityOauthTokenFile().exists())
        assertTrue(manager.getKeyringMarkerFile().exists())

        val storedJson = org.json.JSONObject(manager.getAntigravityOauthTokenFile().readText())
        assertTrue(storedJson.has("token"))
        assertEquals("ya29.sample", storedJson.getJSONObject("token").getString("access_token"))
        assertEquals("1//sample_refresh", storedJson.getJSONObject("token").getString("refresh_token"))
        assertEquals("consumer", storedJson.getString("auth_method"))

        val adcEnv = manager.getEnvironmentVariables()
        assertFalse(adcEnv.containsKey("AGY_ADC_AUTH"))
        assertEquals(manager.getAdcFile().absolutePath, adcEnv["GOOGLE_APPLICATION_CREDENTIALS"])
        assertEquals(manager.geminiDir.absolutePath, adcEnv["ANTIGRAVITY_EXECUTABLE_DATA_DIR"])

        assertTrue(manager.clearAuth())
        assertFalse(manager.isLoggedIn())
        assertNull(manager.getAuthAccount())
        assertFalse(manager.getAdcFile().exists())
        assertFalse(manager.getAntigravityOauthTokenFile().exists())
        assertFalse(manager.getKeyringMarkerFile().exists())
    }

    @Test
    fun testModelSelectionAndPersistence() {
        val root = tempFolder.newFolder("app_files_model")
        val manager = BootstrapManager(root)
        manager.initializeDirectories()

        assertEquals("gemini-3.8-flash-medium", manager.getSelectedModel())
        assertEquals("Gemini 3.8 Flash (Medium)", manager.getSelectedModelDisplayName())

        assertTrue(manager.saveSelectedModel("Gemini 3.8 Flash (High)"))
        assertEquals("gemini-3.8-flash-high", manager.getSelectedModel())
        assertEquals("Gemini 3.8 Flash (High)", manager.getSelectedModelDisplayName())

        assertTrue(manager.saveSelectedModel("Gemini 3.8 Flash (Medium)"))
        assertEquals("gemini-3.8-flash-medium", manager.getSelectedModel())
        assertEquals("Gemini 3.8 Flash (Medium)", manager.getSelectedModelDisplayName())

        assertTrue(manager.saveSelectedModel("Claude Sonnet 4.6 (Thinking)"))
        assertEquals("claude-sonnet-4-6", manager.getSelectedModel())
        assertEquals("Claude Sonnet 4.6 (Thinking)", manager.getSelectedModelDisplayName())

        assertTrue(manager.saveSelectedModel("Gemini 3.1 Pro"))
        assertEquals("gemini-3.1-pro-high", manager.getSelectedModel())
        assertEquals("Gemini 3.1 Pro (High)", manager.getSelectedModelDisplayName())

        assertTrue(manager.saveSelectedModel("3.1 Pro"))
        assertEquals("gemini-3.1-pro-high", manager.getSelectedModel())
        assertEquals("Gemini 3.1 Pro (High)", manager.getSelectedModelDisplayName())
    }

    @Test
    fun testFixedTierModelIdentification() {
        assertTrue(BootstrapManager.isFixedTierModel("Gemini 3.1 Pro (High)"))
        assertTrue(BootstrapManager.isFixedTierModel("gemini-3.1-pro-high"))
        assertTrue(BootstrapManager.isFixedTierModel("gemini-3.1-pro-low"))
        assertTrue(BootstrapManager.isFixedTierModel("Claude Sonnet 4.6 (Thinking)"))
        assertTrue(BootstrapManager.isFixedTierModel("claude-sonnet-4-6"))
        assertTrue(BootstrapManager.isFixedTierModel("GPT-OSS 120B (Medium)"))

        assertFalse(BootstrapManager.isFixedTierModel("Gemini 3.8 Flash"))
        assertFalse(BootstrapManager.isFixedTierModel("gemini-3.8-flash-medium"))
        assertFalse(BootstrapManager.isFixedTierModel("Gemini 3.7 Flash"))

        assertEquals("high", BootstrapManager.fixedTierFor("Gemini 3.1 Pro (High)"))
        assertEquals("low", BootstrapManager.fixedTierFor("Gemini 3.1 Pro (Low)"))
        assertEquals("high", BootstrapManager.fixedTierFor("Claude Sonnet 4.6"))
        assertEquals("medium", BootstrapManager.fixedTierFor("GPT-OSS 120B"))
        assertNull(BootstrapManager.fixedTierFor("Gemini 3.8 Flash"))
    }

    @Test
    fun testApiKeyManagement() {
        val root = tempFolder.newFolder("app_files_apikey")
        val manager = BootstrapManager(root)

        assertFalse(manager.isLoggedIn())
        assertNull(manager.getApiKey())

        assertTrue(manager.saveApiKey("AIzaSyTest123"))
        assertTrue(manager.isLoggedIn())
        assertEquals("AIzaSyTest123", manager.getApiKey())
        assertEquals("Gemini API (Active)", manager.getAuthAccount())
        assertTrue(manager.getSettingsFile().readText().contains("gemini"))

        val env = manager.getEnvironmentVariables()
        assertEquals("AIzaSyTest123", env["GEMINI_API_KEY"])

        assertTrue(manager.clearAuth())
        assertFalse(manager.isLoggedIn())
        assertNull(manager.getApiKey())
    }

    @Test
    fun testShellResolutionWithNativeLibBash() {
        val root = tempFolder.newFolder("app_files_bash")
        val nativeDir = tempFolder.newFolder("native_lib_bash")
        val libBash = File(nativeDir, "libbash.so")
        libBash.writeText("#!/bin/sh\necho bash")
        libBash.setExecutable(true)

        val manager = BootstrapManager(root, nativeDir)
        val shell = manager.getShellExecutable()
        assertNotNull(shell)
        assertEquals("libbash.so", shell.name)
        assertEquals(libBash.absolutePath, shell.absolutePath)

        val env = manager.getEnvironmentVariables()
        assertEquals(libBash.absolutePath, env["SHELL"])
    }

    @Test
    fun testShellResolutionFallback() {
        val root = tempFolder.newFolder("app_files_fallback")
        val manager = BootstrapManager(root)
        val shell = manager.getShellExecutable()
        assertNotNull(shell)
        assertTrue(shell.name.contains("sh") || shell.name.contains("bash"))

        val env = manager.getEnvironmentVariables()
        assertTrue(env.containsKey("SHELL"))
        assertNotNull(env["SHELL"])
    }

    @Test
    fun testShellSymlinksCreation() {
        val root = tempFolder.newFolder("app_files_symlinks")
        val nativeDir = tempFolder.newFolder("native_lib_symlinks")
        val libBash = File(nativeDir, "libbash.so")
        libBash.writeText("#!/bin/sh\necho bash")
        libBash.setExecutable(true)

        val manager = BootstrapManager(root, nativeDir)
        manager.initializeDirectories()

        val bashLink = File(manager.binDir, "bash")
        assertTrue(bashLink.exists())
    }

    @Test
    fun testShellEnvironmentVariablesIncludeBashEnvAndEnv() {
        val root = tempFolder.newFolder("app_files_env")
        val nativeDir = tempFolder.newFolder("native_lib_env")
        val libBash = File(nativeDir, "libbash.so")
        libBash.writeText("#!/bin/sh\necho bash")
        libBash.setExecutable(true)

        val manager = BootstrapManager(root, nativeDir)
        manager.initializeDirectories()

        val env = manager.getEnvironmentVariables()
        assertTrue(env.containsKey("BASH_ENV"))
        assertTrue(env.containsKey("ENV"))
        val shellEnv = File(env["BASH_ENV"]!!)
        assertTrue(shellEnv.exists())
        val scriptContent = shellEnv.readText()
        assertTrue(scriptContent.contains("bash()"))
        assertTrue(scriptContent.contains("SHELL"))
    }

    @Test
    fun testAndroidRuntimeRuleCreated() {
        val root = tempFolder.newFolder("app_files_rule")
        val manager = BootstrapManager(root)
        manager.initializeDirectories()

        val ruleSystem = File(manager.geminiDir, "rules/system-android.md")
        val ruleRuntime = File(manager.geminiDir, "rules/android-runtime.md")
        assertTrue(ruleSystem.exists())
        assertTrue(ruleRuntime.exists())
        val ruleContent = ruleSystem.readText()
        assertTrue(ruleContent.contains("run_command"))
        assertTrue(ruleContent.contains("Android"))
    }

    @Test
    fun testExtractBundledAssets() {
        val root = tempFolder.newFolder("app_files_assets")
        val manager = BootstrapManager(root)
        manager.initializeDirectories()

        val extracted = manager.extractBundledAssets(
            assetList = { path ->
                when (path) {
                    "skills" -> arrayOf("ralph-loop", "gsd-plan-phase")
                    "skills/ralph-loop" -> arrayOf("SKILL.md")
                    "skills/gsd-plan-phase" -> arrayOf("SKILL.md")
                    "gsd-core/workflows" -> arrayOf("plan-phase.md")
                    "gsd-core/references" -> arrayOf("ui-brand.md")
                    "rules" -> arrayOf("system-android.md")
                    else -> emptyArray()
                }
            },
            assetOpener = { path ->
                ByteArrayInputStream("test asset content for $path".toByteArray())
            }
        )

        assertTrue(extracted > 0)
        // Essential skill extracted to active config/skills
        val activeSkillFile = File(manager.geminiDir, "config/skills/ralph-loop/SKILL.md")
        assertTrue(activeSkillFile.exists())
        assertEquals("test asset content for skills/ralph-loop/SKILL.md", activeSkillFile.readText())

        // Non-essential skill extracted to on-demand skills-library
        val librarySkillFile = File(manager.geminiDir, "skills-library/gsd-plan-phase/SKILL.md")
        assertTrue(librarySkillFile.exists())
        assertEquals("test asset content for skills/gsd-plan-phase/SKILL.md", librarySkillFile.readText())

        val workflowFile = File(manager.geminiDir, "antigravity/gsd-core/workflows/plan-phase.md")
        assertTrue(workflowFile.exists())
        assertEquals("test asset content for gsd-core/workflows/plan-phase.md", workflowFile.readText())

        val extractedRule = File(manager.geminiDir, "rules/system-android.md")
        assertTrue(extractedRule.exists())
    }

    @Test
    fun testSelfHealingShellSymlinksAfterNativeLibMove() {
        val root = tempFolder.newFolder("app_files_selfheal")
        val oldNativeDir = tempFolder.newFolder("old_native_lib")
        val oldLibBash = File(oldNativeDir, "libbash.so")
        oldLibBash.writeText("#!/bin/sh\necho old_bash")
        oldLibBash.setExecutable(true)

        val managerOld = BootstrapManager(root, oldNativeDir)
        managerOld.initializeDirectories()

        val bashLink = File(managerOld.binDir, "bash")
        assertTrue(bashLink.exists())

        // Simulate APK upgrade: old directory is deleted, new native lib dir is created with different path
        oldLibBash.delete()
        oldNativeDir.delete()

        // bashLink is now dangling symlink
        assertFalse(bashLink.exists())

        val newNativeDir = tempFolder.newFolder("new_native_lib")
        val newLibBash = File(newNativeDir, "libbash.so")
        newLibBash.writeText("#!/bin/sh\necho new_bash")
        newLibBash.setExecutable(true)

        // Initialize new manager with new native dir (simulates new app startup)
        val managerNew = BootstrapManager(root, newNativeDir)
        managerNew.initializeDirectories()

        // bashLink must be self-healed and exist
        assertTrue(bashLink.exists())
    }

    @Test
    fun testShellFallbackScriptCreation() {
        val root = tempFolder.newFolder("app_files_fallback_script")
        val manager = BootstrapManager(root)
        manager.initializeDirectories()

        val bashFile = File(manager.binDir, "bash")
        assertTrue(bashFile.exists())
    }
}

