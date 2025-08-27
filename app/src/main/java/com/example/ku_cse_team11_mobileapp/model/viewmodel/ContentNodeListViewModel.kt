package com.example.ku_cse_team11_mobileapp.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class NodeListState(
    val isLoading: Boolean = false,
    val items: List<ContentSummary> = emptyList(),
    val error: String? = null
)

class ContentNodeListViewModel(
    private val repo: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NodeListState(isLoading = true))
    val uiState: StateFlow<NodeListState> = _uiState

    fun load(typeApiParam: String, platformFilter: String) {
        _uiState.value = NodeListState(isLoading = true)
        viewModelScope.launch {
            runCatching {
                val ranks = repo.getRanks(typeApiParam)
                val list = ranks.map { it.content }
                if (platformFilter == "ALL") list else list.filter { it.platform == platformFilter }
            }.onSuccess { items ->
                _uiState.value = NodeListState(isLoading = false, items = items)
            }.onFailure { e ->
                _uiState.value = NodeListState(isLoading = false, error = e.message ?: "오류")
            }
        }
    }

    class Factory(private val repo: ContentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ContentNodeListViewModel(repo) as T
    }
}