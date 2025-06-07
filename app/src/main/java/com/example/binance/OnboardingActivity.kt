package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewFlipper: ViewFlipper
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button

    // Indicadores
    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var indicator3: View
    private lateinit var indicator4: View

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
        val prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
        prefs.edit {
            putBoolean("has_seen_onboarding", true).apply()

            startActivity(Intent(this@OnboardingActivity, HomeActivity::class.java))
            finish()
        }
    }
}