package com.yashwant.agymobile.ui

import com.yashwant.agymobile.model.AuthState
import org.junit.Assert.*
import org.junit.Test

class AuthComponentTest {

    @Test
    fun testAuthStateDefaults() {
        val defaultState = AuthState()
        assertFalse(defaultState.isAuthenticated)
        assertNull(defaultState.userEmail)
        assertFalse(defaultState.isAuthenticating)
        assertNull(defaultState.authUrl)
        assertNull(defaultState.error)
    }

    @Test
    fun testAuthStateTransition() {
        val initial = AuthState()
        val authenticating = initial.copy(
            isAuthenticating = true,
            authUrl = "https://accounts.google.com/o/oauth2/auth?client_id=123"
        )
        assertTrue(authenticating.isAuthenticating)
        assertNotNull(authenticating.authUrl)
        assertFalse(authenticating.isAuthenticated)

        val authenticated = authenticating.copy(
            isAuthenticated = true,
            userEmail = "developer@example.com",
            isAuthenticating = false,
            authUrl = null
        )
        assertTrue(authenticated.isAuthenticated)
        assertEquals("developer@example.com", authenticated.userEmail)
        assertFalse(authenticated.isAuthenticating)
        assertNull(authenticated.authUrl)
    }
}
