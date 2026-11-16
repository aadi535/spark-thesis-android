package com.spark.ui.screens.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.spark.data.local.DataStoreManager
import com.spark.data.local.AccountSessionManager
import com.spark.data.network.GoogleAuthRequest
import com.spark.data.network.SparkApiService
import com.spark.ui.screens.main.GlobalUserState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val name: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val apiService: SparkApiService,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    fun signInWithGoogle(context: Context) {
        if (_authState.value is AuthState.Loading) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val auth = FirebaseAuth.getInstance()
            try {
                dataStoreManager.clearJwtToken()
                GlobalUserState.loadAccount(context, null)
                val resource = context.resources.getIdentifier(
                    "default_web_client_id", "string", context.packageName
                )
                check(resource != 0) { "Google sign-in configuration is missing." }
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(resource))
                    .setAutoSelectEnabled(true)
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val credential = CredentialManager.create(context)
                    .getCredential(context, request).credential
                check(credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)
                val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken

                // Firebase and Spring Boot both receive the original Google ID token.
                suspendCancellableCoroutine<Unit> { continuation ->
                    auth.signInWithCredential(GoogleAuthProvider.getCredential(googleToken, null))
                        .addOnCompleteListener { task ->
                            if (!continuation.isActive) {
                                auth.signOut()
                            } else if (task.isSuccessful) {
                                continuation.resume(Unit)
                            } else {
                                continuation.resumeWithException(
                                    task.exception ?: IllegalStateException("Firebase sign-in failed")
                                )
                            }
                        }
                }
                val response = apiService.authenticateWithGoogle(GoogleAuthRequest(googleToken))
                check(response.token.isNotBlank())
                val account = AccountSessionManager.emailKey(response.email)
                dataStoreManager.saveSession(response.token, account)
                GlobalUserState.loadAccount(context, account)
                GlobalUserState.userName = response.name
                GlobalUserState.userEmail = response.email
                GlobalUserState.isLoggedIn = true
                GlobalUserState.isGuest = false
                com.spark.ui.screens.prolab.ProLabPreferences.setBackendProStatus(context, response.isPro)
                try {
                    GlobalUserState.applyBackendProgress(context, apiService.getUserProgress())
                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 401 || e.code() == 403) throw e
                    // Keep only this account's local snapshot if the progress service is unavailable.
                } catch (e: java.io.IOException) {
                    // Offline fallback is restricted to this account's own local data.
                }
                GlobalUserState.saveProgress(context)
                _authState.value = AuthState.Success(GlobalUserState.userName)
            } catch (e: CancellationException) {
                auth.signOut()
                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) { dataStoreManager.clearJwtToken() }
                GlobalUserState.loadAccount(context, null)
                throw e
            } catch (e: Exception) {
                auth.signOut()
                dataStoreManager.clearJwtToken()
                GlobalUserState.loadAccount(context, null)
                _authState.value = AuthState.Error("Sign-in could not be completed. Please try again.")
            }
        }
    }

    fun enterGuestMode(context: Context) {
        if (_authState.value is AuthState.Loading) return
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                dataStoreManager.clearJwtToken()
                FirebaseAuth.getInstance().signOut()
                dataStoreManager.saveSession(null, AccountSessionManager.GUEST)
                GlobalUserState.loadAccount(context, AccountSessionManager.GUEST)
                _authState.value = AuthState.Success("Guest Explorer")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Could not open guest mode. Please try again.")
            }
        }
    }
}
