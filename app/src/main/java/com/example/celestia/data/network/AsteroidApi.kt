package com.example.celestia.data.network

import com.example.celestia.data.model.AsteroidFeedResponse
import retrofit2.http.GET
import retrofit2.http.Query


/**
 * Retrofit service interface for accessing NASA's Near-Earth Object (NEO) Feed API.
 *
 * This API returns a list of asteroids making close approaches to Earth
 * within a specified date range. NASA requires both a start and end date,
 * typically covering 1–7 days.
 *
 * The response is delivered as an [AsteroidFeedResponse], which contains
 * a map of date → list of asteroid data entries.
 */
interface AsteroidApi {

    /**
     * Fetches the daily NEO feed for a given date range.
     *
     * @param startDate The start of the query window (YYYY-MM-DD).
     * @param endDate The end of the query window (YYYY-MM-DD).
     * @param apiKey NASA API key for authentication.
     *
     * @return An [AsteroidFeedResponse] containing asteroid approach data
     *         grouped by date.
     */
    @GET("feed")
    suspend fun getDailyFeed(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String
    ): AsteroidFeedResponse
}
