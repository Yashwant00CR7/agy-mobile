package com.yashwant.agymobile.viewmodel

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompilerDeprecationRoutingTest {

    private fun isDeprecatedBuildCommand(text: String): Boolean {
        val lower = text.lowercase().trim()
        return lower == "build-apk" || lower == "compile-apk" || lower == "/build-apk"
    }

    @Test
    fun testExplicitBuildCommandsAreInterceptedWithDeprecation() {
        assertTrue(isDeprecatedBuildCommand("build-apk"))
        assertTrue(isDeprecatedBuildCommand("compile-apk"))
        assertTrue(isDeprecatedBuildCommand("/build-apk"))
        assertTrue(isDeprecatedBuildCommand("  build-apk  "))
    }

    @Test
    fun testConversationalPromptsAreNeverIntercepted() {
        // These used to be hijacked by the regex in v2.3.0 causing user frustration.
        // Now they must flow directly to the AI!
        assertFalse(isDeprecatedBuildCommand("create an app"))
        assertFalse(isDeprecatedBuildCommand("build an app with Compose"))
        assertFalse(isDeprecatedBuildCommand("make a new project"))
        assertFalse(isDeprecatedBuildCommand("generate a sample application"))
        assertFalse(isDeprecatedBuildCommand("can you build an apk for me?"))
        assertFalse(isDeprecatedBuildCommand("compile this java file"))
    }
}
