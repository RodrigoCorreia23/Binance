package com.example.binance.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * RetrofitClient: objeto singleton que configura Retrofit e expõe
 * a interface BinanceService (contendo login, signup, ping, hasCredentials, getUserProfile, updateUserProfile, etc.).
 *
 * Ajuste BASE_URL conforme seu ambiente:
 *  - Emulador Android Studio: "http://10.0.2.2:8080/"
 *  - Dispositivo físico (mesma rede local): "http://<IP_DO_SEU_PC>:8080/"
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    // 1) Interceptor básico para logar requests/responses no Logcat (nível BASIC)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // 2) OkHttpClient que adiciona o interceptor acima
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // 3) Instância única de Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)  // Atenção: mantenha a barra final "/"
        .client(httpClient)
        // Converte Strings simples, caso algum endpoint retorne apenas texto
        .addConverterFactory(ScalarsConverterFactory.create())
        // Converte JSON <-> objetos Kotlin (Gson)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val binanceService: BinanceService =
        retrofit.create(BinanceService::class.java)

    // 4) Expor a interface BinanceService (com todos os endpoints do seu backend)
    val apiService: BinanceService =
        retrofit.create(BinanceService::class.java)

    /**
     * Para endpoints de credenciais:
     *  - POST   /api/credentials
     *  - GET    /api/credentials/{userId}
     *  - GET    /api/credentials/{userId}/balance
     */
    val credentialsService: CredentialsApiService =
        retrofit.create(CredentialsApiService::class.java)

    /**
     * Para gerenciar o estado do bot:
     *  - GET    /api/bot-state/user/{userId}
     *  - POST   /api/bot-state
     *  - PUT    /api/bot-state/user/{userId}
     */
    val botStateService: BotStateApiService =
        retrofit.create(BotStateApiService::class.java)

    /**
     * Para gerenciar as configurações do bot:
     *  - GET    /api/bot-settings/user/{userId}
     *  - POST   /api/bot-settings
     *  - PUT    /api/bot-settings/user/{userId}
     */
    val botSettingsService: BotSettingsApiService =
        retrofit.create(BotSettingsApiService::class.java)

    /**
     * Caso queira obter dados públicos da Binance diretamente (candles, lista de símbolos etc.),
     * configure outra interface e outro Retrofit se necessário. Aqui só mostramos a propriedade:
     */
    val publicBinanceService: PublicBinanceApiService =
        retrofit.create(PublicBinanceApiService::class.java)
}
