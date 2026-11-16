package com.spark.interceptor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.spark.ui.screens.main.GlobalUserState

enum class BlockingMode(val title: String, val description: String) {
    NORMAL("Normal Mode", "1-time 5-min emergency pass allowed per day"),
    HIGH("High Mode", "No passes allowed. Learn spark or exit to home screen"),
    EXTREME("Extreme Lock", "Strict focus. Apps blocked until 3 daily sparks are completed")
}

/**
 * Manages the list of apps that Spark will intercept.
 * Free users get 3 fixed apps. Pro users can add any app.
 */
object AppBlockerManager {

    // Master Switch to turn Guard ON / OFF
    var isGuardEnabled by mutableStateOf(true)

    // Mindful Breathing Wave Animation Toggle
    var isBreathingEnabled by mutableStateOf(true)

    // Spaced Revision Quests Toggle (Default: false = 100% unique sub-concepts every time)
    var isSpacedRevisionEnabled by mutableStateOf(false)

    // Current Blocking Strictness Mode
    var blockingMode by mutableStateOf(BlockingMode.NORMAL)

    // Track if the 1-time 5-minute normal pass has been used today
    var hasUsedNormalPassToday by mutableStateOf(false)

    // Default blocked apps (free tier)
    private val defaultBlockedApps = listOf(
        BlockedApp("com.instagram.android", "Instagram"),
        BlockedApp("com.zhiliaoapp.musically", "TikTok"),
        BlockedApp("com.google.android.youtube", "YouTube")
    )

    // The active list of blocked apps
    val blockedApps = mutableStateListOf<BlockedApp>().apply {
        addAll(defaultBlockedApps)
    }

    fun setAppEnabled(index: Int, enabled: Boolean) {
        if (index in blockedApps.indices) {
            val app = blockedApps[index]
            blockedApps[index] = app.copy(isEnabled = enabled)
        }
    }

    fun setAppEnabledByPackage(packageName: String, enabled: Boolean) {
        val index = blockedApps.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            setAppEnabled(index, enabled)
        }
    }

    // Track which apps have already been intercepted today
    private val interceptedToday = mutableSetOf<String>()

    // Temporary bypass map (packageName -> expireTimestampMs)
    private val bypassUntilMap = mutableMapOf<String, Long>()

    // How many intercepts the user has completed today (directly linked to GlobalUserState.completedTasks)
    val dailyInterceptCount: Int
        get() = GlobalUserState.completedTasks

    // Unlimited spark generation for testing & Pro users
    val dailyLimit = 999

    fun hasUsageStatsPermission(context: android.content.Context): Boolean {
        val appOps = context.getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    fun isBlocked(packageName: String): Boolean {
        if (!isGuardEnabled) return false
        if (isBypassed(packageName)) return false
        return blockedApps.any { it.packageName == packageName && it.isEnabled }
    }

    fun hasReachedDailyLimit(): Boolean {
        return false
    }

    fun recordIntercept(packageName: String) {
        interceptedToday.add(packageName)
    }

    fun grantNormalPass(packageName: String) {
        if (!hasUsedNormalPassToday) {
            hasUsedNormalPassToday = true
            bypassUntilMap[packageName] = System.currentTimeMillis() + (5 * 60 * 1000L)
        }
    }

    fun isBypassed(packageName: String): Boolean {
        val expireTime = bypassUntilMap[packageName] ?: return false
        if (System.currentTimeMillis() < expireTime) {
            return true
        }
        bypassUntilMap.remove(packageName)
        return false
    }

    fun resetDaily() {
        interceptedToday.clear()
        bypassUntilMap.clear()
        hasUsedNormalPassToday = false
    }
}

data class BlockedApp(
    val packageName: String,
    val displayName: String,
    var isEnabled: Boolean = true
)
