package com.example.binance

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.binance.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    fun onIdiomaPortugues(view: View) {
        changeLanguage("pt")
    }

    fun onIdiomaIngles(view: View) {
        changeLanguage("en")
    }

    private fun changeLanguage(lang: String) {
        val locale = LocaleListCompat.forLanguageTags(lang)
        AppCompatDelegate.setApplicationLocales(locale)

        // Guardar para uso futuro (opcional)
        getSharedPreferences("settings", MODE_PRIVATE)
            .edit()
            .putString("lang", lang)
            .apply()
    }
}
