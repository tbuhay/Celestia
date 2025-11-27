package com.example.celestia.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.celestia.BuildConfig
import com.example.celestia.data.db.CelestiaDao
import com.example.celestia.data.model.*
import com.example.celestia.data.network.AstronautResponse
import com.example.celestia.data.network.RetrofitInstance
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository for Celestia — handles network fetching, Room persistence,
 * and unified error handling. ViewModels should only call into this class.
 */
class CelestiaRepository(private val dao: CelestiaDao) {

    // -------------------------------------------------------------------------
    // NOAA (Kp Index)
    // -------------------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshKpIndex() {
        try {
            val newData = RetrofitInstance.noaaApi.getKpIndex()
            Log.d("CelestiaRepo.KP", "Fetched ${newData.size} Kp readings")

            // 1️. Calculate cutoff timestamp (24 hours ago)
            val now = java.time.LocalDateTime.now()
            val cutoff = now.minusHours(24)
            val cutoffString = cutoff.toString()  // exactly "YYYY-MM-DDTHH:mm:ss"

            // 2️. Delete rows older than 24 hours
            dao.deleteOlderThan(cutoffString)

            // 3️. Insert new ones without overwriting existing rows
            dao.insertOrIgnore(newData)

        } catch (e: Exception) {
            Log.e("CelestiaRepo.KP", "Error refreshing Kp Index", e)
        }
    }

    // -------------------------------------------------------------------------
    // ISS (Live Position)
    // -------------------------------------------------------------------------
    val issReading: Flow<IssReading?> = dao.getIssReading()

    suspend fun refreshIssData() {
        try {
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

        } catch (e: Exception) {
            Log.e("CelestiaRepo.ISS", "Error refreshing ISS data", e)
        }
    }

    // -------------------------------------------------------------------------
    // Astronauts API
    // -------------------------------------------------------------------------

    suspend fun getAstronautsRaw(): AstronautResponse =
        RetrofitInstance.astronautApi.getAstronauts()

    // -------------------------------------------------------------------------
    // Lunar Phase API
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
    // NASA NEO (Asteroids)
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

                result.add(
                    AsteroidApproach(
                        id = id,
                        name = neo.name,
                        approachDate = date,
                        missDistanceKm = missKm,
                        missDistanceAu = missAu,
                        relativeVelocityKph = velKph,
                        diameterMinMeters = diameter.min,
                        diameterMaxMeters = diameter.max,
                        isPotentiallyHazardous = neo.isHazardous
                    )
                )
            }
        }

        return result.sortedBy { it.approachDate }
    }
}
