package com.example.binance.network

/**
 * JSON enviado ao backend para salvar as credenciais da Binance (API Key + Secret Key).
 * Endpoint: POST /api/credentials
 */
data class ApiCredentialsRequest(
    var userId: String?   = null,
    var apiKey: String?   = null,
    var secretKey: String? = null
)
