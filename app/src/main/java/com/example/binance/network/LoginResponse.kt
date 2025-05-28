package com.example.binance.network


data class LoginResponse(
    val token: String,      // ou outro campo que o teu backend devolva
    val userId: String,
    val username: String
)