package com.spark.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.spark.MainActivity
import com.spark.R
import java.util.Calendar

/**
 * Handles notification channels, scheduling daily reminders, and firing focus shield alerts.
 * Compatible from Android 9 (API 28) & Android 10 (API 29) to Android 14 (API 34).
 */
object SparkNotificationManager {

    const val CHANNEL_ID = "spark_daily_reminders"
    private const val NOTIF_ID_REMINDER = 1001
    private const val PREFS_NAME = "spark_notif_prefs"
    private const val KEY_HOUR = "reminder_hour"
    private const val KEY_MINUTE = "reminder_minute"
    private const val KEY_NOTIF_ENABLED = "is_notif_enabled"

    private const val KEY_CUSTOM_SOUND_URI = "custom_sound_uri"

    fun getCustomSoundUri(context: Context): android.net.Uri {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val uriStr = prefs.getString(KEY_CUSTOM_SOUND_URI, null)
        return if (!uriStr.isNullOrEmpty()) {
            try {
                android.net.Uri.parse(uriStr)
            } catch (e: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
        } else {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
    }

    fun setCustomSoundUri(context: Context, uriStr: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_SOUND_URI, uriStr).apply()
        createNotificationChannel(context)
    }

    fun getChannelId(context: Context): String {
        val soundUri = getCustomSoundUri(context)
        return "spark_reminders_" + Math.abs(soundUri.toString().hashCode())
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = getCustomSoundUri(context)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Delete old static default channel if it exists
            try {
                manager.deleteNotificationChannel("spark_daily_reminders")
            } catch (e: Exception) {}

            val dynamicChannelId = getChannelId(context)

            val channel = NotificationChannel(
                dynamicChannelId,
                "Daily Learning Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily notifications reminding you to complete your 30-sec brain sparks"
                setSound(soundUri, audioAttributes)
                enableVibration(true)
            }

            manager.createNotificationChannel(channel)
        }
    }

    fun isNotificationEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NOTIF_ENABLED, true)
    }

    fun setNotificationEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply()
        if (enabled) {
            scheduleDailyReminder(context)
        } else {
            cancelDailyReminder(context)
        }
    }

    fun getScheduledHour(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_HOUR, 9) // Default 9 AM
    }

    fun getScheduledMinute(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_MINUTE, 0) // Default :00
    }

    fun setScheduledTime(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_HOUR, hour).putInt(KEY_MINUTE, minute).apply()
        scheduleDailyReminder(context)
    }

    fun scheduleDailyReminder(context: Context) {
        if (!isNotificationEnabled(context)) return

        val hour = getScheduledHour(context)
        val minute = getScheduledMinute(context)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, SparkNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIF_ID_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }

    fun cancelDailyReminder(context: Context) {
        val intent = Intent(context, SparkNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIF_ID_REMINDER,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    fun showDailyReminderNotification(context: Context) {
        if (!isNotificationEnabled(context)) return

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = getCustomSoundUri(context)
        val titleText = "⚡ Time for Today's Brain Spark!"
        val bodyText = "Keep your learning momentum alive! Take 30 seconds right now to complete your daily interactive quest and expand your mind."

        val dynamicChannelId = getChannelId(context)

        val builder = NotificationCompat.Builder(context, dynamicChannelId)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(titleText)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_REMINDER, builder.build())

        // Re-schedule for next day
        scheduleDailyReminder(context)
    }
}
