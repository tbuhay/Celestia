package com.example.celestia.data.network

import com.example.celestia.data.model.LunarPhase
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for retrieving lunar phase information based on
 * geographic coordinates.
 *
 * This endpoint returns data such as:
 * - Current moon phase name
 * - Illumination percentage
 * - Moonrise and moonset times
 * - Lunar distance from Earth
 *
 * The API requires an API key along with latitude and longitude parameters.
 */
interface LunarApi {

    /**
     * Fetches the current lunar phase data for the given coordinates.
     *
     * @param apiKey API key used for authentication with the lunar/astronomy service.
     * @param latitude Geographic latitude of the user/device.
     * @param longitude Geographic longitude of the user/device.
     *
     * @return A [LunarPhase] object containing astronomical moon data.
     */
    @GET("astronomy")
    suspend fun getLunarPhase(
        @Query("apiKey") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("long") longitude: Double
    ): LunarPhase
}
