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
            Toast.makeText(this, "Erro: ID de utilizador não encontrado/autenticado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

            // Chamada à API
            lifecycleScope.launch {
                val success = ApiClient.addFunds(userId, total.toFloat())

                if (success) {
                    Toast.makeText(this@AddFundsActivity, "€$total adicionado com sucesso!", Toast.LENGTH_LONG).show()

                    etTotal.setText("0.00")
                    rgMethods.clearCheck()
                } else {
                    Toast.makeText(this@AddFundsActivity, "Erro ao adicionar fundos. Tenta novamente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
