package com.example.celestia.data.network

import com.example.celestia.data.model.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,cloud_cover,weather_code"
    ): OpenMeteoResponse
}