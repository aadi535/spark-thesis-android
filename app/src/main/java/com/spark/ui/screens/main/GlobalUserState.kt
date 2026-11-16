package com.spark.ui.screens.main

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.spark.ui.screens.dashboard.GlobalSparkState
import com.spark.ui.screens.prolab.ProLabPreferences

object GlobalUserState {
    var isLoggedIn by mutableStateOf(false)
    var isGuest by mutableStateOf(false)
    var userEmail by mutableStateOf("")
    var userName by mutableStateOf("Explorer")
    var userAvatar by mutableStateOf("⚡")
    var leaderboard by mutableStateOf<com.spark.data.network.LeaderboardResponse?>(null)
    var leaderboardRequested by mutableStateOf(false)

    var xp by mutableStateOf(0)
    var streak by mutableStateOf(0)
    var completedTasks by mutableStateOf(0)
    var activeNeurons by mutableStateOf(0)
    var cognitiveDepth by mutableStateOf(0.0)
    var hasCompletedOnboarding by mutableStateOf(false)
    var isPro by mutableStateOf(false)

    fun updateProfile(context: Context, newName: String, newAvatar: String = userAvatar) {
        userName = newName.trim().replace("✏️", "").ifBlank { "Explorer" }
        userAvatar = newAvatar.ifBlank { "⚡" }
        saveProgress(context)
        org.koin.core.context.GlobalContext.get().get<com.spark.data.network.ProgressSync>().saveDisplayName(userName)
    }

    fun refreshProStatus(context: Context) {
        isPro = ProLabPreferences.isProUser(context)
    }

    private const val PREFS_NAME = "spark_user_progress"

    fun completeTask(taskXp: Int) {
        xp += taskXp
        completedTasks += 1
        activeNeurons += 1
        cognitiveDepth += (taskXp * 0.1)
        
        if (streak == 0) {
            streak = 1
        }
    }

    fun logout() {
        leaderboard = null
        leaderboardRequested = false
        isLoggedIn = false
        isGuest = false
        userEmail = ""
        userName = "Explorer"
        userAvatar = "⚡"
        xp = 0
        streak = 0
        completedTasks = 0
        activeNeurons = 0
        cognitiveDepth = 0.0
        hasCompletedOnboarding = false
        isPro = false
        MindPalaceData.resetActive()
        GlobalSparkState.completeAndClear()
        com.spark.interceptor.InterceptorTaskCache.clear()
        com.spark.interceptor.AppBlockerManager.resetDaily()
    }

    fun loadAccount(context: Context, key: String?) {
        com.spark.data.local.AccountSessionManager.select(key)
        logout()
        if (key == null) return
        loadProgress(context)
        MindPalaceData.loadFromLocal(context)
        isGuest = key == com.spark.data.local.AccountSessionManager.GUEST
        isLoggedIn = !isGuest
        userEmail = if (isGuest) "" else key.removePrefix("email:")
        if (isGuest && userName == "Explorer") userName = "Guest Explorer"
        refreshProStatus(context)
    }

    fun applyBackendProgress(context: Context, progress: com.spark.data.network.UserProgressDto) {
        userName = progress.name.ifBlank { "Explorer" }
        // Some completion paths save XP only locally. An empty backend total must not
        // erase the XP already restored from this account's own storage.
        if (progress.xp > 0 || xp == 0) {
            xp = progress.xp.coerceAtLeast(0)
        }
        streak = progress.streak
        ProLabPreferences.setBackendProStatus(context, progress.isPro)
        MindPalaceData.restoreBackendHistory(context, progress.completedTasks)
        saveProgress(context)
    }

    // Only call after successful backend deletion (or for a local-only guest).
    fun deleteAccount(context: Context, owner: String) {
        com.spark.data.local.AccountSessionManager.clearAccount(context, owner)
        loadAccount(context, null)
    }

    // ── SharedPreferences Persistence ──────────────────────────

    fun saveProgress(context: Context) {
        val prefs = com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME)
        prefs.edit()
            .putBoolean("isLoggedIn", isLoggedIn)
            .putBoolean("isGuest", isGuest)
            .putInt("xp", xp)
            .putInt("streak", streak)
            .putInt("completedTasks", completedTasks)
            .putInt("activeNeurons", activeNeurons)
            .putFloat("cognitiveDepth", cognitiveDepth.toFloat())
            .putString("userName", userName)
            .putString("userAvatar", userAvatar)
            .putString("userEmail", userEmail)
            .putBoolean("hasCompletedOnboarding", hasCompletedOnboarding)
            .apply()
    }

    fun loadProgress(context: Context) {
        val prefs = com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME)
        isLoggedIn = prefs.getBoolean("isLoggedIn", false)
        isGuest = prefs.getBoolean("isGuest", false)
        xp = prefs.getInt("xp", 0)
        streak = prefs.getInt("streak", 0)
        completedTasks = prefs.getInt("completedTasks", 0)
        activeNeurons = prefs.getInt("activeNeurons", 0)
        cognitiveDepth = prefs.getFloat("cognitiveDepth", 0f).toDouble()
        val rawName = prefs.getString("userName", "Explorer") ?: "Explorer"
        userName = rawName.replace("✏️", "").replace("(Dev)", "").trim().ifBlank { "Explorer" }
        userAvatar = prefs.getString("userAvatar", "⚡") ?: "⚡"
        userEmail = prefs.getString("userEmail", "") ?: ""
        hasCompletedOnboarding = prefs.getBoolean("hasCompletedOnboarding", false)
    }

    fun clearSavedProgress(context: Context) {
        com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME).edit().clear().apply()
    }
}
