package com.example.ku_cse_team11_mobileapp.model.community

data class ContentComment(
    val id: Long,
    val contentId: Int,
    val memberId: Long,
    val body: String,
    val createdAt: String,
    val memberName: String
)