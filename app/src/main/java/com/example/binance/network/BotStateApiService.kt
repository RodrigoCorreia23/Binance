package com.example.binance.network

import retrofit2.Call
import retrofit2.http.*
import com.example.binance.network.BotStateRequest
import com.example.binance.network.BotStateResponse

interface BotStateApiService {

    @GET("/api/bot/{userId}/status")
    fun getBotState(@Path("userId") userId: String): Call<BotStateResponse>

    @POST("/api/bot/{userId}/activate")
    fun activateBot(@Path("userId") userId: String): Call<Void>

    @POST("/api/bot/{userId}/deactivate")
    fun deactivateBot(@Path("userId") userId: String): Call<Void>

    // Adiciona este método:
    @POST("/api/bot-state")
    fun createBotState(@Body request: BotStateRequest): Call<BotStateResponse>
}
