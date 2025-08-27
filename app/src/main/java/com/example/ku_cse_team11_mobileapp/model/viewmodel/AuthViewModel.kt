package com.example.ku_cse_team11_mobileapp.model.viewmodel

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.model.repository.AuthRepository
import com.example.ku_cse_team11_mobileapp.model.repository.extractErrorMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormState(
    val loginId: String = "",
    val name: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

class SignUpViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(FormState())
    val ui: StateFlow<FormState> = _ui

    fun updateLoginId(s: String) = _ui.update { it.copy(loginId = s, error = null, message = null) }
    fun updateName(s: String)    = _ui.update { it.copy(name = s, error = null, message = null) }
    fun updatePassword(s: String)= _ui.update { it.copy(password = s, error = null, message = null) }

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    fun submit() {
        val cur = _ui.value
        if (cur.loginId.isBlank()) return _ui.update { it.copy(error = "아이디를 입력하세요") }
        if (cur.name.isBlank())    return _ui.update { it.copy(error = "이름을 입력하세요") }
        if (cur.password.isBlank())return _ui.update { it.copy(error = "비밀번호를 입력하세요") }

        _ui.update { it.copy(isLoading = true, error = null, message = null) }
        viewModelScope.launch {
            runCatching { repo.signup(cur.loginId, cur.name, cur.password) }
                .onSuccess { res -> _ui.update { it.copy(isLoading = false, message = res.message) } }
                .onFailure { e -> _ui.update { it.copy(isLoading = false, error = extractErrorMessage(e)) } }
        }
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SignUpViewModel(repo) as T
    }
}

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(FormState())
    val ui: StateFlow<FormState> = _ui

    fun updateLoginId(s: String)  = _ui.update { it.copy(loginId = s, error = null, message = null) }
    fun updatePassword(s: String) = _ui.update { it.copy(password = s, error = null, message = null) }

    fun submit() {
        val cur = _ui.value
        if (cur.loginId.isBlank())  return _ui.update { it.copy(error = "아이디를 입력하세요") }
        if (cur.password.isBlank()) return _ui.update { it.copy(error = "비밀번호를 입력하세요") }

        _ui.update { it.copy(isLoading = true, error = null, message = null) }
        viewModelScope.launch {
            runCatching { repo.login(cur.loginId, cur.password) }
                .onSuccess { res -> _ui.update { it.copy(isLoading = false, message = res.message) } }
                .onFailure { e -> _ui.update { it.copy(isLoading = false, error = extractErrorMessage(e)) } }
        }
    }

    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(repo) as T
    }
}