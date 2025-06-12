package com.example.binance.models

import com.google.gson.annotations.SerializedName

data class Trade(
    val symbol: String,
    val side: String,

    // preço da operação, como string (podes mudar para Double se preferires)
    @SerializedName("price")
    val price: String,

    // data/hora, vinda da API (ou já formatada)
    @SerializedName("createdAt")
    val createdAt: String,

    // lucro ou perda: ex "+250" ou "-100"
    @SerializedName("profitLoss")
    val profitLoss: String
)
