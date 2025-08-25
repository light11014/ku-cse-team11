package com.example.ku_cse_team11_mobileapp.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.data.AuthProvider
import com.example.ku_cse_team11_mobileapp.data.RegisterRequest
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    // 간단 검증 규칙 (원하면 강화)
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

    fun register(
        context: Context,
        email: String,
        password: String,
        confirm: String,
        nickname: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // 클라이언트 검증
            when {
                email.isBlank() || !emailRegex.matches(email) -> { error = "올바른 이메일을 입력하세요."; return@launch }
                nickname.isBlank() -> { error = "닉네임을 입력하세요."; return@launch }
                password.length < 6 -> { error = "비밀번호는 6자 이상이어야 합니다."; return@launch }
                password != confirm -> { error = "비밀번호가 일치하지 않습니다."; return@launch }
            }

            isLoading = true; error = null
            val api = AuthProvider.provide(context)
            runCatching {
                val res = api.register(RegisterRequest(email, password, nickname))
                TokenStore.save(context, res.accessToken, res.refreshToken) // 자동 로그인
            }.onSuccess { onSuccess() }
                .onFailure { error = it.message ?: "회원가입 실패" }
            isLoading = false
        }
    }
}
