package com.example.binance.network

import retrofit2.http.GET
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BinanceService {
    @GET("/api/ping")
    suspend fun ping(): PingResponse

    @POST("api/users")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<SignUpResponse>

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @POST("/api/credentials")
    suspend fun saveCredentials(
        @Body req: ApiCredentialsRequest
    ): Response<Void>
}
