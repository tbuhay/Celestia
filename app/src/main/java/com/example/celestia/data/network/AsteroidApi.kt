package com.example.celestia.data.network

import com.example.celestia.data.model.AsteroidFeedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AsteroidApi {

    @GET("feed")
    suspend fun getDailyFeed(
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("api_key") apiKey: String
    ): AsteroidFeedResponse
}
