package com.example.celestia.ui.viewmodel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.*
import com.example.celestia.R
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.KpHourlyGroup
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.repository.CelestiaRepository
import com.example.celestia.data.store.ThemeKeys
import com.example.celestia.data.store.themeDataStore
import com.example.celestia.utils.AppLifecycleTracker
import com.example.celestia.notifications.NotificationHelper
import com.example.celestia.utils.AsteroidHelper
import com.example.celestia.utils.LunarHelper
import com.example.celestia.utils.TimeUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Primary ViewModel for Celestia.
 * Acts as the **central orchestrator** for:
 *
 * - NOAA Kp Index
 * - International Space Station (ISS) live position & astronaut count
 * - Lunar phase & location-based moon data
 * - NASA asteroid approach data
 * - Notification logic (Kp alerts)
 * - DataStore preference-driven behavior (location, alerts, theme)
 * - Data transformations for UI (grouping, filtering, formatting)
 *
 * This ViewModel interacts directly with:
 * - [CelestiaRepository] for API + Room DB operations
 * - DataStore (themeDataStore)
 * - SharedPreferences (for last_updated text)
 * - FusedLocationProviderClient (for moon phase location)
 *
 * Because it extends [AndroidViewModel], it has access to Application context.
 */
class CelestiaViewModel(application: Application) : AndroidViewModel(application) {

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private val dao = CelestiaDatabase.getInstance(application).celestiaDao()
    private val repo = CelestiaRepository(dao, application)
    private val prefs = application.getSharedPreferences("celestia_prefs", 0)

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // -------------------------------------------------------------------------
    // NOAA — Kp Index
    // -------------------------------------------------------------------------

    /** Live NOAA Kp Index readings pulled from Room. */
    val readings = repo.readings.asLiveData()

    /** Last updated timestamp stored in SharedPreferences. */
    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    /** Hourly grouped averages of Kp readings for charts & trend cards. */
    private val _groupedKp = MutableLiveData<List<KpHourlyGroup>>(emptyList())
    val groupedKp: LiveData<List<KpHourlyGroup>> = _groupedKp

    // -------------------------------------------------------------------------
    // ISS — Live Position + Crew Count
    // -------------------------------------------------------------------------

    /** Live ISS position from repository (Room cached). */
    val issReading = repo.issReading.asLiveData()

    /** Live astronaut count (refreshed separately). */
    private val _astronautCount = MutableLiveData<Int>()
    val astronautCount: LiveData<Int> = _astronautCount

    // -------------------------------------------------------------------------
    // Lunar Phase
    // -------------------------------------------------------------------------

    /** Live lunar phase data. */
    val lunarPhase = repo.lunarPhase.asLiveData()

    private val _isLunarLoading = MutableLiveData<Boolean>()
    val isLunarLoading: LiveData<Boolean> = _isLunarLoading

    private val _lunarError = MutableLiveData<String?>()
    val lunarError: LiveData<String?> = _lunarError

    private val _lunarUpdated = MutableLiveData<String>()
    val lunarUpdated: LiveData<String?> = _lunarUpdated

    /** Default coordinates (Winnipeg) used when device location is disabled. */
    private val defaultLat = 49.8951
    private val defaultLon = -97.1384

    // -------------------------------------------------------------------------
    // Asteroids
    // -------------------------------------------------------------------------

    /** Next approaching asteroid used for dashboard card. */
    val nextAsteroid = repo.nextAsteroid.asLiveData()

    /** Complete list of cached asteroid approaches (Room). */
    val asteroidList = repo.allAsteroids.asLiveData()

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    init {
        _lastUpdated.value = prefs.getString("last_updated", "Never")

        // Precompute grouped Kp if cached data already exists
        viewModelScope.launch {
            readings.value?.let { computeGroupedKp(it) }
        }

        // Recompute grouping whenever new NOAA readings arrive
        readings.observeForever { list ->
            computeGroupedKp(list)
        }
    }

    // -------------------------------------------------------------------------
    // Global Refresh Handler — Fetches *all* API systems
    // -------------------------------------------------------------------------

