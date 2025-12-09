package com.example.celestia.data.network

import com.example.celestia.data.model.KpReading
import retrofit2.http.GET

/**
 * Retrofit API interface for retrieving Kp Index data from NOAA’s
 * Space Weather Prediction Center (SWPC).
 *
 * This endpoint provides minute-resolution geomagnetic activity readings,
 * commonly used to monitor space weather and aurora potential.
 *
 * The response is a list of Kp readings, each containing:
 * - Timestamp
 * - Raw Kp Index value
 * - Estimated Kp value
 * - Textual status label
 */
interface NoaaApi {

    /**
     * Fetches the planetary Kp Index readings.
     *
     * Endpoint: `planetary_k_index_1m.json`
     *
     * @return A list of [KpReading] objects sorted by timestamp.
     */
    @GET("planetary_k_index_1m.json")
    suspend fun getKpIndex(): List<KpReading>
}
