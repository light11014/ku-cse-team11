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

class ContentNodeListViewModel(
    private val repo: ContentRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val items: List<ContentSummary> = emptyList(),
        val error: String? = null,
        val type: String = "WEBTOON",
        val platform: String = "ALL",
        val limit: Int? = null,
        val empty: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var lastKey: String? = null
    private var loadJob: Job? = null

    /** 탭 변경 시 호출. 같은 파라미터면 재요청을 생략해 중복 호출을 줄임 */
    fun load(type: String, platform: String, limit: Int? = null) {
        val key = "$type|$platform|${limit ?: -1}"
        if (lastKey == key && _uiState.value.items.isNotEmpty()) {
            // 동일 키 & 이미 데이터가 있으면 재요청 생략 (레포 캐시 + UI 즉시 반응)
            return
        }
        lastKey = key

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, type = type, platform = platform, limit = limit) }

            runCatching { repo.getRanks(type, platform, limit) }
                .onSuccess { list ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = list,
                            error = null,
                            empty = list.isEmpty(),
                            type = type,
                            platform = platform,
                            limit = limit
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = emptyList(),
                            error = (e.message ?: "알 수 없는 오류가 발생했어요"),
                            empty = true
                        )
                    }
                }
        }
    }

    /** 사용자가 당겨서 새로고침 같은 액션을 눌렀을 때 */
    fun refresh() {
        // 같은 키로 강제 재호출 (레포가 TTL 내 캐시를 줄 수 있음)
        val s = _uiState.value
        // 새로고침 의도 표시
        _uiState.update { it.copy(isLoading = true, error = null) }
        // lastKey를 초기화해 load가 항상 수행되도록
        lastKey = null
        load(s.type, s.platform, s.limit)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    class Factory(
        private val repo: ContentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ContentNodeListViewModel::class.java))
            return ContentNodeListViewModel(repo) as T
        }
    }
}
