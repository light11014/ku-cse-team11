package com.example.ku_cse_team11_mobileapp.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/login")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("/api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    // ★ 회원가입
    @POST("/api/auth/register")
    suspend fun register(@Body body: RegisterRequest): TokenResponse
}

data class LoginRequest(val email: String, val password: String)
data class RefreshRequest(val refreshToken: String)

// ★ 회원가입 요청 (필요 필드 추가 가능)
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String
)

// 서버가 토큰을 즉시 발급해주면 자동 로그인 UX 가능
data class TokenResponse(val accessToken: String, val refreshToken: String)