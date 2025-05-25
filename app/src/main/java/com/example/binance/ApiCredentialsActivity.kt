package com.example.binance

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ApiCredentialsActivity : AppCompatActivity() {

    private lateinit var etApiKey: EditText
    private lateinit var etSecretKey: EditText
    private lateinit var tvShowSecret: TextView
    private lateinit var btnSave: Button

    private var isSecretVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_credentials)

        etApiKey = findViewById(R.id.etApiKey)
        etSecretKey = findViewById(R.id.etSecretKey)
        tvShowSecret = findViewById(R.id.tvShowSecret)
        btnSave = findViewById(R.id.btnSave)

        tvShowSecret.setOnClickListener { toggleSecretVisibility() }
        btnSave.setOnClickListener { saveCredentials() }
    }

    private fun toggleSecretVisibility() {
        isSecretVisible = !isSecretVisible
        etSecretKey.transformationMethod = if (isSecretVisible) {
            HideReturnsTransformationMethod.getInstance()
        } else {
            PasswordTransformationMethod.getInstance()
        }
        tvShowSecret.text = if (isSecretVisible) "Hide" else "Show"
        etSecretKey.setSelection(etSecretKey.text.length)
    }

    private fun saveCredentials() {
        val apiKey = etApiKey.text.toString().trim()
        val secretKey = etSecretKey.text.toString().trim()
        // TODO: validar campos e armazenar de forma segura (EncryptedSharedPreferences, Keystore...)
    }
}