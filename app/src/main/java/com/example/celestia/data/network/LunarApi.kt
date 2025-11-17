package com.example.celestia.data.network

import com.example.celestia.data.model.LunarPhase
import retrofit2.http.GET
import retrofit2.http.Query

interface LunarApi {

    @GET("astronomy")
    suspend fun getLunarPhase(
        @Query("apiKey") apiKey: String,
        @Query("lat") latitude: Double,
        @Query("long") longitude: Double
    ): LunarPhase
}
