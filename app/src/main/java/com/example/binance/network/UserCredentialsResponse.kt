package com.example.binance.network

data class UserCredentialsResponse(
    val userId: String,
    val encryptedApiKey: String,
    val encryptedSecretKey: String
)
