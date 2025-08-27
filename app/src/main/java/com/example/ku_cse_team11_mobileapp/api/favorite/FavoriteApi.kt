package com.example.ku_cse_team11_mobileapp.api.favorite

import okhttp3.ResponseBody
import retrofit2.http.*

interface FavoriteApi {

    // 즐겨찾기 등록
    @POST("/api/favorites")
    suspend fun addFavorite(
        @Query("memberId") memberId: Long,
        @Query("contentId") contentId: Long
    ): FavoriteDto

    // 특정 작품 즐겨찾기 여부
    @GET("/api/favorites")
    suspend fun isFavorite(
        @Query("memberId") memberId: Long,
        @Query("contentId") contentId: Long
    ): Boolean

    // 즐겨찾기 해제 (응답이 텍스트일 수 있어 ResponseBody로 받음)
    @DELETE("/api/favorites")
    suspend fun removeFavorite(
        @Query("memberId") memberId: Long,
        @Query("contentId") contentId: Long
    ): ResponseBody

    // (선택) 전체 즐겨찾기 목록: 백엔드가 지원하면 사용
    // GET /api/favorites?memberId=1  →  [ {id, memberId, contentId}, ... ]
    @GET("/api/favorites")
    suspend fun listFavorites(
        @Query("memberId") memberId: Long
    ): List<FavoriteDto>
}