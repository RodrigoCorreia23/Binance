package com.example.binance.network

import retrofit2.http.GET

interface ApiService {
    @GET("/api/ping")
    suspend fun ping(): PingResponse
}

data class PingResponse(
    val status: String
)
