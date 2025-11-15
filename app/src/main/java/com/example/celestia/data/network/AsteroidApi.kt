package com.example.celestia.data.network

import com.example.celestia.data.model.AsteroidFeedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AsteroidApi {

    // GET /neo/rest/v1/feed?start_date=YYYY-MM-DD&end_date=YYYY-MM-DD&api_key=YOUR_KEY
    @GET("feed")
    suspend fun getDailyFeed(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String
    ): AsteroidFeedResponse
}
