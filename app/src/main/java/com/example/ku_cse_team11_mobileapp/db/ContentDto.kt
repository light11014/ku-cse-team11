package com.example.ku_cse_team11_mobileapp.db

import com.example.ku_cse_team11_mobileapp.model.ContentType
import com.example.ku_cse_team11_mobileapp.model.Platform
import kotlinx.serialization.Serializable

@Serializable
data class ContentDto(
    val id: Long,
    val title: String,
    val author: String,
    val type: ContentType,
    val description: String? = null,
    val platform: Platform,
    val thumbnailUrl: String? = null,
    val episodeCount: Int? = null,
    val views: Long = 0,
    val likes: Long = 0,
    val tags: List<String> = emptyList()
)