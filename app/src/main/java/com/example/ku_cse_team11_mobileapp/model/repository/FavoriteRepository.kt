package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import kotlinx.coroutines.flow.Flow


interface FavoritesRepository {
    val favoriteIdsFlow: Flow<Set<Long>>
    suspend fun isFavorite(contentId: Long): Boolean
    suspend fun toggleFavorite(contentId: Long): Boolean   // 토글 후 최종 상태 반환
    suspend fun refreshFromServerIfPossible()              // 서버 목록 → 로컬 동기화(가능하면)
    suspend fun listFavoriteSummaries(lang: String? = "kr"): List<ContentSummary>
}