    /**
     * Refreshes all major datasets:
     *
     * - NOAA Kp Index (with notification rules)
     * - ISS live location
     * - Lunar phase (device or default location)
     * - Asteroid approaches
     *
     * Also updates:
     * - "Last Updated" SharedPreference
     * - DataStore keys related to Kp notifications
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() {
        viewModelScope.launch {
            try {
                // ------------------ NOAA KP INDEX ------------------
                launch {
                    try {
                        repo.refreshKpIndex()
                        Log.d("CelestiaVM", "NOAA data refreshed")

                        // Evaluate Kp alert logic
                        handleKpAlertLogic()
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "NOAA refresh failed", e)
                    }
                }

                // ------------------ ISS POSITION -------------------
                launch {
                    try {
                        repo.refreshIssLocation()
                        Log.d("CelestiaVM", "ISS data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "ISS refresh failed", e)
                    }
                }

                // ------------------ LUNAR DATA ---------------------
                launch {
                    refreshLunarPhase()
                }

                // ------------------ ASTEROIDS ----------------------
                launch {
                    try {
                        repo.refreshAsteroids()
                        Log.d("CelestiaVM", "Asteroid data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "Asteroid refresh failed", e)
                    }
                }

                // Global timestamp update
                val now = TimeUtils.format(System.currentTimeMillis().toString())
                prefs.edit().putString("last_updated", now).apply()
                _lastUpdated.postValue(now)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Error during refresh()", e)
            }
        }
    }

    /**
     * Handles logic for sending **Kp Index alerts** when:
     *
     * - User has alerts enabled
     * - App is in background
     * - Latest Kp ≥ 3.5
     * - Kp value changed since last alert
     */
    private suspend fun handleKpAlertLogic() {
        val latestKp = readings.value?.firstOrNull()?.estimatedKp ?: 0.0

        val prefsFlow = getApplication<Application>().themeDataStore.data

        val alertsEnabled = prefsFlow
            .map { it[ThemeKeys.KP_ALERTS_ENABLED] ?: false }
            .first()

        val lastAlertedKp = prefsFlow
            .map { it[ThemeKeys.LAST_ALERTED_KP] ?: -1f }
            .first()

        val isForeground = AppLifecycleTracker.isAppInForeground

        val shouldAlert =
            alertsEnabled &&
                    !isForeground &&
                    latestKp >= 3.5 &&
                    latestKp.toFloat() != lastAlertedKp

        if (shouldAlert) {
            NotificationHelper.sendKpNotification(
                context = getApplication<Application>().applicationContext,
                message = "Kp Index has reached $latestKp"
            )

            // Store last alerted Kp
            getApplication<Application>().themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = latestKp.toFloat()
            }
        }

