package com.example.celestia.ui.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.celestia.data.store.ThemeManager
import kotlinx.coroutines.launch
import com.google.android.gms.location.LocationServices
import java.util.Locale

/**
 * **SettingsViewModel — central controller for all user preferences in Celestia.**
 *
 * This ViewModel exposes a structured, reactive interface for every configurable
 * setting the user can modify. All preferences are persisted using Jetpack
 * **DataStore**, wrapped internally by [ThemeManager] for clean separation of concerns.
 *
 * Since this extends [AndroidViewModel], the Application context is available
 * for:
 * - Geographic geocoding
 * - Accessing DataStore
 * - Accessing location services
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    /** Manager responsible for reading and writing all DataStore-backed settings. */
    private val themeManager = ThemeManager(application)

    /** Device location client (used only for validation helpers, if needed). */
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // ---------------------------------------------------------
    // DARK MODE
    // ---------------------------------------------------------

    /** Live preference indicating whether Dark Mode is enabled. */
    val darkModeEnabled: LiveData<Boolean> =
        themeManager.darkModeFlow.asLiveData()

    /** Updates Dark Mode preference in DataStore. */
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { themeManager.setDarkMode(enabled) }
    }

    // ---------------------------------------------------------
    // TEXT SIZE (ACCESSIBILITY)
    // ---------------------------------------------------------

    /**
     * Global text scaling index:
     *
     * ```
     * 0 = Small
     * 1 = Medium (default)
     * 2 = Large
     * ```
     *
     * This is consumed by the app theme to scale typography across all screens.
     */
    val textSize: LiveData<Int> =
        themeManager.textSizeFlow.asLiveData()

    /** Saves a new text size selection. */
    fun setTextSize(size: Int) {
        viewModelScope.launch { themeManager.setTextSize(size) }
    }

    // ---------------------------------------------------------
    // TIME FORMAT (12h vs 24h)
    // ---------------------------------------------------------

    /** LiveData tracking whether the app uses 24h clock formatting. */
    val timeFormat24h: LiveData<Boolean> =
        themeManager.timeFormat24H.asLiveData()

    /** Updates the preferred time format. */
    fun setTimeFormat(use24h: Boolean) {
        viewModelScope.launch { themeManager.setTimeFormat(use24h) }
    }

    // ---------------------------------------------------------
    // REFRESH ON APP LAUNCH
    // ---------------------------------------------------------

    /** Live flag indicating whether the app should auto-refresh on startup. */
    val refreshOnLaunchEnabled: LiveData<Boolean> =
        themeManager.refreshOnLaunchFlow.asLiveData()

    /** Enables or disables automatic refresh on app launch. */
    fun setRefreshOnLaunch(enabled: Boolean) {
        viewModelScope.launch { themeManager.setRefreshOnLaunch(enabled) }
    }

    // ---------------------------------------------------------
    // DEVICE LOCATION
    // ---------------------------------------------------------

    /** Whether the app should use device GPS for lunar phase calculations. */
    val deviceLocationEnabled: LiveData<Boolean> =
        themeManager.useDeviceLocationFlow.asLiveData()

    /** Updates the device-location preference. */
    fun setUseDeviceLocation(enabled: Boolean) {
        viewModelScope.launch { themeManager.setUseDeviceLocation(enabled) }
    }

    // ---------------------------------------------------------
    // HOME LOCATION (City / Region / Country / Coordinates)
    // ---------------------------------------------------------

    /** Stored home city string. */
    val homeCity: LiveData<String> = themeManager.homeCityFlow.asLiveData()

    /** Stored home region/province/state. */
    val homeRegion: LiveData<String> = themeManager.homeRegionFlow.asLiveData()

    /** Stored home country. */
    val homeCountry: LiveData<String> = themeManager.homeCountryFlow.asLiveData()

    /** Stored home latitude. */
    val homeLat: LiveData<Float> = themeManager.homeLatFlow.asLiveData()

    /** Stored home longitude. */
    val homeLon: LiveData<Float> = themeManager.homeLonFlow.asLiveData()

    /** Saves the home city string. */
    fun setHomeCity(city: String) {
        viewModelScope.launch { themeManager.setHomeCity(city.trim()) }
    }

    /** Saves the home region. */
    fun setHomeRegion(region: String) {
        viewModelScope.launch { themeManager.setHomeRegion(region.trim()) }
    }

    /** Saves the home country. */
    fun setHomeCountry(country: String) {
        viewModelScope.launch { themeManager.setHomeCountry(country.trim()) }
    }

    /** Saves the lat/lon coordinates for Home Location. */
    fun setHomeCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch { themeManager.setHomeCoordinates(lat, lon) }
    }

    /**
     * Clears all saved Home Location fields:
     * - City, region, country
     * - Latitude/longitude
     */
    fun clearHomeLocation() {
        viewModelScope.launch { themeManager.clearHomeLocation() }
    }

    /**
     * Attempts to geocode the provided city/region/country text into coordinates,
     * then persists both:
     *
     * 1. The textual components (city, region, country), and
     * 2. The resolved latitude + longitude (if geocoding succeeds)
     *
     * This allows Home Location to be fully functional for lunar calculations,
     * auto-fill, and user-facing display.
     */
    fun geocodeAndSaveHomeLocation(
        city: String,
        region: String?,
        country: String
    ) {
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(getApplication(), Locale.getDefault())
                val query = listOfNotNull(city, region, country).joinToString(", ")

                val results = geocoder.getFromLocationName(query, 1)
                val best = results?.firstOrNull()

                if (best != null) {
                    themeManager.setHomeCoordinates(best.latitude, best.longitude)
                } else {
                    Log.w("HomeLocation", "Geocoder returned no results for: $query")
                }

                // Always store the text components
                themeManager.setHomeCity(city)
                themeManager.setHomeRegion(region ?: "")
                themeManager.setHomeCountry(country)

            } catch (e: Exception) {
                Log.e("HomeLocation", "Geocoding failed: ${e.localizedMessage}")
            }
        }
    }

    // ---------------------------------------------------------
    // NOTIFICATION PREFERENCES
    // ---------------------------------------------------------

    // ------------------ Kp Index Alerts ------------------

    /** Whether geomagnetic storm alerts (Kp Index) are enabled. */
    val kpAlertsEnabled: LiveData<Boolean> =
        themeManager.kpAlertsEnabledFlow.asLiveData()

    /** Updates the Kp Index alert preference. */
    fun setKpAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { themeManager.setKpAlertsEnabled(enabled) }
    }

    // ------------------ ISS Alerts ------------------

    /** Whether ISS proximity notifications are enabled. */
    val issProximityEnabled: LiveData<Boolean> =
        themeManager.issProximityEnabledFlow.asLiveData()

    /** Updates ISS proximity preference. */
    fun setIssProximityEnabled(enabled: Boolean) {
        viewModelScope.launch { themeManager.setIssProximityEnabled(enabled) }
    }

    // ---------------------------------------------------------
    // CLEAR ALL PERSISTED DATA
    // ---------------------------------------------------------

    /**
     * Clears all cached API data stored by the repository, including:
     *
     * - NOAA Kp readings
     * - Lunar phase data
     * - ISS position cache
     * - Asteroid approach data
     *
     * This operation **does not** alter:
     * - Firebase authentication state
     * - User settings
     *
     * @param onFinished callback invoked on completion (e.g., to update UI).
     */
    fun clearCache(onFinished: () -> Unit) {
        viewModelScope.launch {
            themeManager.clearCache()
            onFinished()
        }
    }
}
