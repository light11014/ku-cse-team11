package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.api.db.ContentApi
import com.example.ku_cse_team11_mobileapp.api.model.ContentDetail
import com.example.ku_cse_team11_mobileapp.api.model.RankResponse

class ContentRepositoryImpl(
    private val api: ContentApi
) : ContentRepository {
    override suspend fun getRanks(type: String): List<RankResponse> =
        api.getRanks(type)

    override suspend fun getContentDetail(contentId: Int, lang: String?): ContentDetail =
        api.getContentDetail(contentId, lang)

    override suspend fun search(
        keyword: String?, contentType: String?, platform: String?,
        minEpisode: Int?, maxEpisode: Int?, page: Int?, size: Int?, lang: String?
    ) = api.search(keyword, contentType, platform, minEpisode, maxEpisode, page, size, lang)
}