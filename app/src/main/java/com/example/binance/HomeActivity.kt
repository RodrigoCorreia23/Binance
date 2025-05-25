package com.example.binance

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.*

interface BinanceApiService {
    @GET("api/v3/ticker/price")
    suspend fun getAllSymbols(): List<SymbolPrice>

    @GET("api/v3/klines")
    suspend fun getKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "5m",
        @Query("limit") limit: Int = 100
    ): List<List<Any>>
}
data class SymbolPrice(val symbol: String, val price: String)

class HomeActivity : AppCompatActivity() {
    private lateinit var acSymbol: AutoCompleteTextView
    private lateinit var tvUsdBalance: TextView
    private lateinit var tvCryptoBalance: TextView
    private lateinit var tvSymbolLabel: TextView
    private lateinit var btnAddFunds: Button
    private lateinit var chart: CandleStickChart
    private lateinit var service: BinanceApiService

    private val client = OkHttpClient()
    private var ws: WebSocket? = null

    private val entries = mutableListOf<CandleEntry>()
    private val timeLabels = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // ---- bind de views ----
        acSymbol        = findViewById(R.id.acSymbol)
        tvUsdBalance    = findViewById(R.id.tvUsdBalance)
        tvCryptoBalance = findViewById(R.id.tvCryptoBalance)
        tvSymbolLabel   = findViewById(R.id.tvSymbolLabel)
        btnAddFunds     = findViewById(R.id.btnAddFunds)
        chart           = findViewById(R.id.chart)

        // exibe saldos iniciais
        tvUsdBalance.text    = "$0.00"
        tvCryptoBalance.text = "0.000000"
        tvSymbolLabel.text   = "BTCUSDT"

        // configura Retrofit
        service = Retrofit.Builder()
            .baseUrl("https://api.binance.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BinanceApiService::class.java)

        lifecycleScope.launch {
            // popula autocomplete
            val symbols = withContext(Dispatchers.IO) {
                service.getAllSymbols().map { it.symbol }
            }.filter { it.endsWith("USDT") }
            acSymbol.setAdapter(ArrayAdapter(
                this@HomeActivity,
                android.R.layout.simple_list_item_1,
                symbols
            ))

            // handlers de troca
            acSymbol.setOnItemClickListener { parent, _, pos, _ ->
                switchSymbol(parent.getItemAtPosition(pos) as String)
            }
            acSymbol.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val sym = acSymbol.text.toString().uppercase(Locale.ROOT)
                    if (symbols.contains(sym)) {
                        switchSymbol(sym)
                    } else {
                        Toast.makeText(this@HomeActivity, "Símbolo inválido", Toast.LENGTH_SHORT).show()
                    }
                    true
                } else false
            }

            // carga inicial
            switchSymbol("BTCUSDT")
        }

        btnAddFunds.setOnClickListener {
            // TODO: adicionar fundos
        }
    }

    private fun switchSymbol(symbol: String) {
        // atualiza label
        tvSymbolLabel.text = symbol

        // fecha socket antigo
        ws?.close(1000, null)

        // limpa dados antigos
        entries.clear()
        timeLabels.clear()
        chart.clear()
        chart.data = null

        // recarrega histórico + gráfico + streaming
        lifecycleScope.launch {
            loadHistorical(symbol)
            setupChart()
            startStreaming(symbol)
        }
    }

    private suspend fun loadHistorical(symbol: String) {
        withContext(Dispatchers.IO) {
            val klines = service.getKlines(symbol)
            klines.forEachIndexed { i, k ->
                val t = when (val raw = k[0]) {
                    is Number -> raw.toLong()
                    is String -> raw.toLong()
                    else      -> 0L
                }
                val open  = (k[1] as String).toFloat()
                val high  = (k[2] as String).toFloat()
                val low   = (k[3] as String).toFloat()
                val close = (k[4] as String).toFloat()
                entries.add(CandleEntry(i.toFloat(), high, low, open, close))
                timeLabels.add(
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(t))
                )
            }
        }
        runOnUiThread {
            // exibe último preço histórico
            if (entries.isNotEmpty()) {
                val last = entries.last().close
                tvCryptoBalance.text = String.format(Locale.getDefault(), "%,.6f", last)
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
                            timeLabels.add(
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(t))
                            )
                        } else if (entries.isNotEmpty()) {
                            val idx = entries.lastIndex
                            entries[idx] = CandleEntry(idx.toFloat(), high, low, open, close)
                        }
                        chart.data?.notifyDataChanged()
                        chart.notifyDataSetChanged()
                        chart.setVisibleXRangeMaximum(50f)
                        chart.moveViewToX(entries.size.toFloat())
                        chart.invalidate()
                        // atualiza preço em real-time
                        tvCryptoBalance.text = String.format(Locale.getDefault(), "%,.6f", close)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        ws?.close(1000, null)
        client.dispatcher.executorService.shutdown()
    }
}