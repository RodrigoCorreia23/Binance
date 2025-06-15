package com.example.binance.network

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREF_NAME = "APP_PREFS"  // Mesmo que HomeActivity
    private const val KEY_USER_ID = "USER_ID"  // Mesmo que HomeActivity

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getUserId(context: Context): String? {
        val prefs = getSharedPreferences(context)

        // Tenta primeiro como String
        return try {
            prefs.getString(KEY_USER_ID, null)
        } catch (e: ClassCastException) {
            // Se falhar, tenta como Long e converte para String
            try {
                val longUserId = prefs.getLong(KEY_USER_ID, -1L)
                if (longUserId != -1L) longUserId.toString() else null
            } catch (e2: ClassCastException) {
                // Se ainda falhar, limpa e retorna null
                prefs.edit().remove(KEY_USER_ID).apply()
                null
            }
        }
    }
}