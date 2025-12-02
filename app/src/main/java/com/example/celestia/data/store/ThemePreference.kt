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

/**
 * Extension property providing a DataStore instance for theme
 * and user preference storage.
 */
val Context.themeDataStore by preferencesDataStore(name = "theme_prefs")

/**
 * Defines strongly-typed preference keys used throughout the app
 * for theme settings, time format, location usage, and alerts.
 */
object ThemeKeys {
    val DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    val TIME_FORMAT_24H = booleanPreferencesKey("use_24_hour_clock")
    val REFRESH_ON_LAUNCH = booleanPreferencesKey("refresh_on_launch")
    val USE_DEVICE_LOCATION = booleanPreferencesKey("use_device_location")

    // Notification keys
    val KP_ALERTS_ENABLED = booleanPreferencesKey("kp_alerts_enabled")
    val LAST_ALERTED_KP = floatPreferencesKey("last_alerted_kp")
    val ISS_ALERTS_ENABLED = booleanPreferencesKey("iss_alerts_enabled")
}

/**
 * Manager responsible for reading and writing user preferences using DataStore.
 *
 * This includes:
 * - Dark mode state
 * - Time format selection
 * - Auto-refresh behavior
 * - Device location usage
 * - Notification preferences (Kp alerts, ISS alerts)
 * - Text size adjustments
 *
 * Also provides a utility to clear all cached Room database data.
 */
class ThemeManager(private val context: Context) {

    // -------------------------------------------------------------------------
    // DARK MODE
    // -------------------------------------------------------------------------

    /** Flow emitting whether dark mode is enabled (default: true). */
    val darkModeFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.DARK_MODE] ?: true
        }

    /** Saves dark mode preference. */
    suspend fun setDarkMode(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.DARK_MODE] = enabled
        }
    }

    // -------------------------------------------------------------------------
    // FONT SIZE
    // -------------------------------------------------------------------------

    /** Flow emitting the selected text size (0=Small, 1=Medium, 2=Large). */
    val textSizeFlow: Flow<Int> =
        context.themeDataStore.data.map { prefs ->
            prefs[TEXT_SIZE_KEY] ?: 1
        }

    /** Saves the selected text size. */
    suspend fun setTextSize(size: Int) {
        context.themeDataStore.edit { prefs ->
            prefs[TEXT_SIZE_KEY] = size
        }
    }

    companion object {
        /** Preference key for storing the user's text size setting. */
        val TEXT_SIZE_KEY = intPreferencesKey("text_size")
    }

    // -------------------------------------------------------------------------
    // TIME FORMAT
    // -------------------------------------------------------------------------

    /** Flow emitting whether 24-hour time format is enabled (default: true). */
    val timeFormat24H: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.TIME_FORMAT_24H] ?: true
        }

    /** Saves the user's selected time format. */
    suspend fun setTimeFormat(use24h: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.TIME_FORMAT_24H] = use24h
        }
    }

    // -------------------------------------------------------------------------
    // REFRESH ON APP LAUNCH
    // -------------------------------------------------------------------------

    /** Flow emitting whether data should refresh automatically on app launch. */
    val refreshOnLaunchFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.REFRESH_ON_LAUNCH] ?: true
        }

    /** Saves auto-refresh preference. */
    suspend fun setRefreshOnLaunch(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.REFRESH_ON_LAUNCH] = enabled
        }
    }

    // -------------------------------------------------------------------------
    // DEVICE LOCATION USAGE
    // -------------------------------------------------------------------------

    /** Flow emitting whether the app should use device location (default: false). */
    val useDeviceLocationFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.USE_DEVICE_LOCATION] ?: false
        }

    /** Saves location usage preference. */
    suspend fun setUseDeviceLocation(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.USE_DEVICE_LOCATION] = enabled
        }
    }

    // -------------------------------------------------------------------------
    // NOTIFICATION PREFERENCES
    // -------------------------------------------------------------------------

    /** Flow emitting whether Kp alerts are enabled. */
    val kpAlertsEnabledFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.KP_ALERTS_ENABLED] ?: false
        }

    /** Saves whether Kp alerts are enabled. */
    suspend fun setKpAlertsEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.KP_ALERTS_ENABLED] = enabled
        }
    }

    /** Flow emitting the last Kp value the user was alerted about. */
    val lastAlertedKpFlow: Flow<Float> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.LAST_ALERTED_KP] ?: -1f
        }

    /** Saves the most recent Kp alert value. */
    suspend fun setLastAlertedKp(value: Float) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.LAST_ALERTED_KP] = value
        }
    }

    /** Flow emitting whether ISS alerts are enabled. */
    val issAlertsEnabledFlow: Flow<Boolean> =
        context.themeDataStore.data.map { prefs ->
            prefs[ThemeKeys.ISS_ALERTS_ENABLED] ?: false
        }

    /** Saves whether ISS alerts are enabled. */
    suspend fun setIssAlertsEnabled(enabled: Boolean) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKeys.ISS_ALERTS_ENABLED] = enabled
        }
    }

    // -------------------------------------------------------------------------
    // CLEAR CACHE (Room)
    // -------------------------------------------------------------------------

    /**
     * Clears all cached Room database content:
     * - Kp Index
     * - ISS readings
     * - Asteroid approaches
     * - Lunar phase data
     */
    suspend fun clearCache() {
        val dao = CelestiaDatabase.getInstance(context).celestiaDao()

        dao.clearKpReadings()
        dao.clearIssReadings()
        dao.clearAsteroids()
        dao.clearLunarPhase()

        Log.d("ThemeManager", "Cache cleared.")
    }
}