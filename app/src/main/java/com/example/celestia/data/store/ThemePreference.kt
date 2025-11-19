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
    val TIME_FORMAT_24H = booleanPreferencesKey("use_24_hour_clock")

    val REFRESH_ON_LAUNCH = booleanPreferencesKey("refresh_on_launch")

    val USE_DEVICE_LOCATION = booleanPreferencesKey("use_device_location")
}

class ThemeManager(private val context: Context) {

    //-----------------------------------------
    // DARK MODE
    //-----------------------------------------
    val darkModeFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.DARK_MODE] ?: true
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.DARK_MODE] = enabled
        }
    }

    //-----------------------------------------
    // TIME FORMAT
    //-----------------------------------------
    val timeFormat24H: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.TIME_FORMAT_24H] ?: true
        }

    suspend fun setTimeFormat(use24h: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.TIME_FORMAT_24H] = use24h
        }
    }

    //-----------------------------------------
    // NEW: REFRESH ON APP LAUNCH
    //-----------------------------------------
    val refreshOnLaunchFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.REFRESH_ON_LAUNCH] ?: true
        }

    suspend fun setRefreshOnLaunch(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.REFRESH_ON_LAUNCH] = enabled
        }
    }

    val useDeviceLocationFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.USE_DEVICE_LOCATION] ?: false
        }

    suspend fun setUseDeviceLocation(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.USE_DEVICE_LOCATION] = enabled
        }
    }
}
