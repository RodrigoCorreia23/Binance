package com.example.binance

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddFundsActivity : AppCompatActivity() {
    private lateinit var rgMethods: RadioGroup
    private lateinit var btnProceed: Button
    private lateinit var etTotal: EditText  // <-- novo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_funds)

        rgMethods  = findViewById(R.id.rgMethods)
        btnProceed = findViewById(R.id.btnProceed)
        etTotal    = findViewById(R.id.etTotal)  // <-- bind

        // já iniciado com "0.00" pelo XML, mas se quiser:
        // etTotal.setText("0.00")

        btnProceed.setOnClickListener {
            val totalStr = etTotal.text.toString()
            val total = totalStr.toDoubleOrNull()
            if (total == null || total <= 0.0) {
                Toast.makeText(this, "Informe um valor maior que zero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (rgMethods.checkedRadioButtonId) {
                R.id.rbCard -> {
                    Toast.makeText(this, "Cartão selecionado (R\$ $totalStr)", Toast.LENGTH_SHORT).show()
                }
                R.id.rbBank -> {
                    Toast.makeText(this, "Conta bancária selecionada (R\$ $totalStr)", Toast.LENGTH_SHORT).show()
                }
                R.id.rbPaypal -> {
                    Toast.makeText(this, "PayPal selecionado (R\$ $totalStr)", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Escolha um método de pagamento", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

