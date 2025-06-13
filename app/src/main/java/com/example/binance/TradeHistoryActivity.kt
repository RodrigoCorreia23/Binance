package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.binance.adapter.BotTradeAdapter
import com.example.binance.network.ApiClient
import com.example.binance.network.BinanceService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.util.*

class TradeHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BotTradeAdapter
    private val trades = mutableListOf<com.example.binance.models.BotTrade>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trade_history)

        recyclerView = findViewById(R.id.recyclerViewTrades)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = BotTradeAdapter(trades)
        recyclerView.adapter = adapter

        fetchTradeHistory()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_refresh -> {
                    // Já estás nesta página
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, BotSettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.nav_refresh
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
