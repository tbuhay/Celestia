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
 * Central ViewModel for Celestia.
 *
 * Handles retrieval from the repository, transforms data for UI use,
 * and exposes state across NOAA Kp Index, ISS, Lunar Phase, and Asteroids.
 */
class CelestiaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CelestiaDatabase.getInstance(application).celestiaDao()
    private val repo = CelestiaRepository(dao, application)
    private val prefs = application.getSharedPreferences("celestia_prefs", 0)

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)

    // -------------------------------------------------------------------------
    // NOAA — Kp Index
    // -------------------------------------------------------------------------

    val readings = repo.readings.asLiveData()

    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    private val _groupedKp = MutableLiveData<List<KpHourlyGroup>>(emptyList())
    val groupedKp: LiveData<List<KpHourlyGroup>> = _groupedKp

    // -------------------------------------------------------------------------
    // ISS — Live Position & Crew Count
    // -------------------------------------------------------------------------

    val issReading = repo.issReading.asLiveData()

    private val _astronautCount = MutableLiveData<Int>()
    val astronautCount: LiveData<Int> = _astronautCount

    // -------------------------------------------------------------------------
    // Lunar Phase
    // -------------------------------------------------------------------------

    val lunarPhase = repo.lunarPhase.asLiveData()

    private val _isLunarLoading = MutableLiveData<Boolean>()
    val isLunarLoading: LiveData<Boolean> = _isLunarLoading

    private val _lunarError = MutableLiveData<String?>()
    val lunarError: LiveData<String?> = _lunarError

    private val _lunarUpdated = MutableLiveData<String>()
    val lunarUpdated: LiveData<String> = _lunarUpdated

    private val defaultLat = 49.8951
    private val defaultLon = -97.1384

    // -------------------------------------------------------------------------
    // Asteroids
    // -------------------------------------------------------------------------

    val nextAsteroid = repo.nextAsteroid.asLiveData()
    val asteroidList = repo.allAsteroids.asLiveData()

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    init {
        _lastUpdated.value = prefs.getString("last_updated", "Never")

        viewModelScope.launch {
            readings.value?.let { computeGroupedKp(it) }
        }

        readings.observeForever { list ->
            computeGroupedKp(list)
        }
    }

    // -------------------------------------------------------------------------
    // Global Refresh Handler
    // -------------------------------------------------------------------------

    @RequiresApi(Build.VERSION_CODES.O)
    fun refresh() {
        viewModelScope.launch {
            try {
                // NOAA
                launch {
                    try {
                        repo.refreshKpIndex()
                        Log.d("CelestiaVM", "NOAA data refreshed")

                        // -----------------------------
                        // KP ALERT CHECK
                        // -----------------------------
                        val latestKp = readings.value?.firstOrNull()?.estimatedKp ?: 0.0
                        Log.d("KPNOTIFY", "Latest Kp: $latestKp")

                        // Access DataStore
                        val prefsFlow = getApplication<Application>().themeDataStore.data

                        // Is Kp alerts toggle ON?
                        val alertsEnabled = prefsFlow
                            .map { it[ThemeKeys.KP_ALERTS_ENABLED] ?: false }
                            .first()

                        // What was the last Kp we alerted on?
                        val lastAlertedKp = prefsFlow
                            .map { it[ThemeKeys.LAST_ALERTED_KP] ?: -1f }
                            .first()

                        // Is app currently open?
                        val isForeground = AppLifecycleTracker.isAppInForeground

                        Log.d("KPNOTIFY", "alertsEnabled=$alertsEnabled, lastAlerted=$lastAlertedKp, foreground=$isForeground")

                        // Condition for firing a NEW alert:
                        val shouldAlert =
                            alertsEnabled &&              // user enabled alerts
                                    !isForeground &&              // app NOT open
                                    latestKp >= 3.5 &&            // storm threshold
                                    latestKp.toFloat() != lastAlertedKp   // Kp changed since last alert

                        if (shouldAlert) {
                            NotificationHelper.send(
                                context = getApplication<Application>().applicationContext,
                                title = "Kp Alert",
                                message = "Kp Index has reached $latestKp"
                            )

                            // Save this Kp so we don't alert again until it changes
                            viewModelScope.launch {
                                getApplication<Application>().themeDataStore.edit {
                                    it[ThemeKeys.LAST_ALERTED_KP] = latestKp.toFloat()
                                }
                            }
                        }

                        // Reset alert memory if Kp drops below 5
                        if (latestKp < 3.5 && lastAlertedKp != -1f) {
                            viewModelScope.launch {
                                getApplication<Application>().themeDataStore.edit {
                                    it[ThemeKeys.LAST_ALERTED_KP] = -1f
                                }
                            }
                        }

                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "NOAA refresh failed", e)
                    }
                }

                // ISS
                launch {
                    try {
                        repo.refreshIssData()
                        Log.d("CelestiaVM", "ISS data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "ISS refresh failed", e)
                    }
                }

                // Lunar Phase
                launch {
                    try {
                        _isLunarLoading.postValue(true)

                        val useDeviceLocation =
                            getApplication<Application>()
                                .themeDataStore
                                .data
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

                // Asteroids
                launch {
                    try {
                        repo.refreshAsteroids()
                        Log.d("CelestiaVM", "Asteroid data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "Asteroid refresh failed", e)
                    }
                }

                // Global “Last Updated”
                val now = TimeUtils.format(System.currentTimeMillis().toString())
                prefs.edit().putString("last_updated", now).apply()
                _lastUpdated.postValue(now)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Error during refresh()", e)
            }
        }
    }

    // -------------------------------------------------------------------------
    // ISS — Astronaut Helpers
    // -------------------------------------------------------------------------

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
    // Lunar Helpers
    // -------------------------------------------------------------------------

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

    fun daysUntilNextFullMoon(age: Double): Double =
        LunarHelper.daysUntilNextFullMoon(age)

    fun daysUntilNextNewMoon(age: Double): Double =
        LunarHelper.daysUntilNextNewMoon(age)

    fun getMoonAge(): Double = LunarHelper.getMoonAge()

    // -------------------------------------------------------------------------
    // Kp Index Grouping
    // -------------------------------------------------------------------------

    fun computeGroupedKp(readings: List<KpReading>) {
        viewModelScope.launch(Dispatchers.Default) {
            val grouped = groupKpReadingsHourly(readings)
            _groupedKp.postValue(grouped)
        }
    }

    fun groupKpReadingsHourly(readings: List<KpReading>): List<KpHourlyGroup> {
        if (readings.isEmpty()) return emptyList()

        val formatterUtc = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val localTZ = TimeZone.getDefault()

        return readings
            .map { reading ->
                val utcMillis = formatterUtc.parse(reading.timestamp)!!.time

                // Convert UTC → local
                val cal = Calendar.getInstance(localTZ).apply {
                    timeInMillis = utcMillis
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val localHourStart = Date(cal.timeInMillis)
                localHourStart to reading
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

    fun getLatestValidKp(readings: List<KpReading>): KpReading? {
        if (readings.isEmpty()) return null

        // Convert timestamps to millis first
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun toMillis(ts: String): Long = formatter.parse(ts)?.time ?: 0L

        val newestMillis = readings.maxOf { toMillis(it.timestamp) }

        return readings.firstOrNull { reading ->
            val millis = toMillis(reading.timestamp)
            val isNewest = millis == newestMillis

            !(isNewest && reading.estimatedKp == 0.0)
        } ?: readings.firstOrNull()
    }

    // -------------------------------------------------------------------------
    // Asteroid Helpers (delegations only)
    // -------------------------------------------------------------------------

    fun getNext7DaysList(list: List<AsteroidApproach>) =
        AsteroidHelper.getNext7DaysList(list)

    fun getFeaturedAsteroid(list: List<AsteroidApproach>) =
        AsteroidHelper.getFeaturedAsteroid(list)

    // -------------------------------------------------------------------------
    // Device Location
    // -------------------------------------------------------------------------

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