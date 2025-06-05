package com.example.binance.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id")
    val id: Long,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,

    // Se o JSON tiver telefone e endereço, descomente abaixo. Senão, remova.
    @SerializedName("phone")
    val phone: String?,

    @SerializedName("address")
    val address: String?
)