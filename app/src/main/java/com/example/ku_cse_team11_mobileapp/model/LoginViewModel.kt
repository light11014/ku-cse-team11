package com.example.ku_cse_team11_mobileapp.model

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.data.AuthProvider
import com.example.ku_cse_team11_mobileapp.data.LoginRequest
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var isLoading by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null); private set

    fun login(context: Context, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true; error = null
            val api = AuthProvider.provide(context)
            runCatching {
                val res = api.login(LoginRequest(email, password))
                TokenStore.save(context, res.accessToken, res.refreshToken)
            }.onSuccess { onSuccess() }
                .onFailure { error = it.message ?: "로그인 실패" }
            isLoading = false
        }
    }

    suspend fun logout(context: Context) {
        TokenStore.clear(context)
    }
}