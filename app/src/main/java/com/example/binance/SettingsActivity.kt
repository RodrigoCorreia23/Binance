package com.example.binance

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.binance.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        bottomNav = findViewById(R.id.bottom_nav)

        // NavMenu
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this@SettingsActivity, HomeActivity::class.java))
                    true
                }
                R.id.nav_refresh -> {
                    startActivity(Intent(this@SettingsActivity, TradeHistoryActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this@SettingsActivity, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@SettingsActivity, BotSettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
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
