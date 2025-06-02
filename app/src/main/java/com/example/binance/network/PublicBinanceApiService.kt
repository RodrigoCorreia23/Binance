package com.example.binance.network

import retrofit2.http.GET
import retrofit2.http.Query

data class SymbolPrice(val symbol: String, val price: String)

interface PublicBinanceApiService {
    @GET("api/v3/ticker/price")
    suspend fun getAllSymbols(): List<SymbolPrice>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "5m",
        @Query("limit") limit: Int = 100
    ): List<List<Any>>
}
