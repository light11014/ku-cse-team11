package com.example.ku_cse_team11_mobileapp.api.db

import com.example.ku_cse_team11_mobileapp.api.model.ContentDetail
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.api.model.PageResponse
import com.example.ku_cse_team11_mobileapp.api.model.RankResponse
import com.example.ku_cse_team11_mobileapp.api.model.TierRequest
import com.example.ku_cse_team11_mobileapp.api.model.TierResponse
import com.example.ku_cse_team11_mobileapp.model.community.ContentComment
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApi {
    @GET("/api/ranks/{type}/all")
    suspend fun getRanks(
        @Path("type") type: String // 예: "WEBTOON"
    ): List<RankResponse>

    @GET("api/ranks/{type}/all")
    suspend fun getRanksAll(
        @Path("type") type: String,          // "WEBTOON" / "WEBNOVEL"
        @Query("limit") limit: Int? = null   // 옵션
    ): List<RankResponse>

    @GET("api/content/{contentId}")
    suspend fun getContentDetail(
        @Path("contentId") contentId: Int,
        @Query("lang") lang: String? = null,            // "kr", "en", "ja" ...
        @Query("memberId") memberId: Long? = null        // 로그인 시 전달
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

    @GET("api/content/{contentId}/comments")
    suspend fun getComments(
        @Path("contentId") contentId: Int
    ): List<ContentComment>

    @POST("api/content/{contentId}/comments")
    suspend fun postComment(
        @Path("contentId") contentId: Int,
        @Query("userId") userId: Long,
        @Query("body") body: String
    ): ContentComment

    @GET("api/favorite")
    suspend fun getFavoriteCountRaw(
        @Query("contentId") contentId: Int
    ): ResponseBody
    @POST("api/tier")
    suspend fun postTier(@Body req: TierRequest): TierResponse
}