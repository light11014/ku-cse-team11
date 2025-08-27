package com.example.ku_cse_team11_mobileapp.model.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.api.model.ContentDetail
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContentDetailViewModel(
    private val repo: ContentRepository,
    private val contentId: Int
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val data: ContentDetail) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState

    init { load() }

    fun load(lang: String? = "kr") {
        viewModelScope.launch {
            runCatching { repo.getContentDetail(contentId, lang) }
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "알 수 없는 오류") }
        }
    }

    // 수동 팩토리
    class Factory(
        private val repo: ContentRepository,
        private val contentId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ContentDetailViewModel(repo, contentId) as T
        }
    }
}