package com.example.celestia.data.repository

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.celestia.BuildConfig
import com.example.celestia.data.db.CelestiaDao
import com.example.celestia.data.model.*
import com.example.celestia.data.network.AstronautResponse
import com.example.celestia.utils.AsteroidHelper
import com.example.celestia.data.network.RetrofitInstance
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for Celestia — acts as the single source of truth for all data.
 *
 * Responsibilities:
 * - Fetch remote data from Retrofit API clients
 * - Store and retrieve data using Room (CelestiaDao)
 * - Apply conversion/mapping from API DTOs → Room entities
 * - Provide automatic Flow-based updates to ViewModels
 * - Handle caching, error logging, and fallback logic
 *
 * ViewModels should depend on this class rather than calling APIs or DAOs directly.
 */
class CelestiaRepository(
    private val dao: CelestiaDao,
    private val context: Context
) {

    // -------------------------------------------------------------------------
    // NOAA (Kp Index)
    // -------------------------------------------------------------------------

    /** A Flow of all stored Kp Index readings from the database. */
    val readings: Flow<List<KpReading>> = dao.getAll()

    /**
     * Fetches Kp Index readings from NOAA and stores them in Room.
     *
     * Errors are logged but not thrown to ensure UI stability.
     */
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
    // ISS (Live Position)
    // -------------------------------------------------------------------------

    /** A Flow containing the latest ISS reading from Room (or null if none). */
    val issReading: Flow<IssReading?> = dao.getIssReading()

    /**
     * Fetches real-time ISS orbital data from where-the-ISS-at API
     * and stores a normalized [IssReading] entity in Room.
     */
    suspend fun refreshIssData() {
        try {
            val response = RetrofitInstance.issApi.getIssPosition()

            val reading = IssReading(
                id = 1, // always override the single entry
                latitude = response.latitude,
                longitude = response.longitude,
                altitude = response.altitude,
                velocity = response.velocity,
                timestamp = TimeUtils.format(System.currentTimeMillis().toString())
            )

            dao.insertIssReading(reading)
            Log.d("CelestiaRepo.ISS", "ISS updated: lat=${reading.latitude}, lon=${reading.longitude}")
        } catch (e: Exception) {
            Log.e("CelestiaRepo.ISS", "Error refreshing ISS data", e)
        }
    }

    // -------------------------------------------------------------------------
    // Astronaut API (cached every 12 hours)
    // -------------------------------------------------------------------------

    private val prefs = context.applicationContext.getSharedPreferences("celestia_prefs", 0)

    /**
     * Returns the cached astronaut count if it's less than 12 hours old.
     * If expired, fetches the count from the API and updates the cache.
     *
     * @return The number of astronauts currently in space.
     */
    suspend fun getCachedAstronautCount(): Int {
        val lastRefresh = prefs.getLong("astronauts_last_refresh", 0L)
        val lastCount = prefs.getInt("astronauts_last_count", -1)
        val now = System.currentTimeMillis()

        val twelveHours = 12 * 60 * 60 * 1000L

        return if (now - lastRefresh < twelveHours && lastCount != -1) {
            lastCount // cached value still valid
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

    /**
     * Fetches raw astronaut API response without caching.
     */
    suspend fun getAstronautsRaw(): AstronautResponse =
        RetrofitInstance.astronautApi.getAstronauts()

    // -------------------------------------------------------------------------
    // Lunar Phase (IPGeolocation)
    // -------------------------------------------------------------------------

    /** Flow of the most recent lunar phase entity from Room. */
    val lunarPhase: Flow<LunarPhaseEntity?> = dao.getLunarPhase()

    /**
     * Fetches lunar phase data for the given coordinates and stores
     * a normalized [LunarPhaseEntity] in Room.
     */
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

    /** Converts [LunarPhase] DTO into a Room [LunarPhaseEntity]. */
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
    // NASA NEO (Asteroid Approaches)
    // -------------------------------------------------------------------------

    /** Flow containing the next upcoming asteroid approach. */
    val nextAsteroid: Flow<AsteroidApproach?> = dao.getNextAsteroid()

    /** Flow containing all stored asteroid approaches. */
    val allAsteroids: Flow<List<AsteroidApproach>> = dao.getAllAsteroids()

    /**
     * Fetches upcoming asteroid approaches for the next 7 days and replaces
     * all stored asteroid data in Room.
     */
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

    /**
     * Converts a NASA [AsteroidFeedResponse] into a list of Room entities.
     *
     * - Extracts close-approach data
     * - Parses string values (velocity, distance)
     * - Combines NEO ID + date to create a unique primary key
     */
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

                // Build asteroid object first with placeholder hazard flag
                val asteroid = AsteroidApproach(
                    id = id,
                    name = neo.name,
                    approachDate = date,
                    missDistanceKm = missKm,
                    missDistanceAu = missAu,
                    relativeVelocityKph = velKph,
                    diameterMinMeters = diameter.min,
                    diameterMaxMeters = diameter.max,
                    isPotentiallyHazardous = false // overwritten next
                )

                // Compute true hazard classification using your rules
                val hazardous = AsteroidHelper.isPotentiallyHazardous(asteroid)

                // Store asteroid with corrected hazard flag
                result.add(
                    asteroid.copy(
                        isPotentiallyHazardous = hazardous
                    )
                )
            }
        }

        return result.sortedBy { it.approachDate }
    }
}