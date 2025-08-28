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
    private val contentId: Int,
    private val defaultLang: String? = "kr"
) : ViewModel() {

    sealed interface UiState {
        data object Loading : UiState
        data class Success(val data: ContentDetail) : UiState
        data class Error(val message: String) : UiState
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState
    /** memberId가 나중에 도착해도 다시 로드할 수 있게 분리 */
    fun load(memberId: Long?, lang: String? = defaultLang) { // ← Int? ➜ Long?
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            runCatching { repo.getContentDetail(contentId, lang, memberId) }
                .onSuccess { _uiState.value = UiState.Success(it) }
                .onFailure { _uiState.value = UiState.Error(it.message ?: "오류") }
        }
    }

    // 수동 팩토리
    class Factory(
        private val repo: ContentRepository,
        private val contentId: Int,
        private val lang: String? = "kr"
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ContentDetailViewModel::class.java))
            return ContentDetailViewModel(repo, contentId, lang) as T
        }
    }
}