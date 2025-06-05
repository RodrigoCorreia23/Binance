package com.example.binance.network

import retrofit2.Call
import com.example.binance.dto.BalanceResponse
import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.*
import com.example.binance.models.UserProfile


data class HasCredResponse(val hasCredentials: Boolean)
data class UpdateProfileRequest(val username: String, val email: String)

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

    @GET("api/users/{id}")
    fun getUserProfile(
        @Path("id") userId: String,
        // Se você usa token de autenticação, pode passar no header
        @Header("Authorization") authHeader: String? = null
    ): Call<UserProfile>

    @PUT("/api/users/{id}")
    fun updateUserProfile(
        @Path("id") userId: String,
        @Header("Authorization") authHeader: String? = null,
        @Body request: UpdateProfileRequest
    ): Call<UserProfile>
}