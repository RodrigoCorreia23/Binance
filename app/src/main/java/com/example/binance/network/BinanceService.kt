package com.example.binance.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

import com.example.binance.dto.BalanceResponse
import com.example.binance.models.BotTrade
import com.example.binance.models.Trade
import com.example.binance.models.UserProfile
import java.util.UUID

// --- DTOs de request/response para login/signup/ping/etc:
data class HasCredResponse(val hasCredentials: Boolean)
data class UpdateProfileRequest(val username: String, val email: String)

// Aqui assumimos que você já tem os data classes:
//   SignUpRequest, SignUpResponse, LoginRequest, LoginResponse, PingResponse, ApiCredentialsRequest

interface BinanceService {

    @GET("/api/ping")
    suspend fun ping(): PingResponse

    @POST("/api/users/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<SignUpResponse>

    @POST("/api/auth/login")
    suspend fun login(
        @Body req: LoginRequest
    ): Response<LoginResponse>

    @GET("/api/credentials/{userId}")
    suspend fun hasCredentials(
        @Path("userId") userId: String
    ): Response<HasCredResponse>

    @POST("/api/credentials")
    suspend fun saveCredentials(
        @Body req: ApiCredentialsRequest
    ): Response<Void>

    // Atenção: este endpoint retorna diretamente um BalanceResponse (não está dentro de Response<…>).
    @GET("/api/credentials/{userId}/balance")
    suspend fun getBalance(
        @Path("userId") userId: String
    ): BalanceResponse

    // Estes dois endpoints usam Call<> em vez de suspend fun:
    @GET("/api/users/{id}")
    fun getUserProfile(
        @Path("id") userId: String,
        @Header("Authorization") authHeader: String? = null
    ): Call<UserProfile>

    @PUT("/api/users/{id}")
    fun updateUserProfile(
        @Path("id") userId: String,
        @Header("Authorization") authHeader: String? = null,
        @Body request: UpdateProfileRequest
    ): Call<UserProfile>

    @GET("/api/bot-trades/user/{userId}")
    suspend fun getBotTrades(
        @Path("userId") userId: String
    ): List<BotTrade>

    @GET("api/bot-trades/user/{userId}")
    suspend fun getTrades(@Path("userId") userId: UUID): List<BotTrade>

}
