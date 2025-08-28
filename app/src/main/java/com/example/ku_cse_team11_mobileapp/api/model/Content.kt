package com.example.ku_cse_team11_mobileapp.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val views: Long,
    val language: String? = null,
    val tier: String? = null

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
    val updatedAt: String,
    val language: String? = null,
    val myTier: String? = null,            // ✅ 추가
    val avgTier: String? = null,           // ✅ 추가
    val stats: TierStats? = null
)

@Serializable
data class TierStats(
    val rating: Map<String, Int> = emptyMap(),   // e.g. {"A":1,"B":1,"S":0,"C":0,"D":0}
    @SerialName("rating_count") val ratingCount: Int = 0
)