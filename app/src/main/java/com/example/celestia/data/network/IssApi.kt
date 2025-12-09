package com.example.celestia.data.network

import com.example.celestia.data.model.IssReading
import retrofit2.http.GET

/**
 * Retrofit API interface for retrieving real-time positional data
 * for the International Space Station (ISS).
 *
 * This endpoint returns the current latitude, longitude, altitude,
 * velocity, and timestamp for satellite ID 25544.
 */
interface IssApi {

    /**
     * Fetches the ISS orbital position and motion data.
     *
     * @return An [IssReading] containing the current ISS telemetry.
     */
    @GET("v1/satellites/25544")
    suspend fun getIssPosition(): IssReading
}