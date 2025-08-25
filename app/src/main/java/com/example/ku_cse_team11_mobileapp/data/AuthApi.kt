package com.example.ku_cse_team11_mobileapp.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse
    @POST("/api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse
}

data class LoginRequest(val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)
data class TokenResponse(val accessToken: String, val refreshToken: String)