package com.yashwant.agymobile.core.compiler

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.ByteArrayInputStream
import java.io.File

class CompilerBootstrapManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var baseDir: File
    private lateinit var nativeLibDir: File
    private lateinit var manager: CompilerBootstrapManager

    @Before
    fun setUp() {
        baseDir = tempFolder.newFolder("app_data")
        nativeLibDir = tempFolder.newFolder("native_libs")
        manager = CompilerBootstrapManager(baseDir, nativeLibDir)
    }

    @Test
    fun testInitializationCreatesCompilerDir() {
        assertTrue(manager.initialize())
        assertTrue(manager.compilerDir.exists())
    }

    @Test
    fun testExtractCompilerAssets() {
        val dummyJar = "PK_DUMMY_ANDROID_JAR".toByteArray()
        val dummyKeystore = "KEYSTORE_DATA".toByteArray()

        val extracted = manager.extractCompilerAssets { assetPath ->
            when (assetPath) {
                "compiler/android.jar" -> ByteArrayInputStream(dummyJar)
                "compiler/debug.keystore" -> ByteArrayInputStream(dummyKeystore)
                else -> null
            }
        }

        assertEquals(2, extracted)
        assertTrue(manager.androidJarFile.exists())
        assertEquals(dummyJar.size.toLong(), manager.androidJarFile.length())
        assertTrue(manager.debugKeystoreFile.exists())
        assertEquals(dummyKeystore.size.toLong(), manager.debugKeystoreFile.length())

        // Re-run should not extract if already present
        val secondRun = manager.extractCompilerAssets { null }
        assertEquals(0, secondRun)
    }

    @Test
    fun testGetAapt2FromNativeLibDir() {
        val libAapt2 = File(nativeLibDir, "libaapt2.so")
        libAapt2.writeText("#!/bin/sh\necho aapt2")
        libAapt2.setExecutable(true)

        val resolved = manager.getAapt2Executable()
        assertNotNull(resolved)
        assertEquals(libAapt2.absolutePath, resolved!!.absolutePath)
    }

    @Test
    fun testGetAapt2FromBinDir() {
        val emptyNativeLib = tempFolder.newFolder("empty_native_libs")
        val customManager = CompilerBootstrapManager(baseDir, emptyNativeLib)
        val binDir = File(baseDir, "bin").apply { mkdirs() }
        val binAapt2 = File(binDir, "libaapt2.so").apply {
            writeText("dummy binary")
            setExecutable(true)
        }

        val resolved = customManager.getAapt2Executable()
        assertNotNull(resolved)
        assertEquals(binAapt2.absolutePath, resolved!!.absolutePath)
    }
}
