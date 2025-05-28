package com.example.binance.network

data class SignUpResponse(
    val id: String,        
    val email: String,
    val username: String,
    val createdAt: String,
    val updatedAt: String?
)
