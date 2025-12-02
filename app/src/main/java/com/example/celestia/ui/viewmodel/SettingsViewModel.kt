package com.example.celestia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.celestia.data.store.ThemeManager
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing **all user-configurable settings** in Celestia.
 *
 * Settings include:
 * - Dark mode (theme)
 * - Text scaling (accessibility)
 * - Time format (12h / 24h)
 * - Auto-refresh on app launch
 * - Device location usage (for moon phase)
 * - Notification preferences (Kp alerts, ISS alerts)
 *
 * All values are persisted using [ThemeManager], which wraps Jetpack **DataStore**.
 *
 * This ViewModel exposes:
 * - Reactive **LiveData** streams for the UI
 * - Write functions that update DataStore asynchronously via `viewModelScope`
 *
 * Because it extends [AndroidViewModel], it has access to the Application context
 * (required for the DataStore instance).
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    /** The DataStore-backed manager handling persistence for all user settings. */
    private val themeManager = ThemeManager(application)

    // ---------------------------------------------------------
    // DARK MODE
    // ---------------------------------------------------------

    /** Whether dark mode is enabled. Reflects live updates from DataStore. */
    val darkModeEnabled: LiveData<Boolean> =
        themeManager.darkModeFlow.asLiveData()

    /** Updates the dark mode preference. */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }

    // ---------------------------------------------------------
    // TEXT SIZE (ACCESSIBILITY)
    // ---------------------------------------------------------

    /**
     * Text size index:
     * ```
     * 0 = Small
     * 1 = Medium (default)
     * 2 = Large
     * ```
     * Used by the theme to scale typography globally.
     */
    val textSize: LiveData<Int> =
        themeManager.textSizeFlow.asLiveData()

    /** Sets the preferred global text size. */
    fun setTextSize(size: Int) {
        viewModelScope.launch {
            themeManager.setTextSize(size)
        }
    }

    // ---------------------------------------------------------
    // TIME FORMAT (12h vs 24h)
    // ---------------------------------------------------------

    /** Whether the user prefers 24-hour clock formatting. */
    val timeFormat24h: LiveData<Boolean> =
        themeManager.timeFormat24H.asLiveData()

    /** Updates the time format preference. */
    fun setTimeFormat(use24h: Boolean) {
        viewModelScope.launch {
            themeManager.setTimeFormat(use24h)
        }
    }

    // ---------------------------------------------------------
    // REFRESH ON APP LAUNCH
    // ---------------------------------------------------------

    /** If true, the app triggers a data refresh automatically on startup. */
    val refreshOnLaunchEnabled: LiveData<Boolean> =
        themeManager.refreshOnLaunchFlow.asLiveData()

    /** Sets the auto-refresh-on-launch preference. */
    fun setRefreshOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setRefreshOnLaunch(enabled)
        }
    }

    // ---------------------------------------------------------
    // DEVICE LOCATION
    // ---------------------------------------------------------

    /** Whether device location should be used for lunar phase calculations. */
    val deviceLocationEnabled: LiveData<Boolean> =
        themeManager.useDeviceLocationFlow.asLiveData()

    /** Enables or disables device-location usage. */
    fun setUseDeviceLocation(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setUseDeviceLocation(enabled)
        }
    }

    // ---------------------------------------------------------
    // NOTIFICATION PREFERENCES
    // ---------------------------------------------------------

    // ------------------ Kp Index Alerts ------------------

    /** Whether geomagnetic Kp alerts are enabled. */
    val kpAlertsEnabled: LiveData<Boolean> =
        themeManager.kpAlertsEnabledFlow.asLiveData()

    /** Enables/disables Kp Index notifications. */
    fun setKpAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setKpAlertsEnabled(enabled)
        }
    }

    // ------------------ ISS Alerts ------------------

    val issProximityEnabled: LiveData<Boolean> =
        themeManager.issProximityEnabledFlow.asLiveData()

    fun setIssProximityEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setIssProximityEnabled(enabled)
        }
    }

    // ---------------------------------------------------------
    // CLEAR ALL PERSISTED DATA
    // ---------------------------------------------------------

    /**
     * Clears all cached data from:
     * - Kp readings
     * - Lunar data
     * - ISS data
     * - Asteroid data
     *
     * This does **not** clear authentication state.
     *
     * @param onFinished callback triggered after clearing completes.
     */
    fun clearCache(onFinished: () -> Unit) {
        viewModelScope.launch {
            themeManager.clearCache()
            onFinished()
        }
    }
}
