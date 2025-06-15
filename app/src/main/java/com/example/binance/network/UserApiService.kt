package com.example.binance.network

import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Call

interface UserApiService {
    @PUT("api/users/{id}/fcm-token")
    fun updateFcmToken(
        @Path("id") userId: String,
        @Body request: FcmTokenRequest
    ): Call<String>
}
