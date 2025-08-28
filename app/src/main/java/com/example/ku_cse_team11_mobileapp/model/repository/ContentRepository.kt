package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.api.model.ContentDetail
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.api.model.PageResponse
import com.example.ku_cse_team11_mobileapp.api.model.RankResponse
import com.example.ku_cse_team11_mobileapp.model.community.ContentComment

interface ContentRepository {
    suspend fun getRanks(type: String, platform: String, limit: Int? = null): List<ContentSummary>
    suspend fun getContentDetail(contentId: Int, lang: String? = null): ContentDetail

    suspend fun search(
        keyword: String? = null,
        contentType: String? = null,
        platform: String? = null,
        minEpisode: Int? = null,
        maxEpisode: Int? = null,
        page: Int? = 0,
        size: Int? = 20,
        lang: String? = null
    ): PageResponse<ContentSummary>

    suspend fun getComments(contentId: Int): List<ContentComment>
    suspend fun addComment(contentId: Int, userId: Long, body: String): ContentComment

    suspend fun getFavoriteCount(contentId: Int): Int
}