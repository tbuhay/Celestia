package com.example.celestia.data.store

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.celestia.data.db.CelestiaDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

object ThemeKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    val TIME_FORMAT_24H = booleanPreferencesKey("use_24_hour_clock")
    val REFRESH_ON_LAUNCH = booleanPreferencesKey("refresh_on_launch")
    val USE_DEVICE_LOCATION = booleanPreferencesKey("use_device_location")

    // NEW NOTIFICATION KEYS
    val KP_ALERTS_ENABLED = booleanPreferencesKey("kp_alerts_enabled")
    val LAST_ALERTED_KP = floatPreferencesKey("last_alerted_kp")
    val ISS_ALERTS_ENABLED = booleanPreferencesKey("iss_alerts_enabled")
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
    // FONT ADJUSTMENTS
    //-----------------------------------------

    val textSizeFlow: Flow<Int> =
        context.themeDataStore.data.map { prefs ->
            prefs[TEXT_SIZE_KEY] ?: 1  // 0 = Small, 1 = Medium, 2 = Large
        }

    suspend fun setTextSize(size: Int) {
        context.themeDataStore.edit { prefs ->
            prefs[TEXT_SIZE_KEY] = size
        }
    }

    companion object {
        val TEXT_SIZE_KEY = intPreferencesKey("text_size")
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
    // REFRESH ON APP LAUNCH
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

    //-----------------------------------------
    // DEVICE LOCATION
    //-----------------------------------------
    val useDeviceLocationFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.USE_DEVICE_LOCATION] ?: false
        }

    suspend fun setUseDeviceLocation(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.USE_DEVICE_LOCATION] = enabled
        }
    }

    //-----------------------------------------
    // NOTIFICATION PREFERENCES
    //-----------------------------------------
    val kpAlertsEnabledFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.KP_ALERTS_ENABLED] ?: false
        }

    suspend fun setKpAlertsEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.KP_ALERTS_ENABLED] = enabled
        }
    }

    // LAST KP VALUE ALERTED
    val lastAlertedKpFlow: Flow<Float> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.LAST_ALERTED_KP] ?: -1f
        }

    suspend fun setLastAlertedKp(value: Float) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.LAST_ALERTED_KP] = value
        }
    }

    val issAlertsEnabledFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.ISS_ALERTS_ENABLED] ?: false
        }

    suspend fun setIssAlertsEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.ISS_ALERTS_ENABLED] = enabled
        }
    }

    suspend fun clearCache() {
        val dao = CelestiaDatabase.getInstance(context).celestiaDao()

        dao.clearKpReadings()
        dao.clearIssReadings()
        dao.clearAsteroids()
        dao.clearLunarPhase()

        Log.d("ThemeManager", "Cache cleared.")
    }
}
