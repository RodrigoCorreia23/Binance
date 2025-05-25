package com.example.binance

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.binance.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbNewsletter: CheckBox
    private lateinit var btnSignUp: Button
    private lateinit var tvLogin: TextView
    private lateinit var tvForgot: TextView
    private lateinit var tvShow: TextView

    // Novo:
    private lateinit var tvStatus: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Bind views
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbNewsletter = findViewById(R.id.cbNewsletter)
        btnSignUp = findViewById(R.id.btnSignUp)
        tvLogin = findViewById(R.id.tvLogin)
        tvForgot = findViewById(R.id.tvForgot)
        tvShow = findViewById(R.id.tvShow)
        tvStatus = findViewById(R.id.tvStatus)   // ← binding do status

        // Toggle password visibility
        tvShow.setOnClickListener { togglePasswordVisibility() }

        // Sign Up button action
        btnSignUp.setOnClickListener {
            // TODO: add sign-up logic (e.g., validate fields, API call)
        }

        // Login link
        tvLogin.setOnClickListener {
            // TODO: navigate to Login screen
        }

        // Forgot password link
        tvForgot.setOnClickListener {
            // TODO: handle password recovery
        }

        // Faz o ping ao backend
        fetchPingStatus()
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        etPassword.transformationMethod =
            if (isPasswordVisible) HideReturnsTransformationMethod.getInstance()
            else PasswordTransformationMethod.getInstance()

        tvShow.text = if (isPasswordVisible) "Hide" else "Show"
        etPassword.setSelection(etPassword.text.length)
    }

    private fun fetchPingStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = RetrofitClient.apiService.ping()
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Status do servidor: ${resp.status}"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text = "Erro ao contactar servidor: ${e.localizedMessage}"
                }
            }
        }
    }
}