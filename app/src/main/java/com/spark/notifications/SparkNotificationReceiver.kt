package com.spark.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver triggered by AlarmManager to fire daily reminders.
 */
class SparkNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        SparkNotificationManager.showDailyReminderNotification(context)
    }
}
