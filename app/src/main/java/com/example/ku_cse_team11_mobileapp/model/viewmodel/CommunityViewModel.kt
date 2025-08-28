// model/viewmodel/CommunityViewModel.kt
package com.example.ku_cse_team11_mobileapp.model.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.model.community.ContentComment
import com.example.ku_cse_team11_mobileapp.model.data.SessionManager
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.min

class CommunityViewModel(
    private val repo: ContentRepository,
    private val session: SessionManager,
    private val contentId: Int
) : ViewModel() {

    private val pageSize = 20

    data class UiState(
        val isLoading: Boolean = false,     // 초기 로드/작성 로딩
        val isRefreshing: Boolean = false,  // 당겨서 새로고침 로딩
        val error: String? = null,
        val items: List<ContentComment> = emptyList(), // 전체 목록
        val visibleCount: Int = 0,          // 화면에 보일 개수 (무한 스크롤용)
        val input: String = ""
    ) {
        val visibleItems: List<ContentComment> get() = items.take(visibleCount)
        val canLoadMore: Boolean get() = visibleCount < items.size
    }

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    init { initialLoad() }

    fun setInput(s: String) = _ui.update { it.copy(input = s) }

    private fun initialLoad() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.getComments(contentId) }
                .onSuccess { list ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            items = list,
                            visibleCount = min(pageSize, list.size)
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "불러오기 실패") }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _ui.update { it.copy(isRefreshing = true, error = null) }
            runCatching { repo.getComments(contentId) }
                .onSuccess { list ->
                    _ui.update {
                        it.copy(
                            isRefreshing = false,
                            items = list,
                            visibleCount = min(pageSize, list.size)
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isRefreshing = false, error = e.message ?: "새로고침 실패") }
                }
        }
    }

    fun loadMore() {
        _ui.update { st ->
            if (!st.canLoadMore) st
            else st.copy(visibleCount = min(st.visibleCount + pageSize, st.items.size))
        }
    }

    fun submit() {
        viewModelScope.launch {
            val body = _ui.value.input.trim()
            if (body.isBlank()) return@launch
            val memberId = session.memberIdFlow.first()
                ?: run { _ui.update { it.copy(error = "로그인 후 이용해주세요") }; return@launch }

            _ui.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.addComment(contentId, memberId, body) }
                .onSuccess { new ->
                    _ui.update { st ->
                        val newItems = listOf(new) + st.items
                        st.copy(
                            isLoading = false,
                            input = "",
                            items = newItems,
                            visibleCount = min(st.visibleCount + 1, newItems.size) // 새 글 즉시 노출
                        )
                    }
                }
                .onFailure { e ->
                    _ui.update { it.copy(isLoading = false, error = e.message ?: "작성 실패") }
                }
        }
    }

    class Factory(
        private val repo: ContentRepository,
        private val session: SessionManager,
        private val contentId: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CommunityViewModel(repo, session, contentId) as T
    }
}
