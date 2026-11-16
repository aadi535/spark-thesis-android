package com.spark.interceptor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.spark.R
import java.util.Timer
import java.util.TimerTask

/**
 * A Foreground Service that polls UsageStatsManager every 100ms
 * to detect when the user opens a blocked app (Instagram, TikTok, YouTube).
 * Launches SparkOverlayActivity INSTANTLY upon detection.
 */
class SparkInterceptorService : Service() {

    companion object {
        private const val TAG = "SparkInterceptor"
        private const val CHANNEL_ID = "spark_interceptor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL_MS = 100L // 100ms ultra-fast polling for 0.05s detection
    }

    private var pollingTimer: Timer? = null
    private var lastDetectedPackage: String? = null
    private var isOverlayShowing = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        val firstStart = pollingTimer == null
        val overlayDismissed = intent?.action == "OVERLAY_DISMISSED"
        if (overlayDismissed) {
            isOverlayShowing = false
            lastDetectedPackage = null
        }
        if (firstStart) {
            startPolling()
            Log.d(TAG, "SparkInterceptorService started")
        }
        if (firstStart || overlayDismissed) {
            InterceptorTaskCache.prefetch(applicationContext)
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        pollingTimer?.cancel()
        pollingTimer = null
        Log.d(TAG, "SparkInterceptorService stopped")
        super.onDestroy()
    }

    private fun startPolling() {
        pollingTimer = Timer()
        pollingTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                checkForegroundApp()
            }
        }, 0, POLL_INTERVAL_MS)
    }

    private fun checkForegroundApp() {
        if (!AppBlockerManager.isGuardEnabled) return

        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return

        val currentTime = System.currentTimeMillis()
        
        // Method 1: Query recent events in the last 1500ms
        val usageEvents = usageStatsManager.queryEvents(currentTime - 1500, currentTime)
        var foregroundPackage: String? = null
        val event = UsageEvents.Event()
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED || event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                foregroundPackage = event.packageName
            }
        }

        // Method 2: Fallback query for most recently used package if event log is buffering
        if (foregroundPackage == null) {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 5000, currentTime)
            foregroundPackage = stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        }

        if (foregroundPackage != null) {
            // Reset state when user is inside Spark app
            if (foregroundPackage == packageName) {
                if (lastDetectedPackage != packageName) {
                    lastDetectedPackage = packageName
                    isOverlayShowing = false
                }
                return
            }

            // Check if this foreground package is in our blocked list
            if (AppBlockerManager.isBlocked(foregroundPackage)) {
                if (foregroundPackage != lastDetectedPackage || !isOverlayShowing) {
                    lastDetectedPackage = foregroundPackage
                    val isLimitReached = AppBlockerManager.hasReachedDailyLimit()
                    Log.d(TAG, "Blocked app detected INSTANTLY: $foregroundPackage (isLimitReached=$isLimitReached)")
                    launchOverlay(foregroundPackage, isLimitReached)
                }
            } else {
                // User navigated away to Launcher, Settings, etc.
                lastDetectedPackage = foregroundPackage
                isOverlayShowing = false
            }
        }
    }

    private fun launchOverlay(blockedPackage: String, isLimitReached: Boolean) {
        isOverlayShowing = true
        AppBlockerManager.recordIntercept(blockedPackage)
        
        val intent = Intent(this, SparkOverlayActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("blocked_package", blockedPackage)
            putExtra("is_limit_reached", isLimitReached)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Spark Learning Guard",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Spark is active and guarding your focus"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Spark Focus Guard Active")
            .setContentText("Guarding your focus against social media doomscrolling")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
