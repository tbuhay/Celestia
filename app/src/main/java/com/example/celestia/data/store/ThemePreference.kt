package com.example.celestia.data.store

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemeKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
}

class ThemeManager(private val context: Context) {

    val darkModeFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.DARK_MODE] ?: true  // default = DARK
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.DARK_MODE] = enabled
        }
    }
}
