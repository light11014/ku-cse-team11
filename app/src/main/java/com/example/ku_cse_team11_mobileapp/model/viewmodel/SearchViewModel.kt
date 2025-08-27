package com.example.ku_cse_team11_mobileapp.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ui/search/SearchViewModel.kt
data class SearchFilters(
    val keyword: String? = null,
    val contentType: String? = null,
    val platform: String? = null,
    val minEpisode: Int? = null,
    val maxEpisode: Int? = null,
    val page: Int = 0,
    val size: Int = 20,
    val lang: String? = "kr"
)

data class SearchState(
    val isLoading: Boolean = false,
    val results: List<ContentSummary> = emptyList(),
    val error: String? = null,
    val page: Int = 0,
    val size: Int = 20,
    val totalPages: Int = 0,
    val last: Boolean = true,  // 기본 true면 초기에 “더보기” 안 뜸
    val filters: SearchFilters = SearchFilters()
)

class SearchViewModel(private val repo: ContentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchState())
    val uiState: StateFlow<SearchState> = _uiState

    fun updateFilters(transform: (SearchFilters) -> SearchFilters) {
        _uiState.update { it.copy(filters = transform(it.filters)) }
    }

    fun setFilters(newFilters: SearchFilters) {
        _uiState.update { it.copy(filters = newFilters) }
    }
    /** 첫 페이지 검색(초기화) */
    fun searchFirstPage() {
        val f = _uiState.value.filters
        _uiState.value = _uiState.value.copy(
            isLoading = true, error = null, results = emptyList(),
            page = 0, totalPages = 0, last = true
        )
        viewModelScope.launch {
            runCatching {
                repo.search(
                    keyword = f.keyword?.takeIf { it.length >= 2 },
                    contentType = f.contentType,
                    platform = f.platform,
                    minEpisode = f.minEpisode,
                    maxEpisode = f.maxEpisode,
                    page = 0,
                    size = f.size,
                    lang = f.lang
                )
            }.onSuccess { page ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = page.content,
                    page = page.number,
                    size = page.size,
                    totalPages = page.totalPages,
                    last = page.last
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "검색 오류")
            }
        }
    }
    // SearchViewModel.kt
    fun clearResults() {
        _uiState.value = _uiState.value.copy(
            results = emptyList(),
            error = null,
            page = 0,
            totalPages = 0,
            last = true
        )
    }

    /** 다음 페이지 이어받기 */
    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.last) return
        val next = state.page + 1
        val f = state.filters.copy(page = next)

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            runCatching {
                repo.search(
                    keyword = f.keyword?.takeIf { it.length >= 2 },
                    contentType = f.contentType,
                    platform = f.platform,
                    minEpisode = f.minEpisode,
                    maxEpisode = f.maxEpisode,
                    page = f.page,
                    size = f.size,
                    lang = f.lang
                )
            }.onSuccess { page ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = _uiState.value.results + page.content,
                    page = page.number,
                    size = page.size,
                    totalPages = page.totalPages,
                    last = page.last
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "검색 오류")
            }
        }
    }

    class Factory(private val repo: ContentRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(repo) as T
    }
}
