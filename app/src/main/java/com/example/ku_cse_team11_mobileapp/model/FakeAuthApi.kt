package com.example.ku_cse_team11_mobileapp.model

import com.example.ku_cse_team11_mobileapp.data.AuthApi
import com.example.ku_cse_team11_mobileapp.data.LoginRequest
import com.example.ku_cse_team11_mobileapp.data.RefreshRequest
import com.example.ku_cse_team11_mobileapp.data.TokenResponse
import kotlinx.coroutines.delay

class FakeAuthApi : AuthApi {
    override suspend fun login(body: LoginRequest): TokenResponse {
        delay(300)
        if (body.email.isBlank() || body.password != "1234") throw IllegalArgumentException("이메일/비밀번호를 확인하세요.")
        return TokenResponse(accessToken = "fake_access_${System.currentTimeMillis()}",
            refreshToken = "fake_refresh_${System.currentTimeMillis()}")
    }
    override suspend fun refresh(body: RefreshRequest): TokenResponse {
        delay(200)
        return TokenResponse(accessToken = "fake_access_${System.currentTimeMillis()}",
            refreshToken = body.refreshToken)
    }
}