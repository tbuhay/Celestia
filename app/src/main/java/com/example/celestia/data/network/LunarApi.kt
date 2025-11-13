package com.example.celestia.data.network

import com.example.celestia.data.model.LunarPhase
import retrofit2.http.GET
import retrofit2.http.Query

interface LunarApi {

    // GET https://api.ipgeolocation.io/astronomy?apiKey=YOUR_KEY&lat=49.8951&long=-97.1384
    @GET("astronomy")
    suspend fun getLunarPhase(
        @Query("apiKey") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("long") longitude: Double
    ): LunarPhase
}
