package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.binance.network.LoginRequest
import com.example.binance.network.LoginResponse
import com.example.binance.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvShow: TextView
    private lateinit var btnLogIn: Button
    private lateinit var tvForgot: TextView
    private lateinit var tvStatus: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail    = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvShow     = findViewById(R.id.tvShow)
        btnLogIn   = findViewById(R.id.btnLogIn)
        tvForgot   = findViewById(R.id.tvForgot)
        tvStatus = findViewById(R.id.tvStatus)


        tvShow.setOnClickListener { togglePasswordVisibility() }

        btnLogIn.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // validação
            when {
                email.isEmpty() || password.isEmpty() -> {
                    tvStatus.text = "Preenche email e password."
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    tvStatus.text = "Email inválido."
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    tvStatus.text = "Password muito curta."
                    return@setOnClickListener
                }
            }

            // chamada de login
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = RetrofitClient.apiService
                        .login(LoginRequest(email, password))

                    withContext(Dispatchers.Main) {
                        when {
                            resp.isSuccessful -> {
                                val body = resp.body()!!
                                // navega para ApiCredentialsActivity
                                startActivity(Intent(
                                    this@LoginActivity,
                                    ApiCredentialsActivity::class.java
                                ).apply {
                                    putExtra("USER_ID",   body.userId)
                                    putExtra("USERNAME",  body.username)
                                })
                                finish()
                            }
                            resp.code() == 401 -> {
                                tvStatus.text = "Credenciais inválidas."
                            }
                            else -> {
                                val err = resp.errorBody()?.string().orEmpty()
                                val msg = runCatching {
                                    JSONObject(err).optString("message", err)
                                }.getOrNull() ?: err
                                tvStatus.text = "Erro ${resp.code()}: $msg"
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        tvStatus.text = "Erro de rede: ${e.localizedMessage}"
                    }
                }
            }
        }

        tvForgot.setOnClickListener {
            // TODO: recuperação de password
        }
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
}
