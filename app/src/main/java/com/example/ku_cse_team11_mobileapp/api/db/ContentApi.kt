package com.example.ku_cse_team11_mobileapp.api.db

import com.example.ku_cse_team11_mobileapp.api.model.ContentDetail
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.api.model.PageResponse
import com.example.ku_cse_team11_mobileapp.api.model.RankResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApi {
    @GET("/api/ranks/{type}/all")
    suspend fun getRanks(
        @Path("type") type: String // 예: "WEBTOON"
    ): List<RankResponse>

    @GET("/api/content/{contentId}")
    suspend fun getContentDetail(
        @Path("contentId") contentId: Int,
        @Query("lang") lang: String? = null // 선택값
    ): ContentDetail

    @GET("/api/search")
    suspend fun search(
        @Query("keyword") keyword: String? = null,         // 2글자 이상
        @Query("contentType") contentType: String? = null, // WEBTOON/WEBNOVEL
        @Query("platform") platform: String? = null,       // KAKAO_WEBTOON 등
        @Query("minEpisode") minEpisode: Int? = null,
        @Query("maxEpisode") maxEpisode: Int? = null,
        @Query("page") page: Int? = 0,                     // 0-base
        @Query("size") size: Int? = 20,
        @Query("lang") lang: String? = null
    ): PageResponse<ContentSummary>
}