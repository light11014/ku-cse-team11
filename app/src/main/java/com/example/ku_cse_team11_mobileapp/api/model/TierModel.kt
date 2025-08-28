package com.example.ku_cse_team11_mobileapp.api.model

import kotlinx.serialization.Serializable

@Serializable
data class TierRequest(
    val contentId: Int,
    val memberId: Long,
    val tier: String   // 예: "S", "A", "B", "C", "D", "F", "UNKNOWN"
)

@Serializable
data class TierResponse(
    val id: Int,
    val memberId: Int,
    val contentId: Int,
    val tier: String,
    val score: Int
)