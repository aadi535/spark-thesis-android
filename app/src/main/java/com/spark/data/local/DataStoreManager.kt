package com.spark.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "spark_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val ACCOUNT_KEY = stringPreferencesKey("account_key")
        val JWT_TOKEN = stringPreferencesKey("jwt_token")
    }

    val sessionFlow: Flow<Pair<String?, String?>> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN] to preferences[ACCOUNT_KEY]
    }

    suspend fun saveSession(token: String?, accountKey: String) {
        context.dataStore.edit { preferences ->
            if (token == null) preferences.remove(JWT_TOKEN) else preferences[JWT_TOKEN] = token
            preferences[ACCOUNT_KEY] = accountKey
        }
    }

    val jwtTokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[JWT_TOKEN]
    }

    suspend fun saveJwtToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
        }
    }

    suspend fun clearJwtToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(JWT_TOKEN)
            preferences.remove(ACCOUNT_KEY)
        }
    }
}
