package com.example.binance.network

import retrofit2.http.GET

interface BinanceService {
    @GET("/api/ping")
    suspend fun ping(): PingResponse
}
