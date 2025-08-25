package com.example.ku_cse_team11_mobileapp.model.community

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ku_cse_team11_mobileapp.domain.CommunityRepository
import kotlinx.coroutines.launch
import com.example.ku_cse_team11_mobileapp.data.CommunityRepositoryProvider
import com.example.ku_cse_team11_mobileapp.domain.Post

class CommunityViewModel(
    private val contentId: Long,
    private val repo: CommunityRepository
) : ViewModel() {

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    // (선택) 로딩/에러 상태도 추가 가능
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun loadPosts() {
        viewModelScope.launch {
            isLoading = true; error = null
            runCatching { repo.getPosts(contentId) }
                .onSuccess { posts = it }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    fun addPost(author: String, content: String) {
        viewModelScope.launch {
            if (content.isBlank()) return@launch
            isLoading = true; error = null
            runCatching { repo.addPost(contentId, author, content) }
                .onSuccess { loadPosts() }
                .onFailure { error = it.message }
            isLoading = false
        }
    }
}

class CommunityViewModelFactory(
    private val contentId: Long,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = CommunityRepositoryProvider.provide(context)
        return CommunityViewModel(contentId, repo) as T
    }
}
