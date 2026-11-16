package com.spark.ui.screens.prolab

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * SharedPreferences-backed persistence for Pro Lab state.
 * Stores: Pro unlock status, coupon code, saved/pinned topics, and active topic preference.
 */
object ProLabPreferences {

    private const val PREFS_NAME = "spark_pro_lab_prefs"
    private const val KEY_IS_PRO = "is_pro_user"
    private const val KEY_COUPON_CODE = "active_coupon_code"
    private const val KEY_SAVED_TOPICS = "saved_topics"
    private const val KEY_ACTIVE_TOPIC = "active_topic_preference"

    // Valid coupon codes that unlock Pro
    val VALID_COUPON_CODES = listOf(
        "SPARK50", "SPARKFREE", "ADITYA", "SPARKPRO", "SPARK100", "PRO", "FREE", "VIP"
    )

    private fun prefs(context: Context): SharedPreferences {
        return com.spark.data.local.AccountSessionManager.preferences(context, PREFS_NAME)
    }

    // ── Pro Status ──────────────────────────────────────────

    fun isProUser(context: Context): Boolean {
        val pro = prefs(context).getBoolean(KEY_IS_PRO, false)
        com.spark.ui.screens.main.GlobalUserState.isPro = pro
        return pro
    }

    fun setProUnlocked(context: Context, couponCode: String) {
        val cleanCode = couponCode.trim().uppercase()
        prefs(context).edit()
            .putBoolean(KEY_IS_PRO, true)
            .putString(KEY_COUPON_CODE, cleanCode)
            .apply()
        com.spark.ui.screens.main.GlobalUserState.isPro = true
    }

    fun setBackendProStatus(context: Context, pro: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_PRO, pro).apply()
        com.spark.ui.screens.main.GlobalUserState.isPro = pro
    }

    fun getActiveCouponCode(context: Context): String? {
        return prefs(context).getString(KEY_COUPON_CODE, null)
    }

    fun isValidCoupon(code: String): Boolean {
        val clean = code.trim().uppercase()
        return VALID_COUPON_CODES.any { it.equals(clean, ignoreCase = true) }
    }

    fun revokeProAccess(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_IS_PRO, false)
            .remove(KEY_COUPON_CODE)
            .apply()
        com.spark.ui.screens.main.GlobalUserState.isPro = false
    }

    // ── Active Topic Preference ─────────────────────────────

    fun setActiveTopicPreference(context: Context, topic: String?) {
        if (topic.isNullOrBlank()) {
            prefs(context).edit().remove(KEY_ACTIVE_TOPIC).apply()
        } else {
            prefs(context).edit().putString(KEY_ACTIVE_TOPIC, topic).apply()
        }
    }

    fun getActiveTopicPreference(context: Context): String? {
        val direct = prefs(context).getString(KEY_ACTIVE_TOPIC, null)
        if (!direct.isNullOrBlank()) return direct
        val saved = getSavedTopics(context)
        return saved.firstOrNull()
    }

    // ── Saved / Pinned Topics ───────────────────────────────

    fun getSavedTopics(context: Context): Set<String> {
        val json = prefs(context).getString(KEY_SAVED_TOPICS, null) ?: return emptySet()
        return try {
            val type = object : TypeToken<Set<String>>() {}.type
            Gson().fromJson(json, type)
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun saveTopic(context: Context, topic: String) {
        val current = getSavedTopics(context).toMutableSet()
        current.add(topic)
        prefs(context).edit()
            .putString(KEY_SAVED_TOPICS, Gson().toJson(current))
            .apply()
        setActiveTopicPreference(context, topic)
    }

    fun removeTopic(context: Context, topic: String) {
        val current = getSavedTopics(context).toMutableSet()
        current.remove(topic)
        prefs(context).edit()
            .putString(KEY_SAVED_TOPICS, Gson().toJson(current))
            .apply()
        if (getActiveTopicPreference(context) == topic) {
            setActiveTopicPreference(context, null)
        }
    }

    fun isTopicSaved(context: Context, topic: String): Boolean {
        return getSavedTopics(context).contains(topic)
    }
}
