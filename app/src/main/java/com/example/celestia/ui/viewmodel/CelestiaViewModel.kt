package com.example.celestia.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.celestia.R
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.model.KpHourlyGroup
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.LunarPhase
import com.example.celestia.data.repository.CelestiaRepository
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    // -------------------------------------------------------------------------
    // Lunar Phase
    // -------------------------------------------------------------------------
    private val _lunarPhase = MutableLiveData<LunarPhase?>()
    val lunarPhase: LiveData<LunarPhase?> = _lunarPhase

    private val _isLunarLoading = MutableLiveData<Boolean>()
    val isLunarLoading: LiveData<Boolean> = _isLunarLoading

    private val _lunarError = MutableLiveData<String?>()
    val lunarError: LiveData<String?> = _lunarError

    private val defaultLat = 49.8951       // Winnipeg
    private val defaultLon = -97.1384

    init {
        _lastUpdated.value = prefs.getString("last_updated", "Never")
    }

    // -------------------------------------------------------------------------
    // REFRESH ALL DATA
    // -------------------------------------------------------------------------
    fun refresh() {
        viewModelScope.launch {
            try {
                launch {
                    try {
                        repo.refreshData()
                        Log.d("CelestiaVM", "NOAA data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "NOAA refresh failed", e)
                    }
                }

                launch {
                    try {
                        repo.refreshIssData()
                        Log.d("CelestiaVM", "ISS data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "ISS refresh failed", e)
                    }
                }

                launch {
                    try {
                        val lunar = repo.fetchLunarPhase(defaultLat, defaultLon)
                        _lunarPhase.postValue(lunar)
                        Log.d("CelestiaVM", "Lunar data refreshed")
                    } catch (e: Exception) {
                        _lunarError.postValue("Lunar refresh failed")
                        Log.e("CelestiaVM", "Lunar refresh failed", e)
                    }
                }

                val now = TimeUtils.format(System.currentTimeMillis().toString())
                prefs.edit().putString("last_updated", now).apply()
                _lastUpdated.postValue(now)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Error during refresh()", e)
            }
        }
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

    fun parseIlluminationPercent(lunar: LunarPhase?): Double {
        if (lunar == null) return 0.0
        return lunar.moonIlluminationPercentage
            .replace("%", "")
            .trim()
            .toDoubleOrNull()
            ?.let { abs(it) }
            ?: 0.0
    }

    fun computeMoonAgeDays(lunar: LunarPhase?): Double {
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
                val result = repo.fetchLunarPhase(latitude, longitude)
                _lunarPhase.postValue(result)
            } catch (e: Exception) {
                _lunarError.postValue("Unable to load lunar data")
                Log.e("CelestiaVM", "loadLunarPhase failed", e)
            } finally {
                _isLunarLoading.postValue(false)
            }
        }
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
                val date = sdf.parse(reading.timestamp)
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

    fun formatKpValue(value: Double, decimals: Int = 1): String {
        return try {
            if (value.isNaN()) return "N/A"
            String.format("%.${decimals}f", value)
                .trimEnd('0')
                .trimEnd('.')
        } catch (e: Exception) {
            value.toString()
        }
    }
}
