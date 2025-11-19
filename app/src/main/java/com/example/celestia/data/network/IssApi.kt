package com.example.celestia.data.network

import com.example.celestia.data.model.Astronaut
import com.example.celestia.data.model.IssReading
import retrofit2.http.GET

interface IssApi {
    @GET("v1/satellites/25544")
    suspend fun getIssPosition(): IssReading
    val people: List<Astronaut>
}