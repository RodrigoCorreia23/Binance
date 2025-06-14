package com.example.binance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.binance.network.ApiCredentialsRequest
import com.example.binance.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ApiCredentialsActivity : AppCompatActivity() {

    private lateinit var etApiKey: EditText
    private lateinit var etSecretKey: EditText
    private lateinit var tvShowSecret: TextView
    private lateinit var btnSave: Button
    private lateinit var tvStatus: TextView

    private var isSecretVisible = false
    private lateinit var userId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_credentials)

        // 1) recupera userId do Intent
        userId = intent.getStringExtra("USER_ID")
            ?: run {
                Toast.makeText(this, getString(R.string.invalid_user), Toast.LENGTH_LONG).show()
                finish()
                return
            }

        etApiKey     = findViewById(R.id.etApiKey)
        etSecretKey  = findViewById(R.id.etSecretKey)
        tvShowSecret = findViewById(R.id.tvShowSecret)
        btnSave      = findViewById(R.id.btnSave)
        tvStatus     = findViewById(R.id.tvStatus)

        tvShowSecret.setOnClickListener { toggleSecretVisibility() }
        btnSave.setOnClickListener { saveCredentials() }
    }

    private fun toggleSecretVisibility() {
        isSecretVisible = !isSecretVisible
        etSecretKey.transformationMethod =
            if (isSecretVisible)
                HideReturnsTransformationMethod.getInstance()
            else
                PasswordTransformationMethod.getInstance()
        tvShowSecret.text = if (isSecretVisible) "Hide" else "Show"
        etSecretKey.setSelection(etSecretKey.text.length)
    }

    private fun saveCredentials() {
        val apiKey    = etApiKey.text.toString().trim()
        val secretKey = etSecretKey.text.toString().trim()

        // 2) validação simples
        when {
            apiKey.isEmpty() -> {
                etApiKey.error = getString(R.string.mandatory_api_key)
                return
            }
            secretKey.isEmpty() -> {
                etSecretKey.error = getString(R.string.mandatory_secret_key)
                return
            }

        }

        tvStatus.text = ""
        etApiKey.error = null
        etSecretKey.error = null

        // 3) envia para o servidor
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = RetrofitClient.binanceService
                    .saveCredentials(ApiCredentialsRequest(userId, apiKey, secretKey))

                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful) {
                        Toast.makeText(
                            this@ApiCredentialsActivity,
                            getString(R.string.added_successfully),
                            Toast.LENGTH_LONG
                        ).show()

                        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                        val hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)

                        val nextIntent = if (!hasCompletedOnboarding) {
                            Intent(this@ApiCredentialsActivity, OnboardingActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                                putExtra("USERNAME", intent.getStringExtra("USERNAME"))
                                putExtra("EMAIL", prefs.getString("EMAIL", ""))
                            }
                        } else {
                            Intent(this@ApiCredentialsActivity, OnboardingActivity::class.java).apply {
                                putExtra("USER_ID", userId)
                                putExtra("USERNAME", intent.getStringExtra("USERNAME"))
                                putExtra("EMAIL", prefs.getString("EMAIL", ""))
                                flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        }

                        nextIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(nextIntent)

                    } else {
                        val err = resp.errorBody()?.string().orEmpty()
                        tvStatus.text = "Error ${resp.code()}: $err"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvStatus.text =  getString(R.string.network_error) + "${e.localizedMessage}"
                }
            }
        }
    }
}
