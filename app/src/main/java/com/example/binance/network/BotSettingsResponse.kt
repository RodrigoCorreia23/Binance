package com.example.binance.network

/**
 * JSON retornado pelo backend ao consultar/criar/atualizar as configurações do bot.
 * Endpoint:
 * - GET  /api/bot-settings/user/{userId}
 * - POST /api/bot-settings
 * - PUT  /api/bot-settings/user/{userId}
 *
 * Exemplo de resposta:
 * {
 *   "id": "abcdef12-3456-7890-abcd-ef1234567890",
 *   "userId": "123e4567-e89b-12d3-a456-426614174000",
 *   "tradingPair": "BTCUSDT",
 *   "orderType": "Market",
 *   "tradeAmount": 0.001,
 *   "limitPrice": null,
 *   "stopPrice": null,
 *   "trailingDelta": null,
 *   "stopLossPerc": 2.0,
 *   "takeProfitPerc": 4.0,
 *   "rsiEnabled": true,
 *   "rsiThreshold": 30,
 *   "macdEnabled": true,
 *   "movingAvgEnabled": true,
 *   "createdAt": "2025-06-05T15:20:00.123Z",
 *   "updatedAt": "2025-06-05T15:25:00.789Z"
 * }
 */
data class BotSettingsResponse(
    val id: String,
    val userId: String,
    val tradingPair: String,
    val orderType: String,
    val tradeAmount: Double,
    val limitPrice: Double?,
    val stopPrice: Double?,
    val trailingDelta: Double?,
    val stopLossPerc: Double,
    val takeProfitPerc: Double,
    val rsiEnabled: Boolean,
    val rsiThreshold: Int?,
    val macdEnabled: Boolean,
    val movingAvgEnabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)
