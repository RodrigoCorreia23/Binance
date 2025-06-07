package com.example.binance

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.binance.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BotSettingsActivity : AppCompatActivity() {

    private lateinit var switchBotActive: Switch
    private lateinit var spinnerTradingPair: Spinner
    private lateinit var spinnerOrderType: Spinner
    private lateinit var etTradeAmount: EditText
    private lateinit var etLimitPrice: EditText
    private lateinit var etStopPrice: EditText
    private lateinit var etTrailingDelta: EditText
    private lateinit var etStopLossPerc: EditText
    private lateinit var etTakeProfitPerc: EditText
    private lateinit var cbRsi: CheckBox
    private lateinit var etRsiThreshold: EditText
    private lateinit var cbMacd: CheckBox
    private lateinit var cbBollinger: CheckBox
    private lateinit var btnSaveBotSettings: Button

    private lateinit var botStateService: BotStateApiService
    private lateinit var botSettingsService: BotSettingsApiService
    private lateinit var credentialsService: CredentialsApiService

    private var userId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bot_settings)

        switchBotActive    = findViewById(R.id.switchBotSettingsActive)
        spinnerTradingPair = findViewById(R.id.spinnerBotTradingPair)
        spinnerOrderType   = findViewById(R.id.spinnerBotOrderType)
        etTradeAmount      = findViewById(R.id.etBotTradeAmount)
        etLimitPrice       = findViewById(R.id.etBotLimitPrice)
        etStopPrice        = findViewById(R.id.etBotStopPrice)
        etTrailingDelta    = findViewById(R.id.etBotTrailingDelta)
        etStopLossPerc     = findViewById(R.id.etBotStopLossPerc)
        etTakeProfitPerc   = findViewById(R.id.etBotTakeProfitPerc)
        cbRsi              = findViewById(R.id.cbBotRsi)
        etRsiThreshold     = findViewById(R.id.etBotRsiThreshold)
        cbMacd             = findViewById(R.id.cbBotMacd)
        cbBollinger        = findViewById(R.id.cbBotBollinger)
        btnSaveBotSettings = findViewById(R.id.btnSaveBotSettings)

        val prefs: SharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = prefs.getString("USER_ID", "") ?: ""
        if (userId.isEmpty()) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        botStateService    = RetrofitClient.botStateService
        botSettingsService = RetrofitClient.botSettingsService
        credentialsService = RetrofitClient.credentialsService

        val pairs = listOf("BTCUSDT", "ETHUSDT", "BNBUSDT")
        spinnerTradingPair.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, pairs)

        val orderTypes = listOf("Market", "Limit", "Stop-Limit", "Limit Maker", "Trailing Stop")
        spinnerOrderType.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, orderTypes)

        loadBotState()
        loadBotSettings()
        loadUserCredentials()

        switchBotActive.setOnCheckedChangeListener { _, isChecked ->
            updateBotState(isChecked)
        }

        btnSaveBotSettings.setOnClickListener {
            saveBotSettings()
        }
    }

    private fun loadBotState() {
        botStateService.getBotState(userId).enqueue(object : Callback<BotStateResponse> {
            override fun onResponse(call: Call<BotStateResponse>, response: Response<BotStateResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    switchBotActive.isChecked = response.body()!!.isActive
                } else {
                    createInitialBotState()
                }
            }
            override fun onFailure(call: Call<BotStateResponse>, t: Throwable) {
                Toast.makeText(this@BotSettingsActivity, "Erro ao carregar estado: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createInitialBotState() {
        val req = BotStateRequest(userId = userId, isActive = false)
        botStateService.createBotState(req).enqueue(object : Callback<BotStateResponse> {
            override fun onResponse(call: Call<BotStateResponse>, response: Response<BotStateResponse>) {
                if (response.isSuccessful) switchBotActive.isChecked = false
            }
            override fun onFailure(call: Call<BotStateResponse>, t: Throwable) {
                Log.e("BotSettingsActivity", "Erro criando estado: ${t.message}")
            }
        })
    }

    private fun updateBotState(isActive: Boolean) {
        val call = if (isActive) botStateService.activateBot(userId)
        else botStateService.deactivateBot(userId)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                val msg = if (isActive) "Bot ativado" else "Bot desativado"
                if (response.isSuccessful) {
                    Toast.makeText(this@BotSettingsActivity, msg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@BotSettingsActivity, "Erro: ${response.code()}", Toast.LENGTH_SHORT).show()
                    switchBotActive.isChecked = !isActive
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@BotSettingsActivity, "Falha: ${t.message}", Toast.LENGTH_LONG).show()
                switchBotActive.isChecked = !isActive
            }
        })
    }

    private fun loadBotSettings() {
        botSettingsService.getBotSettings(userId).enqueue(object : Callback<BotSettingsResponse> {
            override fun onResponse(call: Call<BotSettingsResponse>, response: Response<BotSettingsResponse>) {
                response.body()?.let { populateFieldsWithSettings(it) }
            }
            override fun onFailure(call: Call<BotSettingsResponse>, t: Throwable) {
                Log.e("BotSettingsActivity", "Erro ao carregar settings: ${t.message}")
            }
        })
    }

    private fun populateFieldsWithSettings(s: BotSettingsResponse) {
        (spinnerTradingPair.adapter as ArrayAdapter<String>).let {
            val pos = it.getPosition(s.tradingPair)
            if (pos >= 0) spinnerTradingPair.setSelection(pos)
        }
        (spinnerOrderType.adapter as ArrayAdapter<String>).let {
            val pos = it.getPosition(s.orderType)
            if (pos >= 0) spinnerOrderType.setSelection(pos)
        }

        etTradeAmount.setText(s.tradeAmount.toString())
        s.limitPrice?.let { etLimitPrice.setText(it.toString()) }
        s.stopPrice?.let { etStopPrice.setText(it.toString()) }
        s.trailingDelta?.let { etTrailingDelta.setText(it.toString()) }
        etStopLossPerc.setText(s.stopLossPerc.toString())
        etTakeProfitPerc.setText(s.takeProfitPerc.toString())
        cbRsi.isChecked = s.rsiEnabled
        s.rsiThreshold?.let { etRsiThreshold.setText(it.toString()) }
        cbMacd.isChecked = s.macdEnabled
        cbBollinger.isChecked = s.movingAvgEnabled
    }

    private fun saveBotSettings() {
        val req = BotSettingsRequest(
            userId = userId,
            tradingPair = spinnerTradingPair.selectedItem.toString(),
            orderType = spinnerOrderType.selectedItem.toString(),
            tradeAmount = etTradeAmount.text.toString().toDoubleOrNull() ?: return,
            limitPrice = etLimitPrice.text.toString().toDoubleOrNull(),
            stopPrice = etStopPrice.text.toString().toDoubleOrNull(),
            trailingDelta = etTrailingDelta.text.toString().toDoubleOrNull(),
            stopLossPerc = etStopLossPerc.text.toString().toDoubleOrNull() ?: return,
            takeProfitPerc = etTakeProfitPerc.text.toString().toDoubleOrNull() ?: return,
            rsiEnabled = cbRsi.isChecked,
            rsiThreshold = etRsiThreshold.text.toString().toIntOrNull(),
            macdEnabled = cbMacd.isChecked,
            movingAvgEnabled = cbBollinger.isChecked
        )

        botSettingsService.getBotSettings(userId).enqueue(object : Callback<BotSettingsResponse> {
            override fun onResponse(call: Call<BotSettingsResponse>, response: Response<BotSettingsResponse>) {
                val callToSave = if (response.isSuccessful && response.body() != null)
                    botSettingsService.updateBotSettings(userId, req)
                else
                    botSettingsService.createBotSettings(req)

                callToSave.enqueue(object : Callback<BotSettingsResponse> {
                    override fun onResponse(call: Call<BotSettingsResponse>, resp: Response<BotSettingsResponse>) {
                        if (resp.isSuccessful)
                            Toast.makeText(this@BotSettingsActivity, "Configurações salvas!", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(this@BotSettingsActivity, "Erro: ${resp.code()}", Toast.LENGTH_SHORT).show()
                    }
                    override fun onFailure(call: Call<BotSettingsResponse>, t: Throwable) {
                        Toast.makeText(this@BotSettingsActivity, "Falha: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
            override fun onFailure(call: Call<BotSettingsResponse>, t: Throwable) {
                Toast.makeText(this@BotSettingsActivity, "Erro ao verificar: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun loadUserCredentials() {
        credentialsService.getUserCredentials(userId).enqueue(object : Callback<UserCredentialsResponse> {
            override fun onResponse(call: Call<UserCredentialsResponse>, response: Response<UserCredentialsResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val creds = response.body()!!
                    Log.d("BotSettingsActivity", "Credenciais carregadas (API key: ${creds.encryptedApiKey.take(6)}...)")
                } else {
                    Toast.makeText(this@BotSettingsActivity, "Erro ao carregar credenciais", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserCredentialsResponse>, t: Throwable) {
                Toast.makeText(this@BotSettingsActivity, "Falha: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
