package com.yashwant.agymobile.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class AuthManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testExtractOAuthUrl() {
        val sampleStderr = """
            Authentication required. Please visit the URL to log in:
              https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=1071006060591.apps.googleusercontent.com&code_challenge=xyz&redirect_uri=https%3A%2F%2Fantigravity.google%2Foauth-callback
            Waiting for authentication (timeout 60s)...
        """.trimIndent()

        val extracted = AuthManager.extractOAuthUrl(sampleStderr)
        assertNotNull(extracted)
        assertTrue(extracted!!.startsWith("https://accounts.google.com/o/oauth2/auth"))
        assertTrue(extracted.contains("client_id=1071006060591"))

        val noUrlText = "Some random error message without any url"
        assertNull(AuthManager.extractOAuthUrl(noUrlText))
    }

    @Test
    fun testCheckAuthStateUnauthenticated() {
        val root = tempFolder.newFolder("auth_test_root")
        val bootstrapManager = BootstrapManager(root)
        val authManager = AuthManager(bootstrapManager)

        val state = authManager.checkAuthState()
        assertFalse(state.isAuthenticated)
        assertNull(state.userEmail)
        assertFalse(state.isAuthenticating)
        assertNull(state.authUrl)
    }

    @Test
    fun testImportCredentialsSuccess() {
        val root = tempFolder.newFolder("auth_test_import")
        val bootstrapManager = BootstrapManager(root)
        val authManager = AuthManager(bootstrapManager)

        val json = """
            {
              "access_token": "ya29.imported_token",
              "refresh_token": "1//refresh_token",
              "token_type": "Bearer"
            }
        """.trimIndent()

        val imported = authManager.importCredentials(json)
        assertTrue(imported)

        val state = authManager.authState.value
        assertTrue(state.isAuthenticated)
        assertTrue(bootstrapManager.isLoggedIn())
    }

    @Test
    fun testImportCredentialsInvalid() {
        val root = tempFolder.newFolder("auth_test_invalid")
        val bootstrapManager = BootstrapManager(root)
        val authManager = AuthManager(bootstrapManager)

        val imported = authManager.importCredentials("   ")
        assertFalse(imported)
        assertFalse(authManager.authState.value.isAuthenticated)
    }

    @Test
    fun testSignOut() {
        val root = tempFolder.newFolder("auth_test_signout")
        val bootstrapManager = BootstrapManager(root)
        val authManager = AuthManager(bootstrapManager)

        val json = """{"access_token": "token"}"""
        authManager.importCredentials(json)
        assertTrue(authManager.authState.value.isAuthenticated)

        assertTrue(authManager.signOut())
        assertFalse(authManager.authState.value.isAuthenticated)
        assertNull(authManager.authState.value.userEmail)
    }
}
