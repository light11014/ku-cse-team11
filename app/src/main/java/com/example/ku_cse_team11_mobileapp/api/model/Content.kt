package com.example.ku_cse_team11_mobileapp.api.model

// 랭킹 API 응답
data class RankResponse(
    val rank: Int,
    val content: ContentSummary
)

// 콘텐츠 요약 정보 (랭킹용)
data class ContentSummary(
    val id: Int,
    val title: String,
    val authors: String,
    val thumbnailUrl: String?,
    val platform: String,
    val views: Long
)

// 콘텐츠 상세 응답
data class ContentDetail(
    val id: Int,
    val title: String,
    val authors: String,
    val contentType: String,
    val description: String,
    val platform: String,
    val thumbnailUrl: String?,
    val contentUrl: String?,
    val totalEpisodes: Int,
    val tags: String,
    val category: String,
    val ageRating: String,
    val pubPeriod: String,
    val views: Long,
    val rating: Double,
    val likes: Int,
    val createdAt: String,
    val updatedAt: String
)
