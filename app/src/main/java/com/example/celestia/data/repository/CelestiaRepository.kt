package com.example.celestia.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.celestia.BuildConfig
import com.example.celestia.data.model.ObservationEntry
import com.example.celestia.data.db.CelestiaDao
import com.example.celestia.data.model.*
import com.example.celestia.data.network.AstronautResponse
import com.example.celestia.data.network.RetrofitInstance
import com.example.celestia.utils.AsteroidHelper
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import com.example.celestia.data.model.OpenMeteoResponse
import com.example.celestia.data.model.WeatherSnapshot

/**
 * Repository for Celestia — acts as the single source of truth for all data:
 *  - Fetch remote data using Retrofit
 *  - Persist normalized data into Room
 *  - Expose Flows for ViewModels
 *  - Provide caching, mapping, error-handling
 */
class CelestiaRepository(
    private val dao: CelestiaDao,
    private val context: Context
) {

    // -------------------------------------------------------------------------
    // NOAA — KP INDEX
    // -------------------------------------------------------------------------
    val readings: Flow<List<KpReading>> = dao.getAll()

    suspend fun refreshKpIndex() {
        try {
            val newData = RetrofitInstance.noaaApi.getKpIndex()
            Log.d("CelestiaRepo.KP", "Fetched ${newData.size} Kp readings")
            dao.insertAll(newData)
        } catch (e: Exception) {
            Log.e("CelestiaRepo.KP", "Error refreshing Kp Index", e)
        }
    }

    // -------------------------------------------------------------------------
    // ISS — LIVE POSITION (for UI + Worker)
    // -------------------------------------------------------------------------

    /** Latest ISS reading from the database (single row). */
    val issReading: Flow<IssReading?> = dao.getIssReading()

    /**
     * Fetches real-time ISS orbital data and stores a normalized IssReading record.
     * This is used BOTH for the UI AND for background alerts.
     */
    suspend fun refreshIssLocation(): IssReading? {
        return try {
            val response = RetrofitInstance.issApi.getIssPosition()

            val reading = IssReading(
                id = 1,
                latitude = response.latitude,
                longitude = response.longitude,
                altitude = response.altitude,
                velocity = response.velocity,
                timestamp = TimeUtils.format(System.currentTimeMillis().toString())
            )

            dao.insertIssReading(reading)

            Log.d("CelestiaRepo.ISS", "ISS updated: lat=${reading.latitude}, lon=${reading.longitude}")

            reading

        } catch (e: Exception) {
            Log.e("CelestiaRepo.ISS", "Error refreshing ISS location", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // ASTRONAUT API — CACHED FOR 12 HOURS
    // -------------------------------------------------------------------------
    private val prefs =
        context.applicationContext.getSharedPreferences("celestia_prefs", 0)

    suspend fun getCachedAstronautCount(): Int {
        val lastRefresh = prefs.getLong("astronauts_last_refresh", 0L)
        val lastCount = prefs.getInt("astronauts_last_count", -1)
        val now = System.currentTimeMillis()
        val twelveHours = 12 * 60 * 60 * 1000L

        return if (now - lastRefresh < twelveHours && lastCount != -1) {
            lastCount
        } else {
            val response = RetrofitInstance.astronautApi.getAstronauts()
            val newCount = response.people.size

            prefs.edit()
                .putInt("astronauts_last_count", newCount)
                .putLong("astronauts_last_refresh", now)
                .apply()

            newCount
        }
    }

    suspend fun getAstronautsRaw(): AstronautResponse =
        RetrofitInstance.astronautApi.getAstronauts()

    // -------------------------------------------------------------------------
    // LUNAR PHASE — IPGELOCATION
    // -------------------------------------------------------------------------
    val lunarPhase: Flow<LunarPhaseEntity?> = dao.getLunarPhase()

    suspend fun refreshLunarPhase(lat: Double, lon: Double) {
        try {
            val lunar = RetrofitInstance.lunarApi.getLunarPhase(
                apiKey = BuildConfig.IPGEO_API_KEY,
                latitude = lat,
                longitude = lon
            )

            val updatedAt = TimeUtils.format(System.currentTimeMillis().toString())

            dao.insertLunarPhase(mapLunarToEntity(lunar, updatedAt))

            Log.d("CelestiaRepo.Lunar", "Lunar phase updated successfully")
        } catch (e: Exception) {
            Log.e("CelestiaRepo.Lunar", "Error refreshing lunar data", e)
        }
    }

    private fun mapLunarToEntity(lunar: LunarPhase, updatedAt: String): LunarPhaseEntity {
        return LunarPhaseEntity(
            id = 1,
            moonPhase = lunar.moonPhase,
            illuminationPercent = lunar.moonIlluminationPercentage,
            moonRise = lunar.moonrise,
            moonSet = lunar.moonset,
            moonDistanceKm = lunar.moonDistance,
            updatedAt = updatedAt
        )
    }

    // -------------------------------------------------------------------------
    // NASA NEO — ASTEROIDS
    // -------------------------------------------------------------------------
    val nextAsteroid: Flow<AsteroidApproach?> = dao.getNextAsteroid()
    val allAsteroids: Flow<List<AsteroidApproach>> = dao.getAllAsteroids()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAsteroids() {
        try {
            val today = LocalDate.now()
            val feed = RetrofitInstance.asteroidApi.getDailyFeed(
                startDate = today.toString(),
                endDate = today.plusDays(7).toString(),
                apiKey = BuildConfig.NASA_API_KEY
            )

            val asteroids = mapFeedToEntities(feed)

            dao.clearAsteroids()
            dao.insertAsteroids(asteroids)

            Log.d("CelestiaRepo.Asteroids", "Stored ${asteroids.size} asteroid entries")

        } catch (e: Exception) {
            Log.e("CelestiaRepo.Asteroids", "Error refreshing asteroid data", e)
        }
    }

    private fun mapFeedToEntities(feed: AsteroidFeedResponse): List<AsteroidApproach> {
        val result = mutableListOf<AsteroidApproach>()

        feed.nearEarthObjects.forEach { (date, objects) ->
            objects.forEach { neo ->
                val approach = neo.closeApproachData.firstOrNull() ?: return@forEach

                val diameter = neo.estimatedDiameter.meters
                val missKm = approach.missDistance.km.toDoubleOrNull() ?: 0.0
                val missAu = approach.missDistance.au.toDoubleOrNull() ?: 0.0
                val velKph = approach.velocity.kph.toDoubleOrNull() ?: 0.0

                val id = "${neo.id}_$date"

                val asteroid = AsteroidApproach(
                    id = id,
                    name = neo.name,
                    approachDate = date,
                    missDistanceKm = missKm,
                    missDistanceAu = missAu,
                    relativeVelocityKph = velKph,
                    diameterMinMeters = diameter.min,
                    diameterMaxMeters = diameter.max,
                    isPotentiallyHazardous = false
                )

                val hazardous = AsteroidHelper.isPotentiallyHazardous(asteroid)

                result.add(
                    asteroid.copy(isPotentiallyHazardous = hazardous)
                )
            }
        }

        return result.sortedBy { it.approachDate }
    }

    // -------------------------------------------------------------------------
    // WEATHER
    // -------------------------------------------------------------------------

    private fun mapWeatherCodeToSummary(code: Int?): String? {
        if (code == null) return null

        return when (code) {
            0 -> "Clear"
            1, 2 -> "Mostly clear"
            3 -> "Overcast"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            71, 73, 75, 77 -> "Snow"
            80, 81, 82 -> "Rain showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Unspecified"
        }
    }

    suspend fun fetchCurrentWeather(lat: Double, lon: Double): WeatherSnapshot? {
        return try {
            val response = RetrofitInstance.weatherApi.getCurrentWeather(
                latitude = lat,
                longitude = lon
            )

            val current = response.current

            if (current == null) {
                null
            } else {
                WeatherSnapshot(
                    temperatureC = current.temperatureC,
                    cloudCoverPercent = current.cloudCoverPercent,
                    weatherSummary = mapWeatherCodeToSummary(current.weatherCode)
                )
            }
        } catch (e: Exception) {
            Log.e("CelestiaRepo.Weather", "Error fetching weather", e)
            null
        }
    }

    // -------------------------------------------------------------------------
    // JOURNAL
    // -------------------------------------------------------------------------

    fun getAllObservations(): Flow<List<ObservationEntry>> =
        dao.getAllObservations()

    suspend fun getObservationById(id: Int): ObservationEntry? =
        dao.getObservationById(id)

    suspend fun saveObservation(entry: ObservationEntry) {
        dao.insertObservation(entry)
    }

    suspend fun deleteObservation(entry: ObservationEntry) {
        dao.deleteObservation(entry)
    }
}
