package com.example.binance.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding  // ← import correto

@Composable
fun PingScreen(status: String) {
    Text(
        text = status,
        modifier = Modifier
            .padding(16.dp),                // agora funciona
        style = MaterialTheme.typography.bodyLarge
    )
}
