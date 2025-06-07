package com.example.binance.network

/**
 * JSON enviado ao backend para criar/atualizar as configurações do bot.
 *
 * - Para criar (POST): POST /api/bot-settings
 * - Para atualizar (PUT):  PUT  /api/bot-settings/user/{userId}
 *
 * Os campos limitPrice, stopPrice, trailingDelta e rsiThreshold são opcionais (podem ser null).
 * Exemplo de JSON:
 * {
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
 *   "movingAvgEnabled": true
 * }
 */
data class BotSettingsRequest(
    var userId: String?            = null,
    var tradingPair: String?       = null,
    var orderType: String?         = null,
    var tradeAmount: Double?       = null,
    var limitPrice: Double?        = null,
    var stopPrice: Double?         = null,
    var trailingDelta: Double?     = null,
    var stopLossPerc: Double?      = null,
    var takeProfitPerc: Double?    = null,
    var rsiEnabled: Boolean        = false,
    var rsiThreshold: Int?         = null,
    var macdEnabled: Boolean       = false,
    var movingAvgEnabled: Boolean  = false
)
