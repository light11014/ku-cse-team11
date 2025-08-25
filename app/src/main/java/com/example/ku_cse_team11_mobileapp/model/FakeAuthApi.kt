package com.example.ku_cse_team11_mobileapp.model

import com.example.ku_cse_team11_mobileapp.data.AuthApi
import com.example.ku_cse_team11_mobileapp.data.LoginRequest
import com.example.ku_cse_team11_mobileapp.data.RefreshRequest
import com.example.ku_cse_team11_mobileapp.data.RegisterRequest
import com.example.ku_cse_team11_mobileapp.data.TokenResponse
import kotlinx.coroutines.delay

class FakeAuthApi : AuthApi {
    override suspend fun login(body: LoginRequest): TokenResponse {
        delay(300)
        if (body.email.isBlank() || body.password != "1234")
            throw IllegalArgumentException("이메일/비밀번호를 확인하세요.")
        return TokenResponse("fake_access_${System.currentTimeMillis()}",
            "fake_refresh_${System.currentTimeMillis()}")
    }

    override suspend fun refresh(body: RefreshRequest): TokenResponse {
        delay(200)
        return TokenResponse("fake_access_${System.currentTimeMillis()}",
            body.refreshToken)
    }

    // ★ 회원가입: 비번 최소 길이/이메일 간단 검증
    override suspend fun register(body: RegisterRequest): TokenResponse {
        delay(400)
        require("@" in body.email) { "올바른 이메일을 입력하세요." }
        require(body.password.length >= 4) { "비밀번호는 4자 이상이어야 합니다." }
        require(body.nickname.isNotBlank()) { "닉네임을 입력하세요." }
        // 가입 성공 시 바로 토큰 발급(자동 로그인 UX)
        return TokenResponse("fake_access_${System.currentTimeMillis()}",
            "fake_refresh_${System.currentTimeMillis()}")
    }
}