        // Reset memory when dropping below storm threshold
        if (latestKp < 3.5 && lastAlertedKp != -1f) {
            getApplication<Application>().themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = -1f
            }
        }
    }

    // -------------------------------------------------------------------------
    // ISS — Astronaut Helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves cached astronaut count from repository.
     * (NASA data updates infrequently — cached to minimize calls.)
     */
    fun fetchAstronauts() {
        viewModelScope.launch {
            try {
                _astronautCount.value = repo.getCachedAstronautCount()
            } catch (e: Exception) {
                _astronautCount.value = 0
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lunar Phase Helpers
    // -------------------------------------------------------------------------

    /**
     * Refreshes lunar phase using either:
     * - Device location (if user enabled)
     * - Default coordinates (Winnipeg)
     */
    private suspend fun refreshLunarPhase() {
        try {
            _isLunarLoading.postValue(true)

            val useDeviceLocation =
                getApplication<Application>().themeDataStore.data
                    .map { it[ThemeKeys.USE_DEVICE_LOCATION] ?: false }
                    .first()

            if (useDeviceLocation) {
                getDeviceLocation(
                    onResult = { lat, lon ->
                        viewModelScope.launch { repo.refreshLunarPhase(lat, lon) }
                    },
                    onError = {
                        viewModelScope.launch { repo.refreshLunarPhase(defaultLat, defaultLon) }
                    }
                )
            } else {
                repo.refreshLunarPhase(defaultLat, defaultLon)
            }

            _lunarUpdated.postValue(System.currentTimeMillis().toString())

        } catch (e: Exception) {
            _lunarError.postValue("Lunar refresh failed")
            Log.e("CelestiaVM", "Lunar refresh failed", e)
        } finally {
            _isLunarLoading.postValue(false)
        }
    }

    /** Returns drawable resource ID matching the lunar phase string. */
    fun getMoonPhaseIconRes(phase: String?): Int {
        return when (phase?.uppercase(Locale.US)) {
            "NEW_MOON"        -> R.drawable.new_moon
            "WAXING_CRESCENT" -> R.drawable.waxing_crescent_moon
            "FIRST_QUARTER"   -> R.drawable.first_quarter_moon
            "WAXING_GIBBOUS"  -> R.drawable.waxing_gibbous_moon
            "FULL_MOON"       -> R.drawable.full_moon
            "WANING_GIBBOUS"  -> R.drawable.waning_gibbous_moon
            "LAST_QUARTER"    -> R.drawable.last_quarter_moon
            "WANING_CRESCENT" -> R.drawable.waning_crescent_moon
            else              -> R.drawable.full_moon
        }
    }

    /** Delegates full moon countdown logic to [LunarHelper]. */
    fun daysUntilNextFullMoon(age: Double): Double =
        LunarHelper.daysUntilNextFullMoon(age)

    /** Delegates new moon countdown logic to [LunarHelper]. */
    fun daysUntilNextNewMoon(age: Double): Double =
        LunarHelper.daysUntilNextNewMoon(age)

    /** Returns moon age using internal helper. */
    fun getMoonAge(): Double = LunarHelper.getMoonAge()

    // -------------------------------------------------------------------------
    // Kp Index — Grouping & Filtering
    // -------------------------------------------------------------------------

    /**
     * Computes hourly grouped averages of Kp readings for trend charts.
     * Runs on Dispatchers.Default.
     */
    fun computeGroupedKp(readings: List<KpReading>) {
        viewModelScope.launch(Dispatchers.Default) {
            val grouped = groupKpReadingsHourly(readings)
            _groupedKp.postValue(grouped)
        }
    }

    /**
     * Groups Kp readings by **local hour**, computing:
     * - Average Kp
     * - High value
     * - Low value
     *
     * Returned list is sorted newest → oldest.
     */
    fun groupKpReadingsHourly(readings: List<KpReading>): List<KpHourlyGroup> {
        if (readings.isEmpty()) return emptyList()

        val formatterUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val localTZ = TimeZone.getDefault()

        return readings
            .map { reading ->
                val utcMillis = formatterUtc.parse(reading.timestamp)!!.time

                val cal = Calendar.getInstance(localTZ).apply {
                    timeInMillis = utcMillis
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                Date(cal.timeInMillis) to reading
            }
            .groupBy { it.first }
            .map { (hour, items) ->
                val values = items.map { it.second.estimatedKp }
                KpHourlyGroup(
                    hour = hour,
                    avg = values.average(),
                    high = values.maxOrNull() ?: 0.0,
                    low = values.minOrNull() ?: 0.0
                )
            }
            .sortedByDescending { it.hour }
    }

    /**
     * Returns the newest valid Kp reading, filtering out cases where:
     * - The latest timestamp exists but its value is `0.0` (NOAA update lag)
     */
    fun getLatestValidKp(readings: List<KpReading>): KpReading? {
        if (readings.isEmpty()) return null

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun toMillis(ts: String): Long = formatter.parse(ts)?.time ?: 0L

        val newestMillis = readings.maxOf { toMillis(it.timestamp) }

        return readings.firstOrNull { reading ->
            val millis = toMillis(reading.timestamp)
            !(millis == newestMillis && reading.estimatedKp == 0.0)
        } ?: readings.firstOrNull()
    }

    // -------------------------------------------------------------------------
    // Asteroid Helpers — Delegated to [AsteroidHelper]
    // -------------------------------------------------------------------------

    /** Returns next 7-day list of asteroid approaches. */
    fun getNext7DaysList(list: List<AsteroidApproach>) =
        AsteroidHelper.getNext7DaysList(list)

    /** Returns featured asteroid for dashboard preview. */
    fun getFeaturedAsteroid(list: List<AsteroidApproach>) =
        AsteroidHelper.getFeaturedAsteroid(list)

    // -------------------------------------------------------------------------
    // Device Location Helper
    // -------------------------------------------------------------------------

    /**
     * Gets the last known device location.
     *
     * - Calls [onResult] with `(lat, lon)` on success
     * - Falls back to [onError] if unavailable or permissions are missing
     */
    fun getDeviceLocation(
        onResult: (lat: Double, lon: Double) -> Unit,
        onError: () -> Unit
    ) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) onResult(loc.latitude, loc.longitude)
                else onError()
            }
        } catch (e: Exception) {
            onError()
        }
    }
}
