package com.example.binance.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.binance.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FirebaseService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Novo token gerado: $token")

        val prefs = getSharedPreferences("fcm", MODE_PRIVATE)
        prefs.edit().putString("pendingToken", token).apply()

        sendTokenToBackend(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, body: String?) {
        val builder = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title ?: "Notificação")
            .setContentText(body ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "Notificações", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        manager.notify(0, builder.build())
    }

    private fun sendTokenToBackend(token: String) {
        val userId = SessionManager.getUserId(this)
        if (userId != null) {
            Log.d("FCM", "Token a enviar para backend (userId: $userId): $token")
            val retrofit = Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/") // localhost na Android Emulator
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val api = retrofit.create(UserApiService::class.java)
            val request = FcmTokenRequest(token)

            api.updateFcmToken(userId, request).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d("FCM", "Token enviado para backend. Código: ${response.code()}")
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.e("FCM", "Erro ao enviar token", t)
                }
            })
        } else {
            Log.e("FCM", "User ID é nulo, token não enviado.")
        }
    }
}
