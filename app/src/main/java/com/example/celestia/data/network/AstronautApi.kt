package com.example.celestia.data.network

import com.example.celestia.data.model.Astronaut
import retrofit2.http.GET

/**
 * Retrofit API for retrieving the list of astronauts currently in space.
 *
 * Uses the Open Notify public endpoint (`astros.json`) which provides:
 * - A status message
 * - The total number of astronauts in space
 * - A list of astronaut name/craft assignments
 *
 * This API does not require parameters or authentication.
 */
interface AstronautApi {

    /**
     * Fetches the current astronaut data from the Open Notify API.
     *
     * @return An [AstronautResponse] containing astronaut count and details.
     */
    @GET("astros.json")
    suspend fun getAstronauts(): AstronautResponse
}

/**
 * Response wrapper for astronaut data returned by the Open Notify API.
 *
 * @property message API status message (e.g., "success").
 * @property number Total number of astronauts in space at the time of request.
 * @property people List of astronauts (name + spacecraft assignment).
 */
data class AstronautResponse(
    val message: String,
    val number: Int,
    val people: List<Astronaut>
)