package com.example.binance.dto

data class BalanceResponse(
    val asset: String,
    val free: String,
    val locked: String
)
