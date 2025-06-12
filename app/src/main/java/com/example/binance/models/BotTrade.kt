// app/src/main/java/com/example/binance/models/BotTrade.kt
package com.example.binance.models

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * DTO para representar um trade retornado pelo endpoint
 * GET /api/bot-trades/user/{userId}
 */
data class BotTrade(
    val id: String,

    val symbol: String,
    val side: String,               // "buy" ou "sell"
    val amount: BigDecimal?,        // poderá vir null
    val price: BigDecimal?,

    @SerializedName("fee")
    val fee: BigDecimal?,

    @SerializedName("profit_estimate")
    val profitEstimate: BigDecimal?,

    val status: String,             // "OPEN" ou "CLOSED"

    @SerializedName("createdAt")
    val createdAt: String,          // Ex.: "2025-06-11T17:02:01.110451+01:00"

    @SerializedName("executedAt")
    val executedAt: String?         // poderá vir null
)
