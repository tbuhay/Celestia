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
import com.example.celestia.data.model.ObservationEntry
import androidx.lifecycle.asLiveData
import com.example.celestia.data.model.WeatherSnapshot

/**
 * **CelestiaViewModel — the central orchestrator for the Celestia application.**
 *
 * This ViewModel acts as the unified interface between:
 *
 * - **NOAA Kp Index** (geomagnetic activity)
 * - **ISS live location** & astronaut crew count
 * - **Lunar phase calculations** (lat/lon dependent)
 * - **NASA asteroid approach data**
 * - **Weather data for Observation Journal auto-fill**
 * - **Room database** (cached readings, journal entries)
 * - **DataStore preferences** (home location, Kp alerts, theme settings)
 * - **Local notification rules** (Kp alerting in background)
 *
 * Because this extends [AndroidViewModel], it has safe access to the application
 * context, which is required for:
 *
 * - FusedLocationProviderClient
 * - SharedPreferences
 * - DataStore
 * - Notification dispatching
 *
 * All heavy operations are delegated to [CelestiaRepository], ensuring clean
 * separation of concerns and testability.
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
    // NOAA Kp Index
    // -------------------------------------------------------------------------

    /** Live NOAA Kp Index readings pulled from Room. */
    val readings = repo.readings.asLiveData()

    /** Last updated text for UI header (stored in SharedPreferences). */
    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    /** Hourly grouped Kp readings for charts and trend insights. */
    private val _groupedKp = MutableLiveData<List<KpHourlyGroup>>(emptyList())
    val groupedKp: LiveData<List<KpHourlyGroup>> = _groupedKp

    // -------------------------------------------------------------------------
    // ISS — Live Position + Crew Count
    // -------------------------------------------------------------------------

    /** Live ISS location reading (lat/lon/alt/vel). */
    val issReading = repo.issReading.asLiveData()

    /** Current astronaut count aboard the ISS. */
    private val _astronautCount = MutableLiveData<Int>()
    val astronautCount: LiveData<Int> = _astronautCount

    // -------------------------------------------------------------------------
    // Lunar Phase
    // -------------------------------------------------------------------------

    /** Live lunar phase data for the user’s location. */
    val lunarPhase = repo.lunarPhase.asLiveData()

    private val _isLunarLoading = MutableLiveData<Boolean>()
    val isLunarLoading: LiveData<Boolean> = _isLunarLoading

    private val _lunarError = MutableLiveData<String?>()
    val lunarError: LiveData<String?> = _lunarError

    private val _lunarUpdated = MutableLiveData<String>()
    val lunarUpdated: LiveData<String?> = _lunarUpdated

    /** Default coordinates used when device location is unavailable. */
    private val defaultLat = 49.8951
    private val defaultLon = -97.1384

    /**
     * Computes the preferred fallback location:
     *
     * Priority:
     * 1. **Home Location** (user-set location)
     * 2. **Winnipeg** (legacy fallback)
     *
     * @return Pair(lat, lon)
     */
    private suspend fun getFallbackLocation(): Pair<Double, Double> {
        val prefsFlow = getApplication<Application>().themeDataStore.data

        val lat = prefsFlow.map { it[ThemeKeys.HOME_LAT] ?: 0f }.first()
        val lon = prefsFlow.map { it[ThemeKeys.HOME_LON] ?: 0f }.first()

        return if (lat != 0f || lon != 0f) {
            lat.toDouble() to lon.toDouble()
        } else {
            defaultLat to defaultLon
        }
    }

    // -------------------------------------------------------------------------
    // Asteroids
    // -------------------------------------------------------------------------

    /** Next upcoming asteroid for dashboard preview. */
    val nextAsteroid = repo.nextAsteroid.asLiveData()

    /** All cached asteroid records. */
    val asteroidList = repo.allAsteroids.asLiveData()

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    init {
        _lastUpdated.value = prefs.getString("last_updated", "Never")

        // Precompute grouped Kp if available
        viewModelScope.launch {
            readings.value?.let { computeGroupedKp(it) }
        }

        // Recompute whenever NOAA readings change
        readings.observeForever { computeGroupedKp(it) }
    }

    // -------------------------------------------------------------------------
    // Global Refresh Handler
    // -------------------------------------------------------------------------

    /**
     * Performs a system-wide refresh of:
     *
     * - **NOAA Kp Index**
     * - **ISS live location**
     * - **Lunar phase**
     * - **NASA asteroid feeds**
     *
     * Also updates:
     * - Last updated timestamp (SharedPreferences)
     * - Any DataStore keys related to Kp alert evaluation
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() {
        viewModelScope.launch {
            try {
                // NOAA - Kp Index
                launch {
                    try {
                        repo.refreshKpIndex()
                        handleKpAlertLogic()
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "NOAA refresh failed", e)
                    }
                }

                // --- PRESENTATION NOTE ---------------------
                // This coroutine fetches the ISS position from the API.
                // Because the map screen observes LiveData from the ViewModel,
                // the ISS marker automatically moves whenever this refresh function runs.
                // This demonstrates reactive UI in practice.

                // ISS
                launch {
                    try {
                        repo.refreshIssLocation()
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "ISS refresh failed", e)
                    }
                }

                // Lunar data
                launch {
                    refreshLunarPhase()
                }

                // Asteroids
                launch {
                    try {
                        repo.refreshAsteroids()
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "Asteroid refresh failed", e)
                    }
                }

                // UI timestamp update
                val now = TimeUtils.format(System.currentTimeMillis().toString())
                prefs.edit().putString("last_updated", now).apply()
                _lastUpdated.postValue(now)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Global refresh() failed", e)
            }
        }
    }

    /**
     * Evaluates user Kp alert preferences and dispatches notifications when:
     *
     * Conditions:
     * - Alerts enabled in settings
     * - App is **not in foreground**
     * - Latest Kp ≥ 3.5 (geomagnetic storm threshold)
     * - Kp has changed since last alert
     *
     * Resets alert memory when Kp drops below threshold.
     */
    private suspend fun handleKpAlertLogic() {
        val latestKp = readings.value?.firstOrNull()?.estimatedKp ?: 0.0

        val prefsFlow = getApplication<Application>().themeDataStore.data
        val alertsEnabled = prefsFlow.map { it[ThemeKeys.KP_ALERTS_ENABLED] ?: false }.first()
        val lastAlertedKp = prefsFlow.map { it[ThemeKeys.LAST_ALERTED_KP] ?: -1f }.first()

        val isForeground = AppLifecycleTracker.isAppInForeground

        val shouldAlert =
            alertsEnabled &&
                    !isForeground &&
                    latestKp >= 3.5 &&
                    latestKp.toFloat() != lastAlertedKp

        if (shouldAlert) {
            NotificationHelper.sendKpNotification(
                context = getApplication<Application>(),
                message = "Kp Index has reached $latestKp"
            )

            getApplication<Application>().themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = latestKp.toFloat()
            }
        }

        // Reset memory when storm level drops
        if (latestKp < 3.5 && lastAlertedKp != -1f) {
            getApplication<Application>().themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = -1f
            }
        }
    }

    // -------------------------------------------------------------------------
    // ISS Helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves cached astronaut count from the repository.
     * NASA updates this infrequently, so cached values help reduce API calls.
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
     * Refreshes lunar phase data using:
     *
     * Priority:
     * 1. Device GPS (if enabled)
     * 2. Home Location (DataStore)
     * 3. Winnipeg fallback
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
                        viewModelScope.launch {
                            val (lat, lon) = getFallbackLocation()
                            repo.refreshLunarPhase(lat, lon)
                        }
                    }
                )
            } else {
                val (lat, lon) = getFallbackLocation()
                repo.refreshLunarPhase(lat, lon)
            }

            _lunarUpdated.postValue(System.currentTimeMillis().toString())

        } catch (e: Exception) {
            _lunarError.postValue("Lunar refresh failed")
        } finally {
            _isLunarLoading.postValue(false)
        }
    }

    /** Maps lunar phase strings to drawable resource IDs. */
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

    /** Convenience wrapper for LunarHelper. */
    fun daysUntilNextFullMoon(age: Double): Double = LunarHelper.daysUntilNextFullMoon(age)

    /** Convenience wrapper for LunarHelper. */
    fun daysUntilNextNewMoon(age: Double): Double = LunarHelper.daysUntilNextNewMoon(age)

    /** Returns moon age via helper. */
    fun getMoonAge(): Double = LunarHelper.getMoonAge()

    // -------------------------------------------------------------------------
    // Kp Index — Grouping & Filtering
    // -------------------------------------------------------------------------

    /**
     * Asynchronously computes grouped Kp readings (hourly buckets) for UI charts.
     *
     * @param readings Raw list of NOAA readings.
     */
    fun computeGroupedKp(readings: List<KpReading>) {
        viewModelScope.launch(Dispatchers.Default) {
            val grouped = groupKpReadingsHourly(readings)
            _groupedKp.postValue(grouped)
        }
    }

    /**
     * Groups Kp readings into hourly buckets, computing:
     * - Average Kp
     * - High & Low values
     *
     * Output is sorted newest → oldest.
     *
     * @return List of hourly Kp groups.
     */

    // --- PRESENTATION NOTE (Milestone 2: Making NOAA Data Useful) ----------------
    // The raw NOAA feed is not very user-friendly — it sends many readings
    // across different timestamps and sometimes contains invalid values.
    //
    // This function:
    // - Converts UTC timestamps into local time
    // - Groups all readings by hourly buckets
    // - Computes average, high, and low Kp values per hour
    //
    // This transformed the data into a format the dashboard card could display
    // clearly, giving users a simple trend preview instead of raw numeric noise.
    fun groupKpReadingsHourly(readings: List<KpReading>): List<KpHourlyGroup> {
        if (readings.isEmpty()) return emptyList()

        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun parseUTC(ts: String) = formatter.parse(ts)!!.time

        val localTZ = TimeZone.getDefault()

        return readings
            .map { reading ->
                val utc = parseUTC(reading.timestamp)
                val cal = Calendar.getInstance(localTZ).apply {
                    timeInMillis = utc
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
     * Returns the most reliable Kp reading, filtering out cases where:
     * - The newest timestamp has a `0.0` value (NOAA propagation delay)
     *
     * @return Cleaned latest Kp reading.
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
    // Asteroid Helpers
    // -------------------------------------------------------------------------

    /** Returns the next 7-day asteroid approach list via helper. */
    fun getNext7DaysList(list: List<AsteroidApproach>) =
        AsteroidHelper.getNext7DaysList(list)

    /** Returns dashboard-friendly featured asteroid via helper. */
    fun getFeaturedAsteroid(list: List<AsteroidApproach>) =
        AsteroidHelper.getFeaturedAsteroid(list)

    // -------------------------------------------------------------------------
    // Device Location Helpers
    // -------------------------------------------------------------------------

    /**
     * Retrieves the last known device coordinates.
     *
     * - Calls [onResult] when successful
     * - Calls [onError] if unavailable or missing permissions
     */

    // --- PRESENTATION NOTE (Challenge 3: UX & Location Fallbacks) ----------------
    // This function attempts to retrieve the device's last-known location.
    // It delegates ALL fallback logic to its callers via:
    //   - onResult() for valid locations
    //   - onError() when location is null or unavailable
    //
    // By keeping this function simple, I was able to implement a clean 3-level
    // fallback in the Observation Journal feature:
    //
    // 1) Device GPS
    // 2) Home Location saved by the user
    // 3) Default location (Winnipeg)
    //
    // This design improved reliability and made UX more predictable.
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

    // -------------------------------------------------------------------------
    // Observation Journal Persistence
    // -------------------------------------------------------------------------

    /** All saved journal entries (LiveData). */
    val allJournalEntries: LiveData<List<ObservationEntry>> =
        repo.getAllObservations().asLiveData()

    /** Loads a single journal entry by ID. */
    suspend fun getJournalEntry(id: Int): ObservationEntry? =
        repo.getObservationById(id)

    /** Persists a new or updated journal entry. */
    fun saveJournalEntry(entry: ObservationEntry) {
        viewModelScope.launch { repo.saveObservation(entry) }
    }

    /** Deletes a saved journal entry. */
    fun deleteJournalEntry(entry: ObservationEntry) {
        viewModelScope.launch { repo.deleteObservation(entry) }
    }

    // -------------------------------------------------------------------------
    // Auto-Fill Weather + Sky Data for New Journal Entries
    // -------------------------------------------------------------------------

    /**
     * Data class representing all auto-filled sky/environment fields used when
     * creating a new Observation Journal entry.
     */
    data class AutoObservationFields(
        val latitude: Double? = null,
        val longitude: Double? = null,
        val kpIndex: Double? = null,
        val issLat: Double? = null,
        val issLon: Double? = null,
        val temperatureC: Double? = null,
        val cloudCoverPercent: Int? = null,
        val weatherSummary: String? = null,
        val locationName: String? = null
    )

    /** LiveData exposing the latest auto-fill snapshot for new entries. */
    private val _autoObservationFields = MutableLiveData<AutoObservationFields?>(null)
    val autoObservationFields: LiveData<AutoObservationFields?> = _autoObservationFields

    /**
     * Retrieves home latitude/longitude if set.
     *
     * @return Pair(lat, lon) or (null, null) if unset.
     */
    private suspend fun getHomeLocation(): Pair<Double?, Double?> {
        val prefsFlow = getApplication<Application>().themeDataStore.data

        val lat = prefsFlow.map { it[ThemeKeys.HOME_LAT] ?: 0f }.first()
        val lon = prefsFlow.map { it[ThemeKeys.HOME_LON] ?: 0f }.first()

        return if (lat != 0f && lon != 0f) {
            lat.toDouble() to lon.toDouble()
        } else null to null
    }

    /**
     * **Populates all auto-fill fields** when creating a new Observation Journal entry.
     *
     * Priority:
     * 1. Device location
     * 2. Home Location
     * 3. Winnipeg fallback
     *
     * Includes:
     * - Latest Kp Index
     * - Latest ISS location
     * - Weather snapshot
     * - Location name (if derived from Home or fallback)
     */
    fun refreshAutoObservationFieldsForNewEntry() {
        viewModelScope.launch {
            try {
                // Latest Kp
                val kpList = readings.value.orEmpty()
                val latestKp = getLatestValidKp(kpList)?.estimatedKp

                // Quick attempt to refresh ISS
                val iss = try {
                    repo.refreshIssLocation()
                } catch (_: Exception) { null }

                val issLat = iss?.latitude
                val issLon = iss?.longitude

                // Device → Home → Default fallback
                getDeviceLocation(
                    onResult = { lat, lon ->
                        viewModelScope.launch {
                            fetchWeatherAndPost(
                                lat = lat,
                                lon = lon,
                                kpIndex = latestKp,
                                issLat = issLat,
                                issLon = issLon
                            )
                        }
                    },
                    onError = {
                        viewModelScope.launch {
                            val prefsFlow = getApplication<Application>().themeDataStore.data

                            val homeCity = prefsFlow.map { it[ThemeKeys.HOME_CITY] ?: "" }.first()
                            val homeRegion = prefsFlow.map { it[ThemeKeys.HOME_REGION] ?: "" }.first()
                            val homeCountry = prefsFlow.map { it[ThemeKeys.HOME_COUNTRY] ?: "" }.first()

                            val (homeLat, homeLon) = getHomeLocation()
                            val homeName = listOf(homeCity, homeRegion, homeCountry)
                                .filter { it.isNotBlank() }
                                .joinToString(", ")

                            // Home location available
                            if (homeLat != null && homeLon != null) {
                                fetchWeatherAndPost(
                                    lat = homeLat,
                                    lon = homeLon,
                                    kpIndex = latestKp,
                                    issLat = issLat,
                                    issLon = issLon,
                                    locationName = homeName
                                )
                                return@launch
                            }

                            // Default Winnipeg
                            fetchWeatherAndPost(
                                lat = defaultLat,
                                lon = defaultLon,
                                kpIndex = latestKp,
                                issLat = issLat,
                                issLon = issLon,
                                locationName = "Winnipeg, MB, Canada"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _autoObservationFields.postValue(null)
            }
        }
    }

    /**
     * Fetches a weather snapshot for the given coordinates, then posts a full
     * auto-fill data model to LiveData for Observation Editor.
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param kpIndex Latest Kp Index (optional)
     * @param issLat ISS latitude
     * @param issLon ISS longitude
     * @param locationName Display name for the coordinates (if available)
     */
    private suspend fun fetchWeatherAndPost(
        lat: Double,
        lon: Double,
        kpIndex: Double?,
        issLat: Double?,
        issLon: Double?,
        locationName: String? = null
    ) {
        val weather = repo.fetchCurrentWeather(lat, lon)

        _autoObservationFields.postValue(
            AutoObservationFields(
                latitude = lat,
                longitude = lon,
                kpIndex = kpIndex,
                issLat = issLat,
                issLon = issLon,
                temperatureC = weather?.temperatureC,
                cloudCoverPercent = weather?.cloudCoverPercent,
                weatherSummary = weather?.weatherSummary,
                locationName = locationName
            )
        )
    }
}
