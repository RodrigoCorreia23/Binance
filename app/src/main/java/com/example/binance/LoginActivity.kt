package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.binance.network.LoginRequest
import com.example.binance.network.RetrofitClient
import com.example.binance.network.HasCredResponse
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
        tvStatus   = findViewById(R.id.tvStatus)

        tvShow.setOnClickListener { togglePasswordVisibility() }

        btnLogIn.setOnClickListener {
            val email    = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // 1) validação local
            when {
                email.isEmpty() || password.isEmpty() -> {
                    tvStatus.text = "Preencha email e senha."
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    tvStatus.text = "Email inválido."
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    tvStatus.text = "Senha muito curta."
                    return@setOnClickListener
                }
            }

            // 2) chamada de login
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val resp = RetrofitClient.service
                        .login(LoginRequest(email, password))

                    withContext(Dispatchers.Main) {
                        // limpa erros anteriores
                        etEmail.error    = null
                        etPassword.error = null
                        tvStatus.text    = ""

                        when {
                            resp.isSuccessful -> {
                                // login OK: pega body
                                val body = resp.body()!!

                                // 3) agora verifica se já tem API credentials
                                CoroutineScope(Dispatchers.IO).launch {
                                    val check = RetrofitClient.service
                                        .hasCredentials(body.userId)

                                    withContext(Dispatchers.Main) {
                                        if (check.isSuccessful) {
                                            val hasCreds = check.body()?.hasCredentials == true
                                            val nextCls = if (hasCreds)
                                                HomeActivity::class.java
                                            else
                                                ApiCredentialsActivity::class.java

                                            startActivity(
                                                Intent(
                                                    this@LoginActivity,
                                                    nextCls
                                                ).apply {
                                                    putExtra("USER_ID", body.userId)
                                                    putExtra("USERNAME", body.username)
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                }
                                            )
                                        } else {
                                            tvStatus.text =
                                                "Erro ao verificar credenciais: ${check.code()}"
                                        }
                                    }
                                }
                            }
                            resp.code() == 404 -> {
                                val msg = JSONObject(resp.errorBody()?.string().orEmpty())
                                    .optString("email", "Email não cadastrado")
                                etEmail.error = msg
                            }
                            resp.code() == 401 -> {
                                val msg = JSONObject(resp.errorBody()?.string().orEmpty())
                                    .optString("password", "Senha incorreta")
                                etPassword.error = msg
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
            // TODO: recuperar senha
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
