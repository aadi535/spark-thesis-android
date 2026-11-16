package com.spark.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spark.data.local.DataStoreManager
import com.spark.data.local.AccountSessionManager
import com.spark.data.network.SparkApiService
import androidx.compose.runtime.key
import com.spark.ui.screens.auth.LoginScreen
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.screens.main.MainScreen
import com.spark.ui.screens.onboarding.OnboardingScreen
import org.koin.compose.koinInject

@Composable
fun SparkNavGraph(
    launchSparkFull: Boolean = false,
    sparkTitle: String = "",
    sparkCategory: String = "",
    sparkEmoji: String = "🧠",
    sparkDescription: String = "",
    sparkSteps: Array<String> = emptyArray(),
    sparkFunFact: String = "",
    sparkIsAI: Boolean? = null,
    initialTabRoute: String? = null,
    onSparkConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val dataStoreManager: DataStoreManager = koinInject()
    val apiService: SparkApiService = koinInject()
    val progressSync: com.spark.data.network.ProgressSync = koinInject()
    var deleting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val scope = rememberCoroutineScope()
    // Read the stored session before choosing the initial route; old profile flags are not proof of login.
    val storedToken by produceState<String?>(initialValue = null) {
        val (token, owner) = dataStoreManager.sessionFlow.first()
        val account = owner?.takeIf { it == AccountSessionManager.GUEST || !token.isNullOrBlank() }
        if (account != AccountSessionManager.accountKey || account == null) {
            GlobalUserState.loadAccount(context, account)
            val session = AccountSessionManager.generation
            if (account != null && account != AccountSessionManager.GUEST) {
                try {
                    val progress = apiService.getUserProgress("Bearer $token")
                    if (session == AccountSessionManager.generation) {
                        GlobalUserState.applyBackendProgress(context, progress)
                    }
                } catch (e: retrofit2.HttpException) {
                    if (session == AccountSessionManager.generation && (e.code() == 401 || e.code() == 403)) {
                        dataStoreManager.clearJwtToken()
                        GlobalUserState.loadAccount(context, null)
                    }
                } catch (e: java.io.IOException) {
                    // Restore only this known account's cache while offline.
                }
            }
        }
        // Legacy tokens without an owner never select legacy/test progress.
        value = if (AccountSessionManager.accountKey == null) "" else token.orEmpty()
    }
    if (storedToken == null) return
    val startDestination = remember {
        when {
            storedToken.isNullOrEmpty() && !GlobalUserState.isGuest -> "login"
            !GlobalUserState.hasCompletedOnboarding -> "onboarding"
            else -> "main"
        }
    }

    if (deleting) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {},
            title = { androidx.compose.material3.Text("Deleting Spark account") },
            text = { androidx.compose.material3.CircularProgressIndicator() },
            confirmButton = {}
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(500)
            )
        }
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    GlobalUserState.saveProgress(context)
                    if (GlobalUserState.hasCompletedOnboarding) {
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("onboarding") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    GlobalUserState.hasCompletedOnboarding = true
                    GlobalUserState.saveProgress(context)
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("main") {
            if (AccountSessionManager.accountKey != null) key(AccountSessionManager.accountKey) {
            MainScreen(
                onDeleteAccount = {
                    val owner = AccountSessionManager.accountKey
                    if (!deleting && owner != null) {
                        deleting = true
                        scope.launch {
                            var backendDeleted = false
                            try {
                                progressSync.deleteAccount(owner)
                                backendDeleted = true
                                // Deletion has its own exit: ordinary logout would save local data again.
                                kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
                                    try {
                                        dataStoreManager.clearJwtToken()
                                    } finally {
                                        GlobalUserState.deleteAccount(context, owner)
                                        FirebaseAuth.getInstance().signOut()
                                        onSparkConsumed()
                                    }
                                    try {
                                        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
                                    } catch (e: androidx.credentials.exceptions.ClearCredentialException) {
                                        // Spark's Firebase and backend sessions are already cleared.
                                    }
                                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            } catch (e: kotlinx.coroutines.CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                val message = if (backendDeleted)
                                    "Spark account deleted. Please restart Spark to finish clearing the session."
                                else "Could not confirm account deletion. Please check your connection and retry."
                                android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                            } finally {
                                progressSync.finishDeletion()
                                deleting = false
                            }
                        }
                    }
                },
                onLogout = {
                    scope.launch {
                        try {
                            GlobalUserState.saveProgress(context)
                            com.spark.ui.screens.main.MindPalaceData.saveToLocal(context)
                            dataStoreManager.clearJwtToken()
                            FirebaseAuth.getInstance().signOut()
                            GlobalUserState.loadAccount(context, null)
                            onSparkConsumed()
                            try {
                                CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
                            } catch (e: androidx.credentials.exceptions.ClearCredentialException) {
                                // Local and Firebase sessions are already cleared.
                            }
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        } catch (e: java.io.IOException) {
                            android.widget.Toast.makeText(context, "Could not clear the session. Please retry logout.",
                                android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                },
                onNavigateToGenerator = {
                    navController.navigate("generator")
                },
                launchSparkFull = launchSparkFull,
                sparkTitle = sparkTitle,
                sparkCategory = sparkCategory,
                sparkEmoji = sparkEmoji,
                sparkDescription = sparkDescription,
                sparkSteps = sparkSteps,
                sparkFunFact = sparkFunFact,
                    sparkIsAI = sparkIsAI,
                initialTabRoute = initialTabRoute,
                onSparkConsumed = onSparkConsumed
            )
            }
        }
        
        composable("generator") {
            com.spark.ui.screens.generator.GeneratorScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
