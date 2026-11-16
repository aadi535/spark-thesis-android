package com.spark.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.MessageDigest

/** Selects local ownership only. Legacy unscoped files are deliberately never read. */
object AccountSessionManager {
    const val GUEST = "guest"
    var accountKey: String? by mutableStateOf(null)
        private set
    @Volatile var generation: Long = 0
        private set

    fun emailKey(email: String): String {
        require(email.isNotBlank())
        return "email:${email.trim()}"
    }

    fun select(key: String?) {
        generation++
        accountKey = key
    }

    fun preferences(context: Context, name: String, owner: String = accountKey ?: "signed-out"): SharedPreferences {
        val suffix = MessageDigest.getInstance("SHA-256").digest(owner.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return context.getSharedPreferences("${name}_account_v1_$suffix", Context.MODE_PRIVATE)
    }

    fun clearAccount(context: Context, owner: String) {
        listOf("spark_user_progress", "spark_mind_palace", "spark_pro_lab_prefs",
            "spark_ai_seen", "spark_interceptions").forEach { name ->
            preferences(context, name, owner).edit().clear().apply()
        }
    }
}
