package com.example.binance.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory

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
        .baseUrl(BASE_URL)                               // tem de terminar em “/”
        .client(httpClient)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 4) Serviço Retrofit único
    val service: BinanceService =
        retrofit.create(BinanceService::class.java)
}
