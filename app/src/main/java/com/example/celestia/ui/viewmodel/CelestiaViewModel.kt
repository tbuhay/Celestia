package com.example.celestia.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.repository.CelestiaRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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

                // --- Shared timestamp (applied last) ---
                val formattedTime = currentLocalTime()
                prefs.edit().putString("last_updated", formattedTime).apply()
                _lastUpdated.postValue(formattedTime)

            } catch (e: Exception) {
                Log.e("CelestiaVM", "Error during refresh()", e)
            }
        }
    }

    /** Refresh ISS only */
    fun refreshIssData() {
        viewModelScope.launch {
            try {
                repo.refreshIssData()
                Log.d("CelestiaVM", "ISS data refreshed (manual call)")
            } catch (e: Exception) {
                Log.e("CelestiaVM", "refreshIssData failed", e)
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
}
