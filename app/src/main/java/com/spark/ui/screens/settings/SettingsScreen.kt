package com.spark.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.spark.ui.screens.prolab.ProLabPreferences
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.interceptor.AppBlockerManager
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.screens.profile.EditProfileModal
import com.spark.ui.screens.profile.RankTiers
import com.spark.ui.theme.*
import com.spark.ui.theme.ThemeManager

@Composable
fun SettingsScreen(
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onNavigateToProLab: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()

    // Dialog states
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf(GlobalUserState.userName) }

    // Toggle states
    var guardActive by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(true) }
    var streakAlert by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 100.dp)
        ) {
            ScreenHeading("Your space", "Settings", "Your account, preferences and daily rhythm.")
            // Account Profile Section
            SettingsSectionHeader(title = "Account")

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentXp = GlobalUserState.xp
                val currentLevel = (currentXp / 500) + 1
                val currentRank = RankTiers.findLast { currentXp >= it.requiredXp } ?: RankTiers[0]

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(SurfaceVariant, CardBackground)
                                    )
                                )
                                .border(0.7.dp, GlassEdge, CircleShape)
                                .softPress { showEditProfileDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = GlobalUserState.userAvatar,
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = GlobalUserState.userName,
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Profile",
                                    tint = SparkCyan,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .softPress { showEditProfileDialog = true }
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Level $currentLevel · ${currentRank.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Subscription Badge
                        val isPro = ProLabPreferences.isProUser(context)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isPro) SparkOrange.copy(alpha = 0.15f) else SparkCyan.copy(alpha = 0.15f))
                                .border(1.dp, if (isPro) SparkOrange.copy(alpha = 0.4f) else SparkCyan.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                .softPress { onNavigateToProLab() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isPro) "Pro" else "Essential",
                                color = if (isPro) SparkOrange else SparkCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // View Full Profile Action
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardBackground)
                            .softPress { onNavigateToProfile() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your progress",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Full Profile",
                                color = SparkCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("→", color = SparkCyan, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // APPEARANCE & THEME
            SettingsSectionHeader(title = "Appearance")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GlassEdge, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .softPress { ThemeManager.toggleTheme(context) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (ThemeManager.isLightMode) "◐" else "◑",
                            color = TextPrimary,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (ThemeManager.isLightMode) "Light Mode" else "Dark Mode",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (ThemeManager.isLightMode) "Bright surfaces and clear typography" else "Soft contrast, made for quiet moments",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Switch(
                        checked = ThemeManager.isLightMode,
                        onCheckedChange = { ThemeManager.setTheme(context, it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AppBackground,
                            checkedTrackColor = TextPrimary,
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = SurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SYSTEM PERMISSIONS & SHIELD SETUP CARD
            SettingsSectionHeader(title = "Permissions")
            Spacer(modifier = Modifier.height(8.dp))

            val notifPermLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            val storagePermLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { _ -> }

            val hasNotifPerm = if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true

            val hasStoragePerm = if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }

            val hasOverlayPerm = Settings.canDrawOverlays(context)
            val hasUsagePerm = com.spark.interceptor.AppBlockerManager.hasUsageStatsPermission(context)

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Notification Permission Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (Build.VERSION.SDK_INT >= 33 && !hasNotifPerm) {
                                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            FeatureGlyph("🔔", size = 18.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Notifications Permission", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Required for daily learning alerts & streak reminders", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 64.dp).clip(RoundedCornerShape(10.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (hasNotifPerm) "Ready" else "Allow",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Storage & Media Permission Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!hasStoragePerm) {
                                    if (Build.VERSION.SDK_INT >= 33) {
                                        storagePermLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
                                    } else {
                                        storagePermLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                                    }
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            FeatureGlyph("📁", size = 18.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Storage & Audio Access", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Required to play custom ringtone notification sound files", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 64.dp).clip(RoundedCornerShape(10.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (hasStoragePerm) "Ready" else "Allow",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Display Over Other Apps Overlay Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!hasOverlayPerm) {
                                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                                    context.startActivity(intent)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            FeatureGlyph("🛡️", size = 18.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Display Over Other Apps", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Required to launch Spark Interceptor over blocked apps", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 64.dp).clip(RoundedCornerShape(10.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (hasOverlayPerm) "Ready" else "Allow",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Usage Stats Permission Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!hasUsagePerm) {
                                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                    context.startActivity(intent)
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            FeatureGlyph("📊", size = 18.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Usage Access Service", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("Recognise when a protected app opens", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 64.dp).clip(RoundedCornerShape(10.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, Color.Transparent, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = if (hasUsagePerm) "Ready" else "Allow",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Guard & App Blocking Section
            SettingsSectionHeader(title = "SPARK GUARD & APPS")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Master Guard Switch
                    SettingsSwitchItem(
                        icon = Icons.Default.Shield,
                        title = "Spark Guard Active",
                        subtitle = if (AppBlockerManager.isGuardEnabled) "Intercepting social apps before scrolling" else "Guard is currently PAUSED",
                        checked = AppBlockerManager.isGuardEnabled,
                        onCheckedChange = { AppBlockerManager.isGuardEnabled = it }
                    )

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Breathing Wave Toggle
                    SettingsSwitchItem(
                        icon = Icons.Default.Security,
                        title = "Mindful Breathing Wave",
                        subtitle = if (AppBlockerManager.isBreathingEnabled) "Shows 5-second breath wave before spark prompt" else "Skipped — opens spark prompt directly",
                        checked = AppBlockerManager.isBreathingEnabled,
                        onCheckedChange = { AppBlockerManager.isBreathingEnabled = it }
                    )

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Spaced Revision Quests Toggle
                    SettingsSwitchItem(
                        icon = Icons.Default.Star,
                        title = "Spaced Revision Quests",
                        subtitle = if (AppBlockerManager.isSpacedRevisionEnabled) "Occasionally resurfaces past topics with [REVISION] badge" else "Strictly disabled — 100% brand-new sub-concepts every time",
                        checked = AppBlockerManager.isSpacedRevisionEnabled,
                        onCheckedChange = { AppBlockerManager.isSpacedRevisionEnabled = it }
                    )

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Blocked Apps Toggles
                    AppBlockerManager.blockedApps.forEachIndexed { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    when (app.displayName) {
                                        "Instagram" -> "📸"
                                        "TikTok" -> "🎵"
                                        "YouTube" -> "▶️"
                                        else -> "📱"
                                    },
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(app.displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text(app.packageName, color = TextSecondary, fontSize = 12.sp)
                                }
                            }

                            Switch(
                                checked = app.isEnabled && AppBlockerManager.isGuardEnabled,
                                onCheckedChange = { app.isEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AppBackground, checkedTrackColor = TextPrimary)
                            )
                        }

                        if (index < AppBlockerManager.blockedApps.size - 1) {
                            HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // BLOCKING STRICTNESS MODE SECTION
            SettingsSectionHeader(title = "BLOCKING STRICTNESS MODE")
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    com.spark.interceptor.BlockingMode.values().forEach { mode ->
                        val isSelected = AppBlockerManager.blockingMode == mode

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SparkCyan.copy(alpha = 0.12f) else Color.Transparent)
                                .border(1.dp, if (isSelected) SparkCyan else SurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .softPress { AppBlockerManager.blockingMode = mode }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { AppBlockerManager.blockingMode = mode },
                                colors = RadioButtonDefaults.colors(selectedColor = SparkCyan)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        when (mode) {
                                            com.spark.interceptor.BlockingMode.NORMAL -> "🟢 "
                                            com.spark.interceptor.BlockingMode.HIGH -> "🟠 "
                                            com.spark.interceptor.BlockingMode.EXTREME -> "🔴 "
                                        },
                                        fontSize = 12.sp
                                    )
                                    Text(mode.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(mode.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 15.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Ringtone Picker Launcher
            val ringtoneLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val uri: Uri? = result.data?.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    if (uri != null) {
                        com.spark.notifications.SparkNotificationManager.setCustomSoundUri(context, uri.toString())
                    }
                }
            }

            var showTimePickerModal by remember { mutableStateOf(false) }

            // Notifications Section
            SettingsSectionHeader(title = "NOTIFICATIONS & REMINDERS")
            Spacer(modifier = Modifier.height(8.dp))

            var isNotifEnabled by remember { mutableStateOf(com.spark.notifications.SparkNotificationManager.isNotificationEnabled(context)) }
            var currentHour by remember { mutableStateOf(com.spark.notifications.SparkNotificationManager.getScheduledHour(context)) }
            var currentMinute by remember { mutableStateOf(com.spark.notifications.SparkNotificationManager.getScheduledMinute(context)) }

            val formattedTime = remember(currentHour, currentMinute) {
                val amPm = if (currentHour >= 12) "PM" else "AM"
                val hour12 = when {
                    currentHour == 0 -> 12
                    currentHour > 12 -> currentHour - 12
                    else -> currentHour
                }
                String.format("%02d:%02d %s", hour12, currentMinute, amPm)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Daily Learning Reminder",
                        subtitle = "Remind me to complete daily brain sparks",
                        checked = isNotifEnabled,
                        onCheckedChange = {
                            isNotifEnabled = it
                            com.spark.notifications.SparkNotificationManager.setNotificationEnabled(context, it)
                        }
                    )

                    if (isNotifEnabled) {
                        HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Custom Premium Reminder Time Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .softPress { showTimePickerModal = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FeatureGlyph("⏰", size = 18.dp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Reminder Time", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("Tap to change daily notification schedule", color = TextSecondary, fontSize = 12.sp)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SparkCyan.copy(alpha = 0.15f))
                                    .border(1.dp, SparkCyan.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(formattedTime, color = SparkCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Hybrid Sound Picker Row with persistent Uri saving
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val currentUri = com.spark.notifications.SparkNotificationManager.getCustomSoundUri(context)
                                    val soundPickerIntent = Intent(android.media.RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                        putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TYPE, android.media.RingtoneManager.TYPE_NOTIFICATION)
                                        putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Reminder Sound")
                                        putExtra(android.media.RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                                    }
                                    ringtoneLauncher.launch(soundPickerIntent)
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FeatureGlyph("🎵", size = 18.dp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text("Notification Sound", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    Text("Tap to select custom notification sound", color = TextSecondary, fontSize = 12.sp)
                                }
                            }

                            Icon(Icons.Default.ChevronRight, contentDescription = "Select Sound", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // PREMIUM CUSTOM TIME PICKER MODAL
            if (showTimePickerModal) {
                var selectedHour24 by remember { mutableStateOf(currentHour) }
                var selectedMinute by remember { mutableStateOf(currentMinute) }

                AlertDialog(
                    onDismissRequest = { showTimePickerModal = false },
                    containerColor = CardBackground,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Daily reminder",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Choose when to receive your daily brain spark",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        ) {
                            // PRESET TIME CHIPS
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf(
                                    Pair("09:00 AM", Pair(9, 0)),
                                    Pair("02:00 PM", Pair(14, 0)),
                                    Pair("08:00 PM", Pair(20, 0)),
                                    Pair("09:30 PM", Pair(21, 30))
                                ).forEach { (label, time) ->
                                    val isSelected = selectedHour24 == time.first && selectedMinute == time.second
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) Color.White else SurfaceVariant)
                                            .clickable {
                                                selectedHour24 = time.first
                                                selectedMinute = time.second
                                            }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.Black else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // DIRECT MANUAL TEXT INPUT & STEPPER CONTROLLER
                            var isDirectInputMode by remember { mutableStateOf(false) }

                            if (!isDirectInputMode) {
                                val amPm = if (selectedHour24 >= 12) "PM" else "AM"
                                val displayHour = when {
                                    selectedHour24 == 0 -> 12
                                    selectedHour24 > 12 -> selectedHour24 - 12
                                    else -> selectedHour24
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // HOUR CONTROL (1-by-1 step)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        IconButton(onClick = {
                                            selectedHour24 = (selectedHour24 + 1) % 24
                                        }) {
                                            Text("▲", color = TextSecondary, fontSize = 16.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SurfaceVariant)
                                                .softPress { isDirectInputMode = true }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = String.format("%02d", displayHour),
                                                color = TextPrimary,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(onClick = {
                                            selectedHour24 = if (selectedHour24 == 0) 23 else selectedHour24 - 1
                                        }) {
                                            Text("▼", color = TextSecondary, fontSize = 16.sp)
                                        }
                                    }

                                    Text(" : ", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)

                                    // MINUTE CONTROL (1-by-1 step fix!)
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        IconButton(onClick = {
                                            selectedMinute = (selectedMinute + 1) % 60
                                        }) {
                                            Text("▲", color = TextSecondary, fontSize = 16.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(SurfaceVariant)
                                                .softPress { isDirectInputMode = true }
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = String.format("%02d", selectedMinute),
                                                color = TextPrimary,
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(onClick = {
                                            selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                                        }) {
                                            Text("▼", color = TextSecondary, fontSize = 16.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // AM/PM TOGGLE
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (amPm == "AM") Color.White else SurfaceVariant)
                                                .clickable {
                                                    if (selectedHour24 >= 12) selectedHour24 -= 12
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("AM", color = if (amPm == "AM") Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (amPm == "PM") Color.White else SurfaceVariant)
                                                .clickable {
                                                    if (selectedHour24 < 12) selectedHour24 += 12
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("PM", color = if (amPm == "PM") Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap a number to enter a time",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            } else {
                                // DIRECT KEYBOARD TEXT FIELD EDIT MODE
                                var hourInput by remember {
                                    val displayH = when {
                                        selectedHour24 == 0 -> 12
                                        selectedHour24 > 12 -> selectedHour24 - 12
                                        else -> selectedHour24
                                    }
                                    mutableStateOf(String.format("%02d", displayH))
                                }
                                var minuteInput by remember { mutableStateOf(String.format("%02d", selectedMinute)) }
                                var amPmInput by remember { mutableStateOf(if (selectedHour24 >= 12) "PM" else "AM") }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = hourInput,
                                        onValueChange = {
                                            val filtered = it.take(2).filter { char -> char.isDigit() }
                                            hourInput = filtered
                                            val h = filtered.toIntOrNull()
                                            if (h != null && h in 1..12) {
                                                selectedHour24 = if (amPmInput == "PM") (if (h == 12) 12 else h + 12) else (if (h == 12) 0 else h)
                                            }
                                        },
                                        modifier = Modifier.width(68.dp),
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = SparkCyan,
                                            unfocusedBorderColor = SurfaceVariant
                                        )
                                    )

                                    Text(" : ", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)

                                    OutlinedTextField(
                                        value = minuteInput,
                                        onValueChange = {
                                            val filtered = it.take(2).filter { char -> char.isDigit() }
                                            minuteInput = filtered
                                            val m = filtered.toIntOrNull()
                                            if (m != null && m in 0..59) {
                                                selectedMinute = m
                                            }
                                        },
                                        modifier = Modifier.width(68.dp),
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = SparkCyan,
                                            unfocusedBorderColor = SurfaceVariant
                                        )
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // AM/PM SLIDE TOGGLE
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (amPmInput == "AM") Color.White else SurfaceVariant)
                                                .clickable {
                                                    amPmInput = "AM"
                                                    val h = hourInput.toIntOrNull() ?: 9
                                                    selectedHour24 = if (h == 12) 0 else h
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("AM", color = if (amPmInput == "AM") Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (amPmInput == "PM") Color.White else SurfaceVariant)
                                                .clickable {
                                                    amPmInput = "PM"
                                                    val h = hourInput.toIntOrNull() ?: 9
                                                    selectedHour24 = if (h == 12) 12 else h + 12
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("PM", color = if (amPmInput == "PM") Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Switch to stepper controls",
                                    color = SparkCyan,
                                    fontSize = 12.sp,
                                    modifier = Modifier.softPress { isDirectInputMode = false }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .clickable {
                                    currentHour = selectedHour24
                                    currentMinute = selectedMinute
                                    com.spark.notifications.SparkNotificationManager.setScheduledTime(context, selectedHour24, selectedMinute)
                                    showTimePickerModal = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Save Reminder Time", color = OnAccent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePickerModal = false }) {
                            Text("Cancel", color = TextSecondary)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Danger Zone
            SettingsSectionHeader(title = "ACCOUNT MANAGEMENT", color = Color(0xFFFF5555))
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // Logout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .softPress { onLogout() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Log Out", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }

                    HorizontalDivider(color = SurfaceVariant.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Delete Account
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .softPress { showDeleteConfirmDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete Account", tint = Color(0xFFFF5555), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text("Delete Account", color = Color(0xFFFF5555), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Wipe all XP, streaks & Mind Palace data", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Modal
    if (showEditProfileDialog) {
        EditProfileModal(
            currentName = GlobalUserState.userName,
            currentAvatar = GlobalUserState.userAvatar,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newAvatar ->
                GlobalUserState.updateProfile(context, newName, newAvatar)
                showEditProfileDialog = false
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("⚠️ Delete Account?", color = Color(0xFFFF5555), fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "Are you sure? This will permanently delete your profile, wipe all your XP, reset your streaks, and clear your Mind Palace network.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555))
                ) {
                    Text("Delete Permanently", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardBackground
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, color: Color = TextSecondary) {
    Text(
        text = title,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceVariant.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AppBackground, checkedTrackColor = TextPrimary,
                uncheckedThumbColor = TextSecondary, uncheckedTrackColor = SurfaceVariant,
                uncheckedBorderColor = Color.Transparent))
    }
}
