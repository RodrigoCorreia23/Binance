package com.example.binance

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddFundsActivity : AppCompatActivity() {
    private lateinit var rgMethods: RadioGroup
    private lateinit var btnProceed: Button
    private lateinit var etTotal: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        rgMethods  = findViewById(R.id.rgMethods)
        btnProceed = findViewById(R.id.btnProceed)
        etTotal    = findViewById(R.id.etTotal)

        btnProceed.setOnClickListener {
            val totalStr = etTotal.text.toString()
            val total = totalStr.toDoubleOrNull()

            if (total == null || total <= 0.0) {
                Toast.makeText(this, "Adicione um valor maior que zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (rgMethods.checkedRadioButtonId) {
                R.id.rbCard -> {
                    Toast.makeText(this, "Cartão selecionado (€$totalStr)", Toast.LENGTH_SHORT).show()
                }
                R.id.rbBank -> {
                    Toast.makeText(this, "Conta bancária selecionada (€$totalStr)", Toast.LENGTH_SHORT).show()
                }
                R.id.rbPaypal -> {
                    Toast.makeText(this, "PayPal selecionado (€$totalStr)", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Escolha um método de pagamento", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Guardar o valor no SharedPreferences
            val prefs = getSharedPreferences("wallet", MODE_PRIVATE)
            val currentBalance = prefs.getFloat("balance", 0f)
            val newBalance = currentBalance + total.toFloat()
            prefs.edit().putFloat("balance", newBalance).apply()

            Toast.makeText(this, "€$total adicionado! Novo saldo: €${String.format("%.2f", newBalance)}", Toast.LENGTH_LONG).show()

            // Limpar os campos
            etTotal.setText("0.00")
            rgMethods.clearCheck()
        }
    }
}
