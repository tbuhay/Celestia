package com.example.celestia.data.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Centralized provider for all Retrofit API clients used by the Celestia app.
 *
 * This object creates and exposes lazily-initialized Retrofit service instances
 * for each external data source:
 *
 * - NOAA (Kp Index)
 * - where-the-ISS-at (ISS position)
 * - IPGeolocation (Lunar phase)
 * - NASA NEO API (Asteroid approaches)
 * - Open Notify (Astronauts in space)
 *
 * Each service is constructed with:
 * - Its own base URL
 * - A Gson converter for JSON parsing
 *
 * RetrofitInstance allows all ViewModels and the Repository to access
 * network clients without duplicating setup code.
 */
object RetrofitInstance {

    // Base URLs for each external API
    private const val NOAA_BASE_URL = "https://services.swpc.noaa.gov/json/"
    private const val ISS_BASE_URL = "https://api.wheretheiss.at/"
    private const val IPGEO_BASE_URL = "https://api.ipgeolocation.io/"
    private const val NASA_BASE_URL = "https://api.nasa.gov/neo/rest/v1/"
    private const val ASTRONAUT_URL = "http://api.open-notify.org/"

    /**
     * Retrofit client for NOAA Space Weather Prediction Center.
     * Provides planetary Kp Index readings.
     */
    val noaaApi: NoaaApi by lazy {
        Retrofit.Builder()
            .baseUrl(NOAA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoaaApi::class.java)
    }

    /**
     * Retrofit client for where-the-ISS-at API.
     * Provides real-time ISS orbital position and velocity.
     */
    val issApi: IssApi by lazy {
        Retrofit.Builder()
            .baseUrl(ISS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IssApi::class.java)
    }

    /**
     * Retrofit client for IPGeolocation astronomy API.
     * Provides lunar phase, illumination, and moonrise/moonset times.
     */
    val lunarApi: LunarApi by lazy {
        Retrofit.Builder()
            .baseUrl(IPGEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LunarApi::class.java)
    }

    private val longTimeoutClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val asteroidApi: AsteroidApi by lazy {
        Retrofit.Builder()
            .baseUrl(NASA_BASE_URL)
            .client(longTimeoutClient)   // ⭐ FIXED: Long timeout
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AsteroidApi::class.java)
    }


    /**
     * Retrofit client for the Open Notify API.
     * Provides a list of astronauts currently in space.
     */
    val astronautApi: AstronautApi by lazy {
        Retrofit.Builder()
            .baseUrl(ASTRONAUT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AstronautApi::class.java)
    }
}
