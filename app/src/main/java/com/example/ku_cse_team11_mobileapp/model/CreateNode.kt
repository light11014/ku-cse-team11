// model/CreateNode.kt
package com.example.ku_cse_team11_mobileapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CreateNode(
    val id: Long,
    val title: String,
    val authors: String,
    val contentType: String,
    val description: String? = null,
    val platform: String,
    val thumbnailUrl: String? = null,
    val contentUrl: String? = null,
    val totalEpisodes: Int? = null,
    val tags: String? = null,
    val category: String? = null,
    val ageRating: String? = null,
    val pubPeriod: String? = null,
    val views: Long = 0,
    val rating: Double = 0.0,
    val likes: Long = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)


// API enum 문자열과 1:1 매핑
@Serializable
enum class ContentType {
    @SerialName("WEBNOVEL") WEBNOVEL,
    @SerialName("WEBTOON")  WEBTOON
}

@Serializable
enum class Platform {
    @SerialName("NAVER_WEBTOON") NAVER_WEBTOON,
    @SerialName("NOVELPIA")      NOVELPIA,
    @SerialName("KAKAO_PAGE")    KAKAO_PAGE,
    @SerialName("MOONPIA")       MOONPIA,
    @SerialName("KAKAO_WEBTOON") KAKAO_WEBTOON,
    @SerialName("NAVER_SERIES")  NAVER_SERIES
}
