package com.example.celestia.data.network

import com.example.celestia.data.model.WikiSummary
import retrofit2.http.GET
import retrofit2.http.Path

interface WikipediaApi {

    @GET("page/summary/{title}")
    suspend fun getSummary(@Path("title") title: String): WikiSummary
}