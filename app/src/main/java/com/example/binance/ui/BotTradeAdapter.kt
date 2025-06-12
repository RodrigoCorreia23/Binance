package com.example.binance.adapter

import android.graphics.Color
import android.os.Build
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

        // 3. Data (apenas a parte da data)
        val data = trade.createdAt?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "-"
        holder.tvDate.text = data


        // 4. Preço da ordem
        val preco = trade.price?.setScale(2, RoundingMode.HALF_UP)?.toPlainString() ?: "-"
        holder.tvPrice.text = preco

        // 5. L/P (lucro ou prejuízo) – só visível em vendas
        val lucro = trade.profitEstimate ?: BigDecimal.ZERO
        if (trade.side.lowercase() == "sell") {
            val cor = if (lucro >= BigDecimal.ZERO) "#4CAF50" else "#F44336"
            holder.tvProfit.setTextColor(Color.parseColor(cor))
            holder.tvProfit.text = "${if (lucro >= BigDecimal.ZERO) "+" else ""}${lucro.setScale(2, RoundingMode.HALF_UP)}"
        } else {
            holder.tvProfit.text = "-"
            holder.tvProfit.setTextColor(Color.GRAY)
        }
    }


    override fun getItemCount(): Int = trades.size
}
