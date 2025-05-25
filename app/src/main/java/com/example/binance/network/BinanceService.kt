package com.example.binance.network

import retrofit2.http.GET

data class PingResponse(val message: String)

// serviço Retrofit
interface BinanceService {
    @GET("api/ping")
    suspend fun ping(): PingResponse
}