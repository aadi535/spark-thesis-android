package com.spark.ui.screens.main

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.*
import com.spark.ui.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import com.spark.ui.theme.GlassSurface
import com.spark.ui.theme.GlassEdge
import androidx.compose.ui.draw.clip
import com.spark.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spark.ui.screens.dashboard.GlobalSparkState
import com.spark.ui.screens.dashboard.NewDashboardScreen
import com.spark.ui.screens.generator.SparkAIScreen
import com.spark.ui.screens.prolab.ProLabScreen
import com.spark.ui.screens.profile.ProfileScreen
import com.spark.ui.screens.settings.SettingsScreen
import com.spark.ui.theme.AppBackground
import com.spark.ui.theme.ThemeManager
import com.spark.ui.theme.CardBackground
import com.spark.ui.theme.SparkCyan
import com.spark.ui.theme.TextPrimary
import com.spark.ui.theme.TextSecondary

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard_tab", "Home", Icons.Outlined.Home)
    object SparkAI : BottomNavItem("spark_ai_tab", "Spark AI", Icons.Outlined.AutoAwesome)
    object MindPalace : BottomNavItem("mind_palace_tab", "Palace", Icons.Outlined.EmojiObjects)
    object ProLab : BottomNavItem("prolab_tab", "Pro Lab", Icons.Outlined.Bolt)
    object Settings : BottomNavItem("settings_tab", "Settings", Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onNavigateToGenerator: () -> Unit,
    launchSparkFull: Boolean = false,
    sparkTitle: String = "",
    sparkCategory: String = "",
    sparkEmoji: String = "ðŸ§ ",
    sparkDescription: String = "",
    sparkSteps: Array<String> = emptyArray(),
    sparkFunFact: String = "",
    sparkIsAI: Boolean? = null,
    initialTabRoute: String? = null,
    onSparkConsumed: () -> Unit = {}
) {
    val bottomNavController = rememberNavController()

    // Topic to generate spark for (from Pro Lab â†’ Dashboard flow)
    var pendingGenerateTopic by remember { mutableStateOf<String?>(null) }

    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.SparkAI,
        BottomNavItem.MindPalace,
        BottomNavItem.ProLab,
        BottomNavItem.Settings
    )

    // Handle tab navigation from intent
    LaunchedEffect(initialTabRoute) {
        initialTabRoute?.let { tab ->
            val targetRoute = when (tab) {
                "spark_ai" -> BottomNavItem.SparkAI.route
                "mind_palace" -> BottomNavItem.MindPalace.route
                "pro_lab" -> BottomNavItem.ProLab.route
                else -> null
            }
            targetRoute?.let {
                bottomNavController.navigate(it) {
                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        containerColor = AppBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            val entry by bottomNavController.currentBackStackEntryAsState()
            val currentRoute = entry?.destination?.route
            val showReader = currentRoute == BottomNavItem.Dashboard.route &&
                GlobalSparkState.isFullSparkExpanded && GlobalSparkState.activeFullSpark != null
            if (!showReader) {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 24.dp, vertical = 10.dp)
                    .shadow(12.dp, RoundedCornerShape(30.dp), ambientColor = Color.Black.copy(alpha = 0.12f),
                        spotColor = Color.Black.copy(alpha = 0.16f))
                    .clip(RoundedCornerShape(30.dp)).background(GlassSurface)
                    .border(1.dp, GlassEdge, RoundedCornerShape(30.dp)).padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    items.forEach { item ->
                        val isSelected = currentRoute == item.route
                        val selectedAlpha by animateFloatAsState(if (isSelected) 1f else 0f,
                            tween(240), label = "tabHighlight")
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(26.dp))
                            .background(Color.Transparent)
                            .softPress {

                                    if (currentRoute == "profile_screen") {
                                        if (item.route == BottomNavItem.Dashboard.route) {
                                            bottomNavController.popBackStack(BottomNavItem.Dashboard.route, false)
                                        } else {
                                            bottomNavController.popBackStack(BottomNavItem.Dashboard.route, false)
                                            bottomNavController.navigate(item.route) {
                                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    } else if (currentRoute != item.route) {
                                        bottomNavController.navigate(item.route) {
                                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                            }.padding(vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(item.icon, contentDescription = item.title,
                                tint = if (isSelected) AppBackground else TextSecondary,
                                modifier = Modifier.size(34.dp).clip(CircleShape)
                                    .background(TextPrimary.copy(alpha = selectedAlpha)).padding(7.dp))
                            Spacer(Modifier.height(5.dp))
                            Text(item.title, fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isSelected) TextPrimary else TextMuted, maxLines = 1)
                        }
                    }
                }
            }
        }

    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Dashboard.route,
            enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(320)) { it / 16 } },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(320)) { -it / 16 } },
            popExitTransition = { fadeOut(tween(160)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(BottomNavItem.Dashboard.route) {
                NewDashboardScreen(
                    onNavigateToProLab = {
                        bottomNavController.navigate(BottomNavItem.ProLab.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToProfile = {
                        bottomNavController.navigate("profile_screen")
                    },
                    launchSparkFull = launchSparkFull,
                    sparkTitle = sparkTitle,
                    sparkCategory = sparkCategory,
                    sparkEmoji = sparkEmoji,
                    sparkDescription = sparkDescription,
                    sparkSteps = sparkSteps,
                    sparkFunFact = sparkFunFact,
                    sparkIsAI = sparkIsAI,
                    onSparkConsumed = onSparkConsumed,
                    generateTopicFromProLab = pendingGenerateTopic,
                    onGenerateTopicConsumed = { pendingGenerateTopic = null }
                )
            }
            composable(BottomNavItem.SparkAI.route) {
                SparkAIScreen()
            }
            composable(BottomNavItem.MindPalace.route) {
                MindPalaceScreen()
            }
            composable(BottomNavItem.ProLab.route) {
                ProLabScreen(
                    onNavigateToSettings = {
                        bottomNavController.navigate(BottomNavItem.Settings.route)
                    },
                    onGenerateSparkForTopic = { topic ->
                        // Set the pending topic and navigate to Dashboard
                        pendingGenerateTopic = topic
                        bottomNavController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onLogout = onLogout,
                    onDeleteAccount = onDeleteAccount,
                    onNavigateToProLab = {
                        bottomNavController.navigate(BottomNavItem.ProLab.route)
                    },
                    onNavigateToProfile = {
                        bottomNavController.navigate("profile_screen")
                    }
                )
            }
            composable("profile_screen") {
                ProfileScreen(
                    onLogout = onLogout,
                    onBack = {
                        bottomNavController.popBackStack()
                    }
                )
            }
        }
    }
}
