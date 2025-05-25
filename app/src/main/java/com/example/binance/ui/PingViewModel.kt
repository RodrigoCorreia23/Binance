package com.example.binance.ui

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binance.network.ApiClient
import kotlinx.coroutines.launch

class PingViewModel : ViewModel() {
    var status = mutableStateOf("Conectando…")
        private set

    init {
        viewModelScope.launch {
            status.value = try {
                "Conectado: " + ApiClient.service.ping()
            } catch(e: Exception) {
                "Erro: ${e.localizedMessage}"
            }
        }
    }
}
