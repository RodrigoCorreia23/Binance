package com.example.binance

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddFundsActivity : AppCompatActivity() {
    private lateinit var rgMethods: RadioGroup
    private lateinit var btnProceed: Button
    private lateinit var etTotal: EditText

    private lateinit var rbCard: RadioButton
    private lateinit var rbBank: RadioButton
    private lateinit var rbPaypal: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        rgMethods  = findViewById(R.id.rgMethods)
        btnProceed = findViewById(R.id.btnProceed)
        etTotal    = findViewById(R.id.etTotal)

        rbCard = findViewById(R.id.rbCard)
        rbBank = findViewById(R.id.rbBank)
        rbPaypal = findViewById(R.id.rbPaypal)

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

            // Toast mostrar o novo saldo
            Toast.makeText(this, "€$total adicionado! Novo saldo: €${String.format("%.2f", newBalance)}", Toast.LENGTH_LONG).show()

            // Limpar os campos após a adição
            etTotal.setText("0.00")
            rgMethods.clearCheck()
        }
    }

    // Métodos para os cliques nas opções
    fun selectCard(view: View) {
        rbCard.isChecked = true
    }

    fun selectBank(view: View) {
        rbBank.isChecked = true
    }

    fun selectPaypal(view: View) {
        rbPaypal.isChecked = true
    }
}