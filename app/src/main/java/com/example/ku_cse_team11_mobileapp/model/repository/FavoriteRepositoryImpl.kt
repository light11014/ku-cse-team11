package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.api.favorite.FavoriteApi
import com.example.ku_cse_team11_mobileapp.api.favorite.FavoritesStore
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.model.data.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class FavoritesRepositoryImpl(
    private val api: FavoriteApi,
    private val store: FavoritesStore,
    private val session: SessionManager,
    private val contentRepo: ContentRepository
) : FavoritesRepository {

    override val favoriteIdsFlow: Flow<Set<Long>> = store.idsFlow

    private suspend fun memberId(): Long =
        session.memberIdFlow.first() ?: throw IllegalStateException("로그인 후 이용해주세요")

    override suspend fun isFavorite(contentId: Long): Boolean = runCatching {
        api.isFavorite(memberId(), contentId)
    }.getOrElse {
        // 서버 호출 실패 시 로컬 캐시에서 추정
        store.idsFlow.first().contains(contentId)
    }

    override suspend fun toggleFavorite(contentId: Long): Boolean {
        val mid = memberId()
        // 현재 상태 파악
        val cur = runCatching { api.isFavorite(mid, contentId) }
            .getOrElse { store.idsFlow.first().contains(contentId) }

        return if (cur) {
            // 해제
            runCatching { api.removeFavorite(mid, contentId) }.fold(
                onSuccess = {
                    store.remove(contentId)
                    false
                },
                onFailure = { e ->
                    throw toUserError(e, fallback = "즐겨찾기 해제 실패")
                }
            )
        } else {
            // 등록
            runCatching { api.addFavorite(mid, contentId) }.fold(
                onSuccess = {
                    store.add(contentId)
                    true
                },
                onFailure = { e ->
                    throw toUserError(e, fallback = "즐겨찾기 추가 실패")
                }
            )
        }
    }

    override suspend fun refreshFromServerIfPossible() {
        val mid = memberId()
        runCatching { api.listFavorites(mid) }
            .onSuccess { list -> store.setAll(list.map { it.contentId }.toSet()) }
            .onFailure { /* 서버에 목록 API 없거나 실패하면 무시 */ }
    }

    override suspend fun listFavoriteSummaries(lang: String?): List<ContentSummary> = withContext(Dispatchers.IO) {
        // 1) 서버 목록 동기화 시도 (가능하면)
        refreshFromServerIfPossible()

        // 2) 로컬 캐시에서 ID → 상세 호출 → Summary로 매핑
        val ids = store.idsFlow.first().toList()
        ids.mapNotNull { id ->
            runCatching {
                val d = contentRepo.getContentDetail(id.toInt(), lang)
                ContentSummary(
                    id = d.id,
                    title = d.title,
                    authors = d.authors ?: "",
                    thumbnailUrl = d.thumbnailUrl,
                    platform = d.platform ?: "",
                    views = d.views,
                    tier = d.avgTier
                )
            }.getOrNull()
        }
    }
}

private fun toUserError(e: Throwable, fallback: String): Throwable {
    if (e is HttpException) {
        val msg = e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
        if (msg != null) return IllegalStateException(msg)
        return IllegalStateException("요청 실패 (${e.code()})")
    }
    return IllegalStateException(e.message ?: fallback)
}