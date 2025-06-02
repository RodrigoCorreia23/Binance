package com.example.binance.network

import com.example.binance.dto.BalanceResponse
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.*

data class HasCredResponse(val hasCredentials: Boolean)

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

    @GET("credentials/{userId}/balance")
    suspend fun getBalance(
        @Path("userId") userId: String
    ): BalanceResponse
}