package com.example.binance

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.binance.adapter.BotTradeAdapter
import com.example.binance.databinding.ActivityTradeHistoryBinding
import com.example.binance.models.BotTrade
import com.example.binance.models.Trade
import com.example.binance.network.ApiClient
import com.example.binance.network.BinanceService
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale
import java.util.UUID

class TradeHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BotTradeAdapter
    private val trades = mutableListOf<BotTrade>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_history)

        recyclerView = findViewById(R.id.recyclerViewTrades)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BotTradeAdapter(trades)
        recyclerView.adapter = adapter

        fetchTradeHistory()
    }

    private fun fetchTradeHistory() {
        val apiService = ApiClient.createService(BinanceService::class.java)
        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userIdStr = prefs.getString("USER_ID", null)

        if (userIdStr == null) {
            Toast.makeText(this, "Utilizador não autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = UUID.fromString(userIdStr)

        lifecycleScope.launch {
            try {
                val tradeHistory = apiService.getTrades(userId)
                trades.clear()
                trades.addAll(tradeHistory)
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Toast.makeText(this@TradeHistoryActivity, "Erro ao carregar trades", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
