package com.example.binance.dto;

public class LoginResponse {
    private String userId;
    private String username;
    // podes adicionar token JWT ou outros campos
    public LoginResponse(String userId, String username) {
        this.userId = userId;
        this.username = username;
    }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
}
