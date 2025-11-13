package com.example.celestia.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val NOAA_BASE_URL = "https://services.swpc.noaa.gov/json/"
    private const val ISS_BASE_URL = "https://api.wheretheiss.at/"
    private const val IPGEO_BASE_URL = "https://api.ipgeolocation.io/"

    val noaaApi: NoaaApi by lazy {
        Retrofit.Builder()
            .baseUrl(NOAA_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NoaaApi::class.java)
    }

    val issApi: IssApi by lazy {
        Retrofit.Builder()
            .baseUrl(ISS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IssApi::class.java)
    }

    val lunarApi: LunarApi by lazy {
        Retrofit.Builder()
            .baseUrl(IPGEO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LunarApi::class.java)
    }
}
