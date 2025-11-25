package com.example.celestia.ui.viewmodel

import android.app.Application
import androidx.datastore.dataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.celestia.data.store.ThemeManager
import kotlinx.coroutines.launch

/**
 * ViewModel for managing user settings such as dark mode,
 * time format, auto-refresh, and device location usage.
 *
 * All settings are persisted using ThemeManager (DataStore).
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themeManager = ThemeManager(application)

    // ---------------------------------------------------------
    // DARK MODE
    // ---------------------------------------------------------
    val darkModeEnabled: LiveData<Boolean> =
        themeManager.darkModeFlow.asLiveData()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }

    // ---------------------------------------------------------
    // TIME FORMAT (12h vs 24h)
    // ---------------------------------------------------------
    val timeFormat24h: LiveData<Boolean> =
        themeManager.timeFormat24H.asLiveData()

    fun setTimeFormat(use24h: Boolean) {
        viewModelScope.launch {
            themeManager.setTimeFormat(use24h)
        }
    }

    // ---------------------------------------------------------
    // REFRESH ON APP LAUNCH
    // ---------------------------------------------------------
    val refreshOnLaunchEnabled: LiveData<Boolean> =
        themeManager.refreshOnLaunchFlow.asLiveData()

    fun setRefreshOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setRefreshOnLaunch(enabled)
        }
    }

    // ---------------------------------------------------------
    // DEVICE LOCATION
    // ---------------------------------------------------------
    val deviceLocationEnabled: LiveData<Boolean> =
        themeManager.useDeviceLocationFlow.asLiveData()

    fun setUseDeviceLocation(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setUseDeviceLocation(enabled)
        }
    }

    // ---------------------------------------------------------
    // NOTIFICATION PREFERENCES
    // ---------------------------------------------------------

    // KP ALERTS
    val kpAlertsEnabled: LiveData<Boolean> =
        themeManager.kpAlertsEnabledFlow.asLiveData()

    fun setKpAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setKpAlertsEnabled(enabled)
        }
    }

    // ISS ALERTS
    val issAlertsEnabled: LiveData<Boolean> =
        themeManager.issAlertsEnabledFlow.asLiveData()

    fun setIssAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setIssAlertsEnabled(enabled)
        }
    }

}
