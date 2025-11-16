package com.example.celestia.data.network

import com.example.celestia.data.model.Astronaut
import retrofit2.http.GET

interface AstronautApi {
    @GET("astros.json")
    suspend fun getAstronauts(): AstronautResponse
}



data class AstronautResponse(
    val message: String,
    val number: Int,
    val people: List<Astronaut>
)