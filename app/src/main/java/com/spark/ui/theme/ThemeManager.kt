package com.spark.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    var isLightMode by mutableStateOf(false)

    private const val PREFS_NAME = "spark_theme_prefs"
    private const val KEY_IS_LIGHT_MODE = "is_light_mode"

    fun toggleTheme(context: Context) {
        setTheme(context, !isLightMode)
    }

    fun setTheme(context: Context, light: Boolean) {
        isLightMode = light
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_IS_LIGHT_MODE, light)
            .apply()
    }

    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        isLightMode = prefs.getBoolean(KEY_IS_LIGHT_MODE, false)
    }
}
