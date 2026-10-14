package com.yashwant.agymobile.core.compiler

import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class BuildApkBridgeTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var appDataDir: File
    private lateinit var projectDir: File
    private lateinit var bootstrapManager: CompilerBootstrapManager
    private lateinit var engine: ApkCompilerEngine

    @Before
    fun setUp() {
        appDataDir = tempFolder.newFolder("app_data")
        projectDir = tempFolder.newFolder("TestApp")

        bootstrapManager = CompilerBootstrapManager(appDataDir)
        bootstrapManager.initialize()

        val userHome = System.getProperty("user.home") ?: ""
        val sdkJar = File(userHome, "Library/Android/sdk/platforms/android-35/android.jar")
        if (sdkJar.exists()) {
            sdkJar.copyTo(bootstrapManager.androidJarFile, overwrite = true)
        }

        val sdkKeystore = File(userHome, ".android/debug.keystore")
        if (sdkKeystore.exists()) {
            sdkKeystore.copyTo(bootstrapManager.debugKeystoreFile, overwrite = true)
        }

        engine = ApkCompilerEngine(bootstrapManager)
    }

    @Test
    fun testExecuteBuildReturnsStructuredJsonOnSuccess() {
        val aapt2 = bootstrapManager.getAapt2Executable()
        if (aapt2 == null || !bootstrapManager.androidJarFile.exists()) {
            return
        }

        File(projectDir, "AndroidManifest.xml").writeText(
            """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example.test">
                <application android:label="BridgeApp" android:hasCode="true">
                    <activity android:name=".MainActivity" android:exported="true" />
                </application>
            </manifest>
            """.trimIndent()
        )
        val srcDir = File(projectDir, "src/com/example/test").apply { mkdirs() }
        File(srcDir, "MainActivity.java").writeText(
            """
            package com.example.test;
            import android.app.Activity;
            public class MainActivity extends Activity {}
            """.trimIndent()
        )

        val (result, json) = BuildApkBridge.executeBuild(engine, projectDir)
        assertTrue(result.success)
        assertTrue(json.getBoolean("success"))
        assertEquals("BridgeApp", json.getString("app_name"))
        assertEquals("com.example.test", json.getString("package_name"))
        assertTrue(json.has("apk_path"))
        assertTrue(json.getLong("size_bytes") > 0)
    }

    @Test
    fun testExecuteBuildReturnsStructuredErrorsOnFailure() {
        val aapt2 = bootstrapManager.getAapt2Executable()
        if (aapt2 == null || !bootstrapManager.androidJarFile.exists()) {
            return
        }

        File(projectDir, "AndroidManifest.xml").writeText(
            """
            <manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example.fail">
                <application />
            </manifest>
            """.trimIndent()
        )
        val srcDir = File(projectDir, "src/com/example/fail").apply { mkdirs() }
        File(srcDir, "MainActivity.java").writeText(
            """
            package com.example.fail;
            public class MainActivity { syntax error }
            """.trimIndent()
        )

        val (result, json) = BuildApkBridge.executeBuild(engine, projectDir)
        assertFalse(result.success)
        assertFalse(json.getBoolean("success"))
        assertEquals("JAVA_COMPILATION", json.getString("failed_stage"))
        assertTrue(json.has("errors"))
        assertTrue(json.getJSONArray("errors").length() > 0)
        assertTrue(json.has("instruction"))
    }
}
