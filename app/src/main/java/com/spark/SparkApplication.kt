package com.spark

import android.app.Application
import com.spark.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SparkApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@SparkApplication)
            modules(appModule)
        }

        com.spark.notifications.SparkNotificationManager.createNotificationChannel(this)
        com.spark.notifications.SparkNotificationManager.scheduleDailyReminder(this)
    }
}
