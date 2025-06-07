package com.example.binance.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

)