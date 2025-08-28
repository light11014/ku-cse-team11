package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.api.db.ContentApi
import com.example.ku_cse_team11_mobileapp.api.model.ContentDetail
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.api.model.RankResponse
import java.util.concurrent.ConcurrentHashMap

class ContentRepositoryImpl(
    private val api: ContentApi
) : ContentRepository {

    private data class CacheEntry<T>(val data: T, val ts: Long)
    private val ranksCache = ConcurrentHashMap<String, CacheEntry<List<ContentSummary>>>()
    private val TTL_MS = 5 * 60_000 // 5분 캐시

    private fun key(type: String, platform: String, limit: Int?) =
        "$type|$platform|${limit ?: -1}"

    override suspend fun getRanks(
        type: String,
        platform: String,
        limit: Int?
    ): List<ContentSummary> {
        val k = key(type, platform, limit)
        val now = System.currentTimeMillis()

        // 캐시 히트면 바로 반환
        ranksCache[k]?.let { if (now - it.ts < TTL_MS) return it.data }

        // ✅ 서버는 /all만 지원 → platform은 무시
        val fresh = api.getRanksAll(type, limit).map { it.content }

        // (선택) 클라이언트에서 플랫폼 탭 필터링
        val filtered = if (!platform.equals("ALL", ignoreCase = true)) {
            fresh.filter { it.platform.equals(platform, ignoreCase = true) }
        } else {
            fresh
        }

        ranksCache[k] = CacheEntry(filtered, now)
        return filtered
    }

    override suspend fun getContentDetail(contentId: Int, lang: String?): ContentDetail =
        api.getContentDetail(contentId, lang)

    override suspend fun search(
        keyword: String?, contentType: String?, platform: String?,
        minEpisode: Int?, maxEpisode: Int?, page: Int?, size: Int?, lang: String?
    ) = api.search(keyword, contentType, platform, minEpisode, maxEpisode, page, size, lang)

    override suspend fun getComments(contentId: Int) =
        api.getComments(contentId)

    override suspend fun addComment(contentId: Int, userId: Long, body: String) =
        api.postComment(contentId, userId, body)

    override suspend fun getFavoriteCount(contentId: Int): Int {
        val text = api.getFavoriteCountRaw(contentId).string().trim()
        return text.toIntOrNull() ?: 0
    }
}