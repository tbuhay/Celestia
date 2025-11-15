package com.example.celestia.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.celestia.R
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.Astronaut
import com.example.celestia.data.model.KpHourlyGroup
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.LunarPhaseEntity
import com.example.celestia.data.model.WikiSummary
import com.example.celestia.data.repository.CelestiaRepository
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

class CelestiaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CelestiaDatabase.getInstance(application).dao()
    private val repo = CelestiaRepository(dao)
    private val prefs = application.getSharedPreferences("celestia_prefs", 0)

    // -------------------------------------------------------------------------
    // NOAA — Kp Index
    // -------------------------------------------------------------------------
    val readings = repo.readings.asLiveData()

    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    // -------------------------------------------------------------------------
    // ISS Live Position
    // -------------------------------------------------------------------------
    val issReading = repo.issReading.asLiveData()

    private val _selectedAstronaut = MutableLiveData<WikiSummary?>()
    val selectedAstronaut: LiveData<WikiSummary?> = _selectedAstronaut

    private val _astronauts = MutableLiveData<List<Astronaut>>()
    val astronauts: LiveData<List<Astronaut>> = _astronauts
    // -------------------------------------------------------------------------
    // Lunar Phase (persistent in Room)
    // -------------------------------------------------------------------------
    val lunarPhase = repo.lunarPhase.asLiveData()

    private val _isLunarLoading = MutableLiveData<Boolean>()
    val isLunarLoading: LiveData<Boolean> = _isLunarLoading

    private val _lunarError = MutableLiveData<String?>()
    val lunarError: LiveData<String?> = _lunarError

    private val defaultLat = 49.8951
    private val defaultLon = -97.1384

    private val _lunarUpdated = MutableLiveData<String>()
    val lunarUpdated: LiveData<String> = _lunarUpdated

    // -------------------------------------------------------------------------
    // Asteroids
    // -------------------------------------------------------------------------
    val nextAsteroid = repo.nextAsteroid.asLiveData()
    val asteroidList = repo.allAsteroids.asLiveData()

    init {
        _lastUpdated.value = prefs.getString("last_updated", "Never")
    }

    // -------------------------------------------------------------------------
    // REFRESH ALL DATA
    // -------------------------------------------------------------------------
    fun refresh() {
        viewModelScope.launch {
            try {
                // NOAA
                launch {
                    try {
                        repo.refreshData()
                        Log.d("CelestiaVM", "NOAA data refreshed")
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
                        repo.refreshLunarPhase(defaultLat, defaultLon)
                        _lunarUpdated.postValue(currentLocalTime())
                        Log.d("CelestiaVM", "Lunar phase refreshed")
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

                // Update global "Last Updated" timestamp (for Home screen header)
                val now = TimeUtils.format(System.currentTimeMillis().toString())
                prefs.edit().putString("last_updated", now).apply()
                _lastUpdated.postValue(now)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Error during refresh()", e)
            }
        }
    }

    // -------------------------------------------------------------------------
    // ASTRONAUT HELPERS
    // -------------------------------------------------------------------------
    fun loadAstronautDetails(name: String) {
        viewModelScope.launch {
            try {
                Log.d("ASTRO", "Fetching Wikipedia for: $name")
                val result = repo.getAstronautSummary(name)   // <-- pass RAW name
                _selectedAstronaut.value = result
            } catch (e: Exception) {
                Log.e("ASTRO", "Wiki fetch failed", e)
                _selectedAstronaut.value = null
            }
        }
    }

    fun fetchAstronauts() {
        viewModelScope.launch {
            try {
                _astronauts.value = repo.loadAstronauts().filter { it.craft == "ISS" }
            } catch (e: Exception) {
                _astronauts.value = emptyList()
            }
        }
    }

    fun clearAstronaut() {
        _selectedAstronaut.value = null
    }

    // -------------------------------------------------------------------------
    // LUNAR HELPERS
    // -------------------------------------------------------------------------
    fun formatMoonPhaseName(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown Phase"
        return raw.lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

    fun parseIlluminationPercent(lunar: LunarPhaseEntity?): Double {
        if (lunar == null) return 0.0
        return lunar.illuminationPercent
            .replace("%", "")
            .trim()
            .toDoubleOrNull()
            ?.let { abs(it) }
            ?: 0.0
    }

    fun computeMoonAgeDays(lunar: LunarPhaseEntity?): Double {
        if (lunar == null) return 0.0
        return when (lunar.moonPhase.uppercase(Locale.US)) {
            "NEW_MOON"        -> 0.0
            "WAXING_CRESCENT" -> 4.0
            "FIRST_QUARTER"   -> 7.4
            "WAXING_GIBBOUS"  -> 11.0
            "FULL_MOON"       -> 14.8
            "WANING_GIBBOUS"  -> 18.4
            "LAST_QUARTER"    -> 22.1
            "WANING_CRESCENT" -> 26.0
            else              -> 0.0
        }
    }

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

    fun isWaxing(phase: String?): Boolean {
        return when (phase?.uppercase()) {
            "WAXING_CRESCENT",
            "FIRST_QUARTER",
            "WAXING_GIBBOUS",
            "FULL_MOON" -> true
            else -> false
        }
    }

    fun loadLunarPhase(latitude: Double = defaultLat, longitude: Double = defaultLon) {
        viewModelScope.launch {
            _isLunarLoading.value = true
            _lunarError.value = null

            try {
                repo.refreshLunarPhase(latitude, longitude)
                _lunarUpdated.postValue(currentLocalTime())
            } catch (e: Exception) {
                _lunarError.postValue("Unable to load lunar data")
                Log.e("CelestiaVM", "loadLunarPhase failed", e)
            } finally {
                _isLunarLoading.postValue(false)
            }
        }
    }

    private val lunarCycleDays = 29.53

    fun daysUntilNextFullMoon(age: Double): Double {
        val fullMoonAge = 14.8
        return if (age <= fullMoonAge) {
            fullMoonAge - age
        } else {
            (lunarCycleDays - age) + fullMoonAge
        }
    }

    fun daysUntilNextNewMoon(age: Double): Double {
        return if (age <= 0.0) {
            0.0
        } else {
            lunarCycleDays - age
        }
    }

    private fun currentLocalTime(): String {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date())
    }

    // -------------------------------------------------------------------------
    // GROUP HOURLY KP DATA (USED IN KpIndexScreen)
    // -------------------------------------------------------------------------
    fun groupKpReadingsHourly(readings: List<KpReading>): List<KpHourlyGroup> {
        if (readings.isEmpty()) return emptyList()

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        return readings
            .groupBy { reading ->
                val date = sdf.parse(reading.timestamp)!!   // <- fix nullable Date?
                val epoch = date.time
                Date(epoch - (epoch % (60 * 60 * 1000))) // floor to the top of the hour
            }
            .map { (hour, group) ->
                val values = group.map { it.estimatedKp }
                val avg = values.average()
                val high = values.maxOrNull() ?: avg
                val low = values.minOrNull() ?: avg

                KpHourlyGroup(hour, avg, high, low)
            }
            .sortedByDescending { it.hour }
    }

    // -------------------------------------------------------------------------
    // ASTEROID HELPERS — Option D Filtering
    // -------------------------------------------------------------------------
    private fun avgDiameter(asteroid: AsteroidApproach): Double {
        return (asteroid.diameterMinMeters + asteroid.diameterMaxMeters) / 2.0
    }

    private fun isMeaningful(asteroid: AsteroidApproach): Boolean {
        val diameter = avgDiameter(asteroid)
        val isBigEnough = diameter >= 120.0     // meters
        val isCloseEnough = asteroid.missDistanceAu <= 0.5
        return isBigEnough && isCloseEnough
    }

    private fun isWithinNext7Days(dateString: String): Boolean {
        val today = LocalDate.now()
        val date = LocalDate.parse(dateString)
        return !date.isBefore(today) && !date.isAfter(today.plusDays(7))
    }

    fun getMeaningfulAsteroids(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list.filter { asteroid ->
            isMeaningful(asteroid) && isWithinNext7Days(asteroid.approachDate)
        }
            .sortedWith(
                compareBy<AsteroidApproach> { it.missDistanceAu }
                    .thenBy { LocalDate.parse(it.approachDate) }
            )
    }

    fun getNext7DaysList(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list.filter { isMeaningful(it) && isWithinNext7Days(it.approachDate) }
            .sortedBy { LocalDate.parse(it.approachDate) }
    }

    // Featured asteroid for UI (Option D)
    fun getFeaturedAsteroid(list: List<AsteroidApproach>): AsteroidApproach? {
        if (list.isEmpty()) return null

        // First: meaningful asteroid (size + distance) within next 7 days
        val meaningful = getMeaningfulAsteroids(list)
        if (meaningful.isNotEmpty()) return meaningful.first()

        // Second fallback: closest asteroid within next 7 days
        val next7 = list.filter { isWithinNext7Days(it.approachDate) }
        if (next7.isNotEmpty()) {
            return next7.minByOrNull { it.missDistanceAu }
        }

        // Last fallback: closest in entire list
        return list.minByOrNull { it.missDistanceAu }
    }
}
