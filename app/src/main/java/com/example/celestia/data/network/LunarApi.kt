package com.example.celestia.data.network

import com.example.celestia.data.model.LunarPhase
import retrofit2.http.GET
import retrofit2.http.Query

interface LunarApi {
    // The GitHub API supports ?date=YYYY-MM-DD but defaults to current date
    @GET("api/v1/moon")
    suspend fun getLunarData(@Query("date") date: String? = null): LunarPhase
}
