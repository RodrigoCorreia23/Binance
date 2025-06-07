package com.example.binance.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface CredentialsApiService {
    @GET("/api/credentials/user/{userId}")
    fun getUserCredentials(@Path("userId") userId: String): Call<UserCredentialsResponse>
}
