package com.example.celestia.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.celestia.BuildConfig
import com.example.celestia.data.db.CelestiaDao
import com.example.celestia.data.model.IssReading
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.LunarPhase
import com.example.celestia.data.network.RetrofitInstance
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.data.model.AsteroidFeedResponse
import com.example.celestia.data.model.Astronaut
import com.example.celestia.data.model.LunarPhaseEntity
import com.example.celestia.data.network.AstronautResponse
import java.time.LocalDate

class CelestiaRepository(private val dao: CelestiaDao) {

    // -------------------------------------------------------------------------
    //  NOAA (Kp Index)
    // -------------------------------------------------------------------------
    val readings: Flow<List<KpReading>> = dao.getAll()

    suspend fun refreshData() {
        try {
            val newData = RetrofitInstance.noaaApi.getKpIndex()
            dao.insertAll(newData)
            Log.d("CelestiaRepo", "Inserted ${newData.size} Kp readings")
        } catch (e: Exception) {
            Log.e("CelestiaRepo", "Error fetching NOAA data", e)
        }
    }

    // -------------------------------------------------------------------------
    //  ISS (Live Position) — Persisted in Room
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
            Log.d(
                "CelestiaRepo",
                "ISS data stored locally: ${reading.latitude}, ${reading.longitude}"
            )

        } catch (e: Exception) {
            Log.e("CelestiaRepo", "Error refreshing ISS data", e)
        }
    }

    suspend fun loadAstronauts(): List<Astronaut> {
        return RetrofitInstance.astronautApi.getAstronauts().people
    }

    suspend fun loadAstronautCount(): Int {
        return RetrofitInstance.astronautApi.getAstronauts().number
    }

    suspend fun getAstronautsRaw(): AstronautResponse {
        return RetrofitInstance.astronautApi.getAstronauts()
    }


    // -------------------------------------------------------------------------
    //  Lunar Phase API
    // -------------------------------------------------------------------------
    val lunarPhase: Flow<LunarPhaseEntity?> = dao.getLunarPhase()

    suspend fun fetchLunarPhase(
        latitude: Double,
        longitude: Double
    ): LunarPhase {
        return RetrofitInstance.lunarApi.getLunarPhase(
            apiKey = BuildConfig.IPGEO_API_KEY,
            latitude = latitude,
            longitude = longitude
        )
    }

    suspend fun refreshLunarPhase(lat: Double, lon: Double) {
        val lunar = RetrofitInstance.lunarApi.getLunarPhase(
            apiKey = BuildConfig.IPGEO_API_KEY,
            latitude = lat,
            longitude = lon
        )

        val now = TimeUtils.format(System.currentTimeMillis().toString())

        dao.insertLunarPhase(mapToEntity(lunar, now))
    }

    private fun mapToEntity(lunar: LunarPhase, updatedAt: String): LunarPhaseEntity {
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
    //  Asteroids (NASA NEO)
    // -------------------------------------------------------------------------
    val nextAsteroid: Flow<AsteroidApproach?> = dao.getNextAsteroid()
    val allAsteroids: Flow<List<AsteroidApproach>> = dao.getAllAsteroids()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun refreshAsteroids() {
        try {
            val today = LocalDate.now()
            val startDate = today.toString()
            val endDate = today.plusDays(7).toString()

            val feed = RetrofitInstance.asteroidApi.getDailyFeed(
                startDate = startDate,
                endDate = endDate,
                apiKey = BuildConfig.NASA_API_KEY
            )

            val flattened = mapFeedToEntities(feed)

            dao.clearAsteroids()
            dao.insertAsteroids(flattened)

            Log.d("CelestiaRepo", "Inserted ${flattened.size} asteroid records")

        } catch (e: Exception) {
            Log.e("CelestiaRepo", "Error refreshing asteroid data", e)
        }
    }

    private fun mapFeedToEntities(feed: AsteroidFeedResponse): List<AsteroidApproach> {
        val result = mutableListOf<AsteroidApproach>()

        feed.nearEarthObjects.forEach { (date, list) ->
            list.forEach { neo ->

                val approach = neo.closeApproachData.firstOrNull() ?: return@forEach

                val diameter = neo.estimatedDiameter.meters
                val missKm = approach.missDistance.km.toDoubleOrNull() ?: 0.0
                val missAu = approach.missDistance.au.toDoubleOrNull() ?: 0.0
                val velKph = approach.velocity.kph.toDoubleOrNull() ?: 0.0

                val entityId = "${neo.id}_$date"

                result.add(
                    AsteroidApproach(
                        id = entityId,
                        name = neo.name,

                        // ✔ FIXED: Use FEED DATE, NOT DTO DATE
                        approachDate = date,

                        missDistanceKm = missKm,
                        missDistanceAu = missAu,
                        relativeVelocityKph = velKph,
                        diameterMinMeters = diameter.min,
                        diameterMaxMeters = diameter.max,

                        // Your testing logic; fine for now
                        isPotentiallyHazardous = neo.isHazardous
                    )
                )
            }
        }

        return result.sortedBy { it.approachDate }
    }
}
