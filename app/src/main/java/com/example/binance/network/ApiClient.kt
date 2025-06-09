package com.example.binance.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // 1) Interceptor para log básico de requests/responses
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // 2) Cliente HTTP com o interceptor adicionado
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 3) Instância Retrofit configurada
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)                               // tem de terminar em "/"
        .client(httpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 4) Serviço Retrofit único
    val service: BinanceService =
        retrofit.create(BinanceService::class.java)

    // 5) Funções para Fundos/Balance
    suspend fun addFunds(userId: String, amount: Float): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("${BASE_URL}api/users/$userId/add-funds")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "PUT"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                // Criar JSON body
                val jsonBody = JSONObject()
                jsonBody.put("amount", amount)

                // Enviar dados
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonBody.toString())
                writer.flush()
                writer.close()

                // Verificar resposta
                connection.responseCode == HttpURLConnection.HTTP_OK

            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun getUserBalance(userId: String): Float? {
        return withContext(Dispatchers.IO) {
            try {
                val fullUrl = "${BASE_URL}api/users/$userId"
                Log.d("ApiClient", "Fazendo requisição para: $fullUrl")

                val url = URL(fullUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                Log.d("ApiClient", "Response code: ${connection.responseCode}")

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().readText()
                    Log.d("ApiClient", "Response body: $response")

                    val jsonResponse = JSONObject(response)
                    val balance = jsonResponse.optDouble("balance", 0.0).toFloat()
                    Log.d("ApiClient", "Balance extraído: $balance")

                    balance
                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.readText()
                    Log.e("ApiClient", "Erro HTTP: ${connection.responseCode} - $errorResponse")
                    null
                }
            } catch (e: Exception) {
                Log.e("ApiClient", "Exceção ao buscar balance", e)
                e.printStackTrace()
                null
            }
        }
    }
}