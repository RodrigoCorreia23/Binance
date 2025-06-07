package com.example.binance.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * APIs para configurar o bot (BotSettings).
 */
public interface BotSettingsApiService {

    @POST("/api/bot-settings")
    Call<BotSettingsResponse> createBotSettings(@Body BotSettingsRequest body);

    @PUT("/api/bot-settings/user/{userId}")
    Call<BotSettingsResponse> updateBotSettings(
            @Path("userId") String userId,
            @Body BotSettingsRequest body
    );

    @GET("/api/bot-settings/user/{userId}")
    Call<BotSettingsResponse> getBotSettings(@Path("userId") String userId);
}

