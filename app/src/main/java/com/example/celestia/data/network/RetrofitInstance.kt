package com.example.celestia.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
 * - Open-Meteo (Weather)
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
    private const val WEATHER_URL = "https://api.open-meteo.com/"

    // -------------------------------------------------------------------------
    // NOAA Space Weather API (KP Index)
    // -------------------------------------------------------------------------
    val noaaApi: NoaaApi by lazy {
        Retrofit.Builder()
            .baseUrl(NOAA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoaaApi::class.java)
    }

    // -------------------------------------------------------------------------
    // ISS Position API
    // -------------------------------------------------------------------------
    val issApi: IssApi by lazy {
        Retrofit.Builder()
            .baseUrl(ISS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IssApi::class.java)
    }

    // -------------------------------------------------------------------------
    // Lunar Phase API (IPGeolocation)
    // -------------------------------------------------------------------------
    val lunarApi: LunarApi by lazy {
        Retrofit.Builder()
            .baseUrl(IPGEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LunarApi::class.java)
    }

    // -------------------------------------------------------------------------
    // NASA NEO API (Asteroids)
    // -------------------------------------------------------------------------
    val asteroidApi: AsteroidApi by lazy {
        Retrofit.Builder()
            .baseUrl(NASA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AsteroidApi::class.java)
    }

    // -------------------------------------------------------------------------
    // Astronauts in space (Open Notify)
    // -------------------------------------------------------------------------
    val astronautApi: AstronautApi by lazy {
        Retrofit.Builder()
            .baseUrl(ASTRONAUT_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AstronautApi::class.java)
    }

    // -------------------------------------------------------------------------
    // Open-Meteo Weather API (Weather for observation entries)
    // -------------------------------------------------------------------------
    val weatherApi: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}
