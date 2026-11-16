package com.spark.ui.screens.onboarding

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.spark.ui.theme.*

/**
 * Check if Usage Stats permission is granted.
 */
fun hasUsageStatsPermission(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

/**
 * Check if Overlay (Draw Over Other Apps) permission is granted.
 */
fun hasOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}

/**
 * Check if Battery Optimization is ignored (Background Run allowed).
 */
fun hasBatteryOptimizationPermission(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(context.packageName)
}

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Slide state: 0 = App Intro 1, 1 = App Intro 2, 2 = Permissions Setup
    var currentPage by remember { mutableIntStateOf(0) }

    // Track permission states
    var usageGranted by remember { mutableStateOf(hasUsageStatsPermission(context)) }
    var overlayGranted by remember { mutableStateOf(hasOverlayPermission(context)) }
    var batteryGranted by remember { mutableStateOf(hasBatteryOptimizationPermission(context)) }

    // Re-check permissions on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageGranted = hasUsageStatsPermission(context)
                overlayGranted = hasOverlayPermission(context)
                batteryGranted = hasBatteryOptimizationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Auto-advance when all permissions are granted
    LaunchedEffect(usageGranted, overlayGranted, batteryGranted, currentPage) {
        if (currentPage == 2 && usageGranted && overlayGranted && batteryGranted) {
            kotlinx.coroutines.delay(800)
            onComplete()
        }
    }

    // Glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(4.dp)
                            .width(if (currentPage == index) 28.dp else 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (currentPage == index) SparkCyan
                                else SurfaceVariant.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            // Main Content Body based on currentPage
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + slideInHorizontally { width -> if (targetState > initialState) width else -width } togetherWith
                            fadeOut(animationSpec = tween(300)) + slideOutHorizontally { width -> if (targetState > initialState) -width else width }
                },
                modifier = Modifier.weight(1f),
                label = "pageTransition"
            ) { page ->
                when (page) {
                    0 -> IntroSlideOne()
                    1 -> IntroSlideTwo()
                    2 -> PermissionSetupSlide(
                        usageGranted = usageGranted,
                        overlayGranted = overlayGranted,
                        batteryGranted = batteryGranted,
                        glowAlpha = glowAlpha,
                        onGrantUsage = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        },
                        onGrantOverlay = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        },
                        onGrantBattery = {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }

            // Bottom Action Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (currentPage < 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.horizontalGradient(listOf(SparkCyan, SparkCyan))
                            )
                            .softPress { currentPage++ },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (currentPage == 0) "How it works" else "Set up my focus",
                                color = OnAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Skip Tutorial",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier
                            .softPress { onComplete() }
                            .padding(8.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(28.dp))
                            .background(
                                if (usageGranted && overlayGranted) SparkCyan else GradientPurple
                            )
                            .softPress { onComplete() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (usageGranted && overlayGranted) "🚀 Enter Spark Dashboard" else "Continue to App",
                            color = if (usageGranted && overlayGranted) Color.Black else TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IntroSlideOne() {
    Column(Modifier.fillMaxSize().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        SparkOrb(Modifier.size(200.dp))
        Spacer(Modifier.height(32.dp))
        ScreenHeading("A fresh start", "A small pause.\nA new perspective.",
            "Small moments of learning, right where distractions used to be.")
        Row(Modifier.fillMaxWidth().premiumSurface().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("30-second ideas", color = TextPrimary, fontSize = 13.sp)
            Text("More focus", color = SparkCyan, fontSize = 13.sp)
        }
    }
}

@Composable
private fun IntroSlideTwo() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "A pause with purpose.",
            color = TextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Seamless micro-learning in 3 simple steps:",
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        IntroStepItem(
            number = "1",
            emoji = "📱",
            title = "Open Social Media",
            description = "Launch Instagram or YouTube as you normally do."
        )

        Spacer(modifier = Modifier.height(12.dp))

        IntroStepItem(
            number = "2",
            emoji = "✨",
            title = "Spark Pop-Up Appears",
            description = "A bite-sized AI micro-lesson card displays on your screen."
        )

        Spacer(modifier = Modifier.height(12.dp))

        IntroStepItem(
            number = "3",
            emoji = "🧠",
            title = "Learn & Earn XP",
            description = "Absorb the 30-sec fact, build your Mind Palace, or skip to your app."
        )
    }
}

@Composable
private fun IntroStepItem(
    number: String,
    emoji: String,
    title: String,
    description: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SparkCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                FeatureGlyph(emoji, size = 22.dp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun IntroBadge(emoji: String, text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, SparkCyan.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FeatureGlyph(emoji, size = 12.dp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PermissionSetupSlide(
    usageGranted: Boolean,
    overlayGranted: Boolean,
    batteryGranted: Boolean,
    glowAlpha: Float,
    onGrantUsage: () -> Unit,
    onGrantOverlay: () -> Unit,
    onGrantBattery: () -> Unit
) {
    val scrollState = rememberScrollState()
    val currentStep = when {
        !usageGranted -> 1
        !overlayGranted -> 2
        !batteryGranted -> 3
        else -> 4
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FeatureGlyph("🛡️", size = 40.dp)

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Activate Spark Guard",
            color = TextPrimary,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Enable 3 permissions so Spark can protect your focus continuously:",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        PermissionCard(
            stepNumber = 1,
            title = "1. Usage Access",
            description = "Detects when Instagram, TikTok, or YouTube opens.",
            isGranted = usageGranted,
            isActive = currentStep == 1,
            glowAlpha = glowAlpha,
            onGrant = onGrantUsage
        )

        Spacer(modifier = Modifier.height(14.dp))

        PermissionCard(
            stepNumber = 2,
            title = "2. Display Over Apps",
            description = "Pops up learning micro-lessons over social media.",
            isGranted = overlayGranted,
            isActive = currentStep == 2,
            glowAlpha = glowAlpha,
            onGrant = onGrantOverlay
        )

        Spacer(modifier = Modifier.height(14.dp))

        PermissionCard(
            stepNumber = 3,
            title = "3. Unrestricted Background Run",
            description = "Keeps Spark Guard running permanently without Android battery kills.",
            isGranted = batteryGranted,
            isActive = currentStep == 3,
            glowAlpha = glowAlpha,
            onGrant = onGrantBattery
        )
    }
}

@Composable
fun PermissionCard(
    stepNumber: Int,
    title: String,
    description: String,
    isGranted: Boolean,
    isActive: Boolean,
    glowAlpha: Float,
    onGrant: () -> Unit
) {
    val borderColor = when {
        isGranted -> SparkCyan.copy(alpha = 0.18f)
        isActive -> SparkCyan.copy(alpha = glowAlpha)
        else -> SurfaceVariant.copy(alpha = 0.3f)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive || isGranted) CardBackground else CardBackground.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGranted) SparkCyan.copy(alpha = 0.2f)
                        else if (isActive) GradientPurple.copy(alpha = 0.2f)
                        else SurfaceVariant.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isGranted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Done",
                        tint = SparkCyan,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        color = if (isActive) TextPrimary else TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isGranted) SparkCyan else if (isActive) TextPrimary else TextSecondary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (!isGranted && isActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(listOf(SparkCyan, SparkCyan))
                        )
                        .softPress { onGrant() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text("Grant", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else if (isGranted) {
                FeatureGlyph("✅", size = 18.dp)
            }
        }
    }
}
