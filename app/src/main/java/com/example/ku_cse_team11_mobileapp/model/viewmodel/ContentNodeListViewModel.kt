package com.example.ku_cse_team11_mobileapp.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SortTab(val label: String) { RANK("랭킹"), TIER("티어") }
data class ContentNodeListUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<ContentSummary> = emptyList(),
    val sort: SortTab = SortTab.RANK
)

class ContentNodeListViewModel(
    private val repo: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentNodeListUiState())
    val uiState: StateFlow<ContentNodeListUiState> = _uiState

    /**
     * @param contentType  "WEBTOON" | "WEBNOVEL"
     * @param platform     "ALL" | "KAKAO_WEBTOON" | ...  (TIER 모드에선 무시됨)
     * @param sort         랭킹/티어
     */

    fun load(contentType: String, platform: String, sort: SortTab) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, sort = sort) }

            // "ALL"은 null로 치환(서버가 받지 않는 경우 대비)
            val platformParam: String =
                if (platform.equals("ALL", true)) "ALL" else platform

            runCatching {
                when (sort) {
                    SortTab.RANK -> {
                        // ✅ 플랫폼까지 같이 전달
                        // (Repo 시그니처 예: getRanks(type: String, platform: String?, limit: Int? = null))
                        repo.getRanks(contentType, platformParam /*, limit = null */)
                    }
                    SortTab.TIER -> {
                        repo.getTierSorted(contentType, platformParam)
                    }
                }
            }.onSuccess { list ->
                _uiState.update { it.copy(isLoading = false, items = list) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "로드 실패") }
            }
        }
    }
    @Suppress("UNCHECKED_CAST")
    class Factory(private val repo: ContentRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ContentNodeListViewModel(repo) as T
    }
}