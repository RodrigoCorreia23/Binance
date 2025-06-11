package com.example.binance

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.binance.network.ApiClient
import kotlinx.coroutines.launch
import androidx.appcompat.widget.Toolbar

class AddFundsActivity : AppCompatActivity() {
    private lateinit var rgMethods: RadioGroup
    private lateinit var btnProceed: Button
    private lateinit var etTotal: EditText
    private lateinit var ivBack: ImageView

    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        rgMethods  = findViewById(R.id.rgMethods)
        btnProceed = findViewById(R.id.btnProceed)
        etTotal    = findViewById(R.id.etTotal)
        ivBack     = findViewById(R.id.ivBack)

        // Configura o clique no botão de voltar
        ivBack.setOnClickListener {
            finish()
        }

        val prefs: SharedPreferences = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        userId = prefs.getString("USER_ID", "") ?: ""

        // Fecha a atividade se o ID do utilizador não estiver disponível
        if (userId.isEmpty()) {
            Toast.makeText(this, getString(R.string.id_not_found), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnProceed.setOnClickListener {
            val totalStr = etTotal.text.toString()
            val total = totalStr.toDoubleOrNull()

            if (total == null || total <= 0.0) {
                Toast.makeText(this, getString(R.string.value_bigger_than_zero), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (rgMethods.checkedRadioButtonId) {
                R.id.rbCard -> {
                    Toast.makeText(this, getString(R.string.card_selected) +"(€$totalStr)", Toast.LENGTH_SHORT).show()
                }
                R.id.rbBank -> {
                    Toast.makeText(this, getString(R.string.bank_selected) + "(€$totalStr)", Toast.LENGTH_SHORT).show()
                }
                R.id.rbPaypal -> {
                    Toast.makeText(this, getString(R.string.paypal_selected) +"(€$totalStr)", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, getString(R.string.choose_payment_method), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Chamada à API
            lifecycleScope.launch {
                val success = ApiClient.addFunds(userId, total.toFloat())

                if (success) {
                    Toast.makeText(this@AddFundsActivity, getString(R.string.added_successfully) +": €$total ", Toast.LENGTH_LONG).show()

                    etTotal.setText("0.00")
                    rgMethods.clearCheck()
                } else {
                    Toast.makeText(this@AddFundsActivity, getString(R.string.error_adding_funds), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
