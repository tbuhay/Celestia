package com.example.celestia.data.repository

import android.util.Log
import com.example.celestia.BuildConfig
import com.example.celestia.data.db.CelestiaDao
import com.example.celestia.data.model.IssReading
import com.example.celestia.data.model.KpReading
import com.example.celestia.data.model.LunarPhase
import com.example.celestia.data.network.RetrofitInstance
import com.example.celestia.utils.TimeUtils
import kotlinx.coroutines.flow.Flow

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
            Log.d("CelestiaRepo", "ISS data stored locally: ${reading.latitude}, ${reading.longitude}")

        } catch (e: Exception) {
            Log.e("CelestiaRepo", "Error refreshing ISS data", e)
        }
    }


    // -------------------------------------------------------------------------
    //  Lunar Phase API
    // -------------------------------------------------------------------------
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
}
