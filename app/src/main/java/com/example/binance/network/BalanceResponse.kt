package com.example.binance.network

/**
 * JSON retornado pelo backend com o saldo livre em USDT do usuário.
 * Endpoint: GET /api/credentials/{userId}/balance
 *
 * Exemplo de JSON:
 * {
 *   "free": "1234.567890"
 * }
 */
data class BalanceResponse(
    val free: String
)
