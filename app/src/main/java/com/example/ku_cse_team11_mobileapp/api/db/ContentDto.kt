package com.example.ku_cse_team11_mobileapp.api.db


import com.google.gson.annotations.SerializedName

data class ContentDto(
    val id: Long,
    val title: String,
    val author: String,
    val contenttype: String,            // "NOVEL", "WEBTOON" ...
    val description: String?,
    val platform: String,        // "NAVERWEBTOON", ...
    @SerializedName("thumbnailUrl") val img: String?,
    val contentUrl: String?,
    val publishDate: String?,    // "2024-08-01"
    val episodeCount: Int?,
    val contentStatus: String?,  // "ONGOING", "COMPLETED" ...
    val tags: String?,           // "판타지,학원" (CSV)
    val genre: String?,
    val ageRating: String?,
    val updateFrequency: String?,
    val views: Long?,
    val rating: Double?,
    val likes: Long?
)