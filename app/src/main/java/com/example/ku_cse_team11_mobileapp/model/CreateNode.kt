package com.example.ku_cse_team11_mobileapp.model


import kotlinx.serialization.Serializable

@Serializable
data class CreateNode(
    val id: Long,
    val title: String,
    val author: String,
    val type: ContentType,
    val description: String? = null,
    val platform: Platform,
    val thumbnailUrl: String? = null,
    val contentUrl: String? = null,
    val publishDate: String? = null,   // LocalDate -> String (ISO 형식)으로 받는게 안전
    val episodeCount: Int? = null,
    val tags: String? = null,          // 백엔드에서 아직 문자열이므로 그대로 둠
    val genre: String? = null,
    val ageRating: String? = null,
    val updateFrequency: String? = null,
    val views: Long = 0,
    val rating: Double = 0.0,
    val likes: Long = 0,
    val createdAt: String? = null,     // LocalDateTime -> String
    val updatedAt: String? = null
)
