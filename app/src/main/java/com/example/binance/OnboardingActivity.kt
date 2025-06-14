package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.example.binance.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button

    // Indicadores
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initViews()
        setupClickListeners()
        updateIndicators()
    }

    private fun initViews() {
        viewFlipper = findViewById(R.id.viewFlipper)
        btnNext = findViewById(R.id.btnNext)
        btnBack = findViewById(R.id.btnBack)

        // Indicadores
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        indicator3 = findViewById(R.id.indicator3)

        // Esconder botão "Voltar" na primeira página
        btnBack.visibility = View.GONE
    }

    private fun setupClickListeners() {
        btnNext.setOnClickListener {
            if (viewFlipper.displayedChild < viewFlipper.childCount - 1) {
                viewFlipper.showNext()
                updateIndicators()
                updateButtons()
            } else {
                finishOnboarding()
            }
        }

        btnBack.setOnClickListener {
            if (viewFlipper.displayedChild > 0) {
                viewFlipper.showPrevious()
                updateIndicators()
                updateButtons()
            }
        }
    }

    private fun updateIndicators() {
        // Reset todos os indicadores
        indicator1.setBackgroundColor(getColor(android.R.color.darker_gray))
        indicator2.setBackgroundColor(getColor(android.R.color.darker_gray))
        indicator3.setBackgroundColor(getColor(android.R.color.darker_gray))

        // Ativar o indicador atual
        when (viewFlipper.displayedChild) {
            0 -> indicator1.setBackgroundColor(getColor(android.R.color.holo_blue_bright))
            1 -> indicator2.setBackgroundColor(getColor(android.R.color.holo_blue_bright))
            2 -> indicator3.setBackgroundColor(getColor(android.R.color.holo_blue_bright))
        }
    }

    private fun updateButtons() {
        val currentPage = viewFlipper.displayedChild
        val totalPages = viewFlipper.childCount

        // Mostrar/esconder botão "Voltar"
        btnBack.visibility = if (currentPage == 0) View.GONE else View.VISIBLE

        // Mudar texto do botão na última página
        if (currentPage == totalPages - 1) {
            btnNext.text = getString(R.string.finish)
        } else {
            btnNext.text = getString(R.string.next)
        }
    }

    private fun finishOnboarding() {
        // Marcar que o onboarding foi visto
        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        prefs.edit {
            putBoolean("has_completed_onboarding", true)
        }

        // Obter dados do usuário dos extras (passados da LoginActivity)
        val userId = intent.getStringExtra("USER_ID")
        val username = intent.getStringExtra("USERNAME")
        val email = intent.getStringExtra("EMAIL")

        // Verificar se o usuário tem credenciais da Binance
        if (userId != null) {
            checkBinanceCredentialsAndNavigate(userId, username, email)
        } else {
            // Fallback - ir para ApiCredentialsActivity
            navigateToApiCredentials(userId, username)
        }
    }

    private fun checkBinanceCredentialsAndNavigate(userId: String, username: String?, email: String?) {
        // Desabilitar botão para evitar múltiplos cliques
        btnNext.isEnabled = false
        btnNext.text = "Carregando..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val check = RetrofitClient.binanceService.hasCredentials(userId)

                withContext(Dispatchers.Main) {
                    if (check.isSuccessful) {
                        val hasCreds = check.body()?.hasCredentials == true

                        if (hasCreds) {
                            navigateToHome(userId, username, email)
                        } else {
                            navigateToApiCredentials(userId, username)
                        }
                    } else {
                        // Em caso de erro, ir para ApiCredentials como fallback
                        navigateToApiCredentials(userId, username)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Em caso de erro, ir para ApiCredentials como fallback
                    navigateToApiCredentials(userId, username)
                }
            }
        }
    }

    private fun navigateToHome(userId: String, username: String?, email: String?) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USERNAME", username)
            putExtra("EMAIL", email)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToApiCredentials(userId: String?, username: String?) {
        val intent = Intent(this, ApiCredentialsActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USERNAME", username)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}