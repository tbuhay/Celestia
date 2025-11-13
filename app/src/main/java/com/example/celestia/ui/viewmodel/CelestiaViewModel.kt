package com.example.celestia.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.celestia.R
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.repository.CelestiaRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.celestia.data.model.LunarPhase
import kotlin.math.abs

class CelestiaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = CelestiaDatabase.getInstance(application).dao()
    private val repo = CelestiaRepository(dao)
    private val prefs = application.getSharedPreferences("celestia_prefs", 0)

    // --- NOAA: Kp Index ---
    val readings = repo.readings.asLiveData()
    private val _lastUpdated = MutableLiveData<String>()
    val lastUpdated: LiveData<String> = _lastUpdated

    // --- ISS: Live Position (persisted in Room) ---
    val issReading = repo.issReading.asLiveData()

    // --- Lunar Phase ---
    private val _lunarPhase = MutableLiveData<LunarPhase?>()
    val lunarPhase: LiveData<LunarPhase?> = _lunarPhase

    private val _isLunarLoading = MutableLiveData<Boolean>()
    val isLunarLoading: LiveData<Boolean> = _isLunarLoading

    private val _lunarError = MutableLiveData<String?>()
    val lunarError: LiveData<String?> = _lunarError

    init {
        _lastUpdated.value = prefs.getString("last_updated", "Never")
    }

    private fun currentLocalTime(): String {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(Date())
    }

    /** Refresh both NOAA + ISS data */
    fun refresh() {
        viewModelScope.launch {
            try {
                // --- NOAA ---
                launch {
                    try {
                        repo.refreshData()
                        Log.d("CelestiaVM", "NOAA data refreshed")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "NOAA refresh failed", e)
                    }
                }

                // --- ISS ---
                launch {
                    try {
                        repo.refreshIssData()
                        Log.d("CelestiaVM", "ISS data refreshed via repository")
                    } catch (e: Exception) {
                        Log.e("CelestiaVM", "ISS refresh failed", e)
                    }
                }

                launch {
                    try {
                        // Fetch the live lunar data from API
                        val lunar = repo.fetchLunarPhase(
                            latitude = defaultLat,
                            longitude = defaultLon
                        )

                        // Push it into LiveData
                        _lunarPhase.postValue(lunar)

                        Log.d("CelestiaVM", "Lunar data refreshed")
                    } catch (e: Exception) {
                        _lunarError.postValue("Lunar refresh failed")
                        Log.e("CelestiaVM", "Lunar refresh failed", e)
                    }
                }

                // --- Shared timestamp (applied last) ---
                val formattedTime = currentLocalTime()
                prefs.edit().putString("last_updated", formattedTime).apply()
                _lastUpdated.postValue(formattedTime)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Error during refresh()", e)
            }
        }
    }

    /** NOAA formatting helpers */
    fun formatKpTimestamp(utcString: String): String {
        return try {
            // NOAA sometimes omits 'Z' — handle both cases safely
            val cleaned = utcString.trim()

            val parser = if (cleaned.endsWith("Z")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            } else {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            }

            parser.timeZone = TimeZone.getTimeZone("UTC") // always parse as UTC

            val date = parser.parse(cleaned)

            val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
            formatter.timeZone = TimeZone.getDefault() // convert to device local (Winnipeg)
            formatter.format(date!!)
        } catch (e: Exception) {
            // fallback — just return original UTC string
            utcString
        }
    }

    fun formatKpValue(kp: Double, decimals: Int = 2): String {
        return String.format(Locale.US, "%.${decimals}f", kp)
    }

    fun groupKpReadingsHourly(readings: List<com.example.celestia.data.model.KpReading>): List<Triple<Date, Float, Float>> {
        if (readings.isEmpty()) return emptyList()

        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        // Group by truncated-to-hour timestamps
        val grouped = readings.mapNotNull { r ->
            try {
                val date = parser.parse(r.timestamp)
                if (date != null) Pair(date, r.estimatedKp.toFloat()) else null
            } catch (_: Exception) {
                null
            }
        }.groupBy { (date, _) ->
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { time = date }
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.time
        }

        // Compute average per hour group
        return grouped.entries.sortedByDescending { it.key }.map { (hourStart, values) ->
            val list = values.map { it.second }
            val avg = list.average().toFloat()
            val high = list.maxOrNull() ?: avg
            val low = list.minOrNull() ?: avg
            Triple(hourStart, avg, high.coerceAtLeast(low))
        }
    }

    fun formatMoonPhaseName(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown Phase"
        return raw
            .lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

    fun parseIlluminationPercent(lunar: LunarPhase?): Double {
        if (lunar == null) return 0.0
        val raw = lunar.moonIlluminationPercentage
            .replace("%", "")
            .trim()
        val value = raw.toDoubleOrNull() ?: 0.0
        return abs(value) // negative means waning in this API
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

    fun isWaxing(phase: String?): Boolean {
        return when (phase?.uppercase()) {
            "WAXING_CRESCENT",
            "FIRST_QUARTER",
            "WAXING_GIBBOUS" -> true

            "FULL_MOON" -> true     // peak light

            "WANING_GIBBOUS",
            "LAST_QUARTER",
            "WANING_CRESCENT",
            "NEW_MOON" -> false

            else -> true
        }
    }
    private val defaultLat = 49.8951   // Winnipeg
    private val defaultLon = -97.1384

    fun loadLunarPhase(
        latitude: Double = defaultLat,
        longitude: Double = defaultLon
    ) {
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
    fun formatLunarTimestamp(date: String, time: String): String {
        return try {
            // date: "2025-11-12"
            // time: "19:04:19.662"  (already local for your location)

            // Parse just the date so we can format "Nov 12"
            val dayParser = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val dateObj = dayParser.parse(date)

            val dayFormatter = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)
            val dayText = dayFormatter.format(dateObj!!)

            // Take only HH:mm from "19:04:19.662"
            val timeText = time.take(5)  // "19:04"

            "$dayText, $timeText"
        } catch (e: Exception) {
            // Fallback – still readable
            "${date} ${time.take(5)}"
        }
    }


}
