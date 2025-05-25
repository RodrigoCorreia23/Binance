package com.example.binance

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvShow: TextView
    private lateinit var btnLogIn: Button
    private lateinit var tvForgot: TextView

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Bind views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvShow = findViewById(R.id.tvShow)
        btnLogIn = findViewById(R.id.btnLogIn)
        tvForgot = findViewById(R.id.tvForgot)

        // Toggle password visibility
        tvShow.setOnClickListener { togglePasswordVisibility() }

        // Log In button action
        btnLogIn.setOnClickListener {
            // TODO: implementar lógica de login (validações, chamada API, etc.)
        }

        // Forgot password link
        tvForgot.setOnClickListener {
            // TODO: tratar recuperação de senha
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        etPassword.transformationMethod = if (isPasswordVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        tvShow.text = if (isPasswordVisible) "Hide" else "Show"
        etPassword.setSelection(etPassword.text.length)
    }
}
