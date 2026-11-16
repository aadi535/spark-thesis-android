package com.spark

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import com.spark.data.local.DataStoreManager
import com.spark.ui.navigation.SparkNavGraph
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.theme.SparkTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationFlowTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val store get() = DataStoreManager(context)

    @Before fun resetAuthenticationOnly() {
        runBlocking { store.clearJwtToken() }
        FirebaseAuth.getInstance().signOut()
        compose.runOnUiThread {
            GlobalUserState.loadAccount(context, com.spark.data.local.AccountSessionManager.GUEST)
            GlobalUserState.hasCompletedOnboarding = true
            GlobalUserState.saveProgress(context)
            GlobalUserState.loadAccount(context, null)
        }
    }

    private fun openApp() {
        compose.setContent { SparkTheme { SparkNavGraph() } }
    }

    private fun waitForText(text: String) {
        compose.waitUntil(15000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun guestEntryClearsStaleTokenAndLogoutReturnsToLogin() {
        openApp()
        waitForText("Explore as a guest")
        // Simulate a stale token arriving after the login screen was opened.
        runBlocking { store.saveJwtToken("stale-test-session") }
        compose.onNodeWithText("Explore as a guest").performClick()
        waitForText("Settings")
        assertNull(runBlocking { store.jwtTokenFlow.first() })
        assertNull(FirebaseAuth.getInstance().currentUser)
        assertTrue(GlobalUserState.isGuest)
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Log Out").performScrollTo().performClick()
        waitForText("Continue with Google")
        assertNull(runBlocking { store.jwtTokenFlow.first() })
        assertFalse(GlobalUserState.isGuest)
    }

    @Test fun logoutClearsPersistedBackendSession() {
        runBlocking { store.saveSession("test-session-for-local-logout-only", com.spark.data.local.AccountSessionManager.emailKey("test@example.com")) }
        compose.runOnUiThread {
            GlobalUserState.loadAccount(context, com.spark.data.local.AccountSessionManager.emailKey("test@example.com"))
            GlobalUserState.hasCompletedOnboarding = true
        }
        openApp()
        waitForText("Settings")
        compose.onNodeWithText("Settings").performClick()
        compose.onNodeWithText("Log Out").performScrollTo().performClick()
        waitForText("Continue with Google")
        assertNull(runBlocking { store.jwtTokenFlow.first() })
        assertNull(FirebaseAuth.getInstance().currentUser)
        assertFalse(GlobalUserState.isLoggedIn)
    }

    @Test fun oldProfileLoginFlagDoesNotBypassLogin() {
        compose.runOnUiThread { GlobalUserState.isLoggedIn = true }
        openApp()
        waitForText("Continue with Google")
        compose.onNodeWithText("Explore as a guest").assertIsDisplayed()
    }
    @Test fun retrofitInterceptorUsesStoredJwtAndOmitsItForGuestAndExchange() {
        var authorization: String? = null
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(com.spark.data.network.AuthInterceptor(store))
            .addInterceptor { chain ->
                authorization = chain.request().header("Authorization")
                okhttp3.Response.Builder().request(chain.request())
                    .protocol(okhttp3.Protocol.HTTP_1_1).code(200).message("OK")
                    .body(okhttp3.ResponseBody.create(null, "")).build()
            }.build()
        fun request(path: String) {
            client.newCall(okhttp3.Request.Builder().url("https://example.invalid$path").build())
                .execute().close()
        }
        runBlocking { store.saveJwtToken("backend-test-jwt") }
        request("/api/v1/user/progress")
        assertEquals("Bearer backend-test-jwt", authorization)
        request("/api/v1/auth/google")
        assertNull(authorization)
        runBlocking { store.clearJwtToken() }
        request("/api/v1/user/progress")
        assertNull(authorization)
    }

}
