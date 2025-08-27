package com.example.ku_cse_team11_mobileapp.model.community

data class CommunityPost(
    val id: Long,            // System.currentTimeMillis() 등
    val contentId: Int,      // 작품 ID
    val author: String,      // 작성자 (세션 이름 or "익명")
    val title: String,
    val body: String,
    val createdAt: Long      // epoch millis
)