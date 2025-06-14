package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.binance.network.RetrofitClient
import com.example.binance.network.SignUpRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
//import androidx.appcompat.app.AppCompatDelegate //Modo escuro hardcoded

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbNewsletter: CheckBox
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView
    private lateinit var tvShow: TextView
    private lateinit var tvStatus: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnLang = findViewById<Button>(R.id.btnChangeLanguage)
        btnLang.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        etName       = findViewById(R.id.etName)
        etEmail      = findViewById(R.id.etEmail)
        etPassword   = findViewById(R.id.etPassword)
        cbNewsletter = findViewById(R.id.cbNewsletter)
        btnSignUp    = findViewById(R.id.btnSignUp)
        tvLogin      = findViewById(R.id.tvLogin)
        tvShow       = findViewById(R.id.tvShow)
        tvStatus     = findViewById(R.id.tvStatus)

        tvShow.setOnClickListener { togglePasswordVisibility() }

        btnSignUp.setOnClickListener {
            val username = etName.text.toString().trim()
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // 1) Validação de frontend
            when {
                username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                    tvStatus.text =getString(R.string.fill_all_fields)
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    tvStatus.text = getString(R.string.invalid_email)
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    tvStatus.text = getString(R.string.invalid_password)
                    return@setOnClickListener
                }
            }

            // 2) Chamada à API
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient
                        .apiService
                        .signUp(SignUpRequest(username, email, password))

                    withContext(Dispatchers.Main) {
                        // limpa erros antigos
                        etName.error = null
                        etEmail.error = null
                        tvStatus.text = ""

                        when {
                            response.isSuccessful -> {
                                // mostra mensagem de boas-vindas
                                val welcome = getString(R.string.welcome) +"$username!"
                                tvStatus.text = welcome
                                Toast.makeText(
                                    this@MainActivity,
                                    welcome,
                                    Toast.LENGTH_SHORT
                                ).show()

                                // espera 1.5s antes de navegar
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(
                                        Intent(
                                            this@MainActivity,
                                            LoginActivity::class.java
                                        )
                                    )
                                    finish()
                                }, 1500)
                            }

                            response.code() == 409 -> {
                                // conflito de unicidade (email ou username já existe)
                                val errBody = response.errorBody()?.string().orEmpty()
                                try {
                                    val obj = JSONObject(errBody)

                                    if (obj.has("email")) {
                                        etEmail.error = obj.getString("email")
                                    }
                                    if (obj.has("username")) {
                                        etName.error = obj.getString("username")
                                    }
                                    // se não veio nenhum dos dois, mostra mensagem genérica
                                    else {
                                        tvStatus.text = getString(R.string.signup_error)
                                    }
                                } catch (e: Exception) {
                                    tvStatus.text = getString(R.string.signup_error)
                                }
                            }

                            else -> {
                                tvStatus.text = "Erro ${response.code()}: " +
                                        response.errorBody()?.string().orEmpty()
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvStatus.text = getString(R.string.network_error) +"${e.localizedMessage}"
                    }
                }
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        //fetchPingStatus()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        etPassword.transformationMethod =
            if (isPasswordVisible)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
        tvShow.text = if (isPasswordVisible) "Hide" else "Show"
        etPassword.setSelection(etPassword.text.length)
    }

    /* Função para testar a conexão com o servidor
    private fun fetchPingStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = RetrofitClient.apiService.ping()
                withContext(Dispatchers.Main) {
                    tvStatus.text = getString(R.string.state_error) +"${resp.message}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = getString(R.string.server_error) +":${e.localizedMessage}"
                }
            }
        }
    }*/
}
