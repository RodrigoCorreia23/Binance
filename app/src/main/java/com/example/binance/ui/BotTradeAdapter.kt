package com.example.binance.adapter

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.binance.R
import com.example.binance.models.BotTrade
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter


class BotTradeAdapter(private val trades: List<BotTrade>) :
    RecyclerView.Adapter<BotTradeAdapter.TradeViewHolder>() {

    class TradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSymbol: TextView = itemView.findViewById(R.id.tvSymbol)
        val tvSide: TextView = itemView.findViewById(R.id.tvSide)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvProfit: TextView = itemView.findViewById(R.id.tvProfit)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TradeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bot_trade, parent, false)
        return TradeViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TradeViewHolder, position: Int) {
        val trade = trades[position]

        Log.d("BotTrade", "Position: $position")
        Log.d("BotTrade", "Symbol: ${trade.symbol}")
        Log.d("BotTrade", "Side: ${trade.side}")
        Log.d("BotTrade", "ProfitEstimate: ${trade.profitEstimate}")
        Log.d("BotTrade", "ProfitEstimate is null: ${trade.profitEstimate == null}")

        // 1. Ativo (par de trading)
        holder.tvSymbol.text = trade.symbol

        // 2. Tipo (compra ou venda)
        holder.tvSide.text = trade.side.uppercase()
        holder.tvSide.setTextColor(
            if (trade.side.lowercase() == "buy")
                Color.parseColor("#2196F3") // Azul para compra
            else
                Color.parseColor("#F44336") // Vermelho para venda
        )

        // 3. Data
        val data = trade.createdAt?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "-"
        holder.tvDate.text = data

        // 4. Preço
        val preco = trade.price?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "-"
        holder.tvPrice.text = preco

        // 5. L/P - CORREÇÃO: Remover a condição de apenas SELL
        val lucro = trade.profitEstimate

        if (lucro != null) {
            // Formatação adequada para valores pequenos
            val lucroFormatado = when {
                lucro.abs() < BigDecimal("0.01") -> {
                    lucro.setScale(8, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
                }
                lucro.abs() < BigDecimal("1") -> {
                    lucro.setScale(4, RoundingMode.HALF_UP).toPlainString()
                }
                else -> {
                    lucro.setScale(2, RoundingMode.HALF_UP).toPlainString()
                }
            }

            val cor = if (lucro >= BigDecimal.ZERO) "#4CAF50" else "#F44336"
            holder.tvProfit.setTextColor(Color.parseColor(cor))
            holder.tvProfit.text = "${if (lucro > BigDecimal.ZERO) "+" else ""}${lucroFormatado}Z"
        } else {
            holder.tvProfit.text = "-"
            holder.tvProfit.setTextColor(Color.GRAY)
        }
    }


    override fun getItemCount(): Int = trades.size
}
