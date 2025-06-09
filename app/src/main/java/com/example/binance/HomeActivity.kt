package com.example.binance

import com.example.binance.BotSettingsActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.binance.network.PublicBinanceApiService
import com.example.binance.network.BinanceService
import com.example.binance.network.RetrofitClient
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.graphics.Paint
import com.example.binance.network.ApiClient

class HomeActivity : AppCompatActivity() {

    private lateinit var acSymbol: AutoCompleteTextView
    private lateinit var tvUsdBalance: TextView
    private lateinit var tvCryptoBalance: TextView
    private lateinit var tvSymbolLabel: TextView
    private lateinit var btnAddFunds: Button
    private lateinit var chart: CandleStickChart
    private lateinit var bottomNav: BottomNavigationView

    // serviços Retrofit
    private lateinit var publicService: PublicBinanceApiService
    private lateinit var backendService: BinanceService

    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private val entries = mutableListOf<CandleEntry>()
    private val timeLabels = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // binds
        acSymbol        = findViewById(R.id.acSymbol)
        tvUsdBalance    = findViewById(R.id.tvUsdBalance)
        tvCryptoBalance = findViewById(R.id.tvCryptoBalance)
        tvSymbolLabel   = findViewById(R.id.tvSymbolLabel)
        btnAddFunds     = findViewById(R.id.btnAddFunds)
        chart           = findViewById(R.id.chart)
        bottomNav       = findViewById(R.id.bottom_nav)


        tvCryptoBalance.text = "0.000000"
        tvSymbolLabel.text   = "BTCUSDT"

        // Retrofit para Binance pública
        publicService = Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PublicBinanceApiService::class.java)

        // Retrofit para seu backend (baseUrl deve terminar em /api/)
        backendService = RetrofitClient.binanceService

        // lê userId salvo no login
        val prefs  = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        Log.d("HomeActivity", "Loaded from prefs → userId='$userId'")

        // Load and display real balance
        if (userId.isNotEmpty()) {
            loadBalance(userId)
        } else {
            Log.e("HomeActivity", "UserId está vazio!")
        }


        // popula symbols e inicia gráfico
        lifecycleScope.launch {
            val symbols = withContext(Dispatchers.IO) {
                publicService.getAllSymbols().map { it.symbol }
            }.filter { it.endsWith("USDT") }

            acSymbol.setAdapter(ArrayAdapter(
                this@HomeActivity,
                android.R.layout.simple_list_item_1,
                symbols
            ))

            acSymbol.setOnItemClickListener { parent, _, pos, _ ->
                switchSymbol(parent.getItemAtPosition(pos) as String)
            }

            acSymbol.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val sym = acSymbol.text.toString().uppercase(Locale.getDefault())
                    if (symbols.contains(sym)) {
                        switchSymbol(sym)
                    } else {
                        Toast.makeText(
                            this@HomeActivity,
                            "Símbolo inválido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                } else false
            }

            switchSymbol("BTCUSDT")
        }

        // botão Add Funds
        btnAddFunds.setOnClickListener {
            startActivity(Intent(this, AddFundsActivity::class.java))
        }

        // configura clique dos itens do BottomNavigationView
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // já está na Home, nada a fazer
                    true
                }
                R.id.nav_refresh -> {
                    // recarregar dados para o símbolo atual
                    val currentSym = tvSymbolLabel.text.toString()
                    if (currentSym.isNotBlank()) {
                        switchSymbol(currentSym)
                    }
                    true
                }
                R.id.nav_profile -> {
                    // abre ProfileActivity
                    startActivity(Intent(this@HomeActivity, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    // caso tenha SettingsActivity, abra aqui. Senão, apenas um Toast.
                    startActivity(Intent(this@HomeActivity, BotSettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // item “Home” selecionado por padrão
        bottomNav.selectedItemId = R.id.nav_home
    }

    private fun switchSymbol(symbol: String) {
        tvSymbolLabel.text = symbol
        ws?.close(1000, null)
        entries.clear()
        timeLabels.clear()
        chart.clear()
        chart.data = null

        lifecycleScope.launch {
            loadHistorical(symbol)
            setupChart()
            startStreaming(symbol)
        }
    }

    private suspend fun loadHistorical(symbol: String) {
        withContext(Dispatchers.IO) {
            val klines = publicService.getKlines(symbol)
            klines.forEachIndexed { i, k ->
                val t     = (k[0] as Number).toLong()
                val open  = (k[1] as String).toFloat()
                val high  = (k[2] as String).toFloat()
                val low   = (k[3] as String).toFloat()
                val close = (k[4] as String).toFloat()
                entries.add(CandleEntry(i.toFloat(), high, low, open, close))
                timeLabels.add(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(t)))
            }
        }
        runOnUiThread {
            if (entries.isNotEmpty()) {
                tvCryptoBalance.text = String.format(Locale.getDefault(), "%,.6f", entries.last().close)
            }
        }
    }

    private fun setupChart() {
        if (entries.isEmpty()) return
        val ds = CandleDataSet(entries, "").apply {
            setDrawValues(false)
            increasingColor = Color.GREEN
            increasingPaintStyle = Paint.Style.FILL
            decreasingColor = Color.RED
            decreasingPaintStyle = Paint.Style.FILL
            shadowColorSameAsCandle = true
        }
        chart.apply {
            data = CandleData(ds)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.valueFormatter = IndexAxisValueFormatter(timeLabels)
            xAxis.granularity = 1f
            animateX(500)
        }
    }

    private fun startStreaming(symbol: String) {
        val stream = symbol.lowercase(Locale.getDefault()) + "@kline_5m"
        val req = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/$stream")
            .build()
        ws = client.newWebSocket(req, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val obj     = JSONObject(text).getJSONObject("k")
                    val isFinal = obj.getBoolean("x")
                    val open    = obj.getString("o").toFloat()
                    val high    = obj.getString("h").toFloat()
                    val low     = obj.getString("l").toFloat()
                    val close   = obj.getString("c").toFloat()
                    val t       = obj.getLong("t")

                    runOnUiThread {
                        if (isFinal) {
                            entries.add(CandleEntry(entries.size.toFloat(), high, low, open, close))
                            timeLabels.add(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(t)))
                        } else if (entries.isNotEmpty()) {
                            entries[entries.lastIndex] = CandleEntry(entries.lastIndex.toFloat(), high, low, open, close)
                        }
                        chart.data?.notifyDataChanged()
                        chart.notifyDataSetChanged()
                        chart.setVisibleXRangeMaximum(50f)
                        chart.moveViewToX(entries.size.toFloat())
                        chart.invalidate()
                        tvCryptoBalance.text = String.format(Locale.getDefault(), "%,.6f", close)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        val userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isNotEmpty()) {
            loadBalance(userId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ws?.close(1000, null)
        client.dispatcher.executorService.shutdown()
    }

    private fun loadBalance(userId: String) {
        Log.d("HomeActivity", "Iniciando loadBalance para userId: '$userId'")

        lifecycleScope.launch {
            val balance = ApiClient.getUserBalance(userId)
            Log.d("HomeActivity", "Balance retornado: $balance")

            if (balance != null) {
                Log.d("HomeActivity", "Atualizando UI com balance: $balance")
                tvUsdBalance.text = String.format(Locale.getDefault(), "€%,.2f", balance)
            } else {
                Log.e("HomeActivity", "Balance é null")
                Toast.makeText(
                    this@HomeActivity,
                    "Erro ao carregar saldo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


}
