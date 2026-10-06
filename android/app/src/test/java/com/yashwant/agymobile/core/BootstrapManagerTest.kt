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

        assertTrue(manager.clearAuth())
        assertFalse(manager.isLoggedIn())
        assertNull(manager.getAuthAccount())
    }
}
