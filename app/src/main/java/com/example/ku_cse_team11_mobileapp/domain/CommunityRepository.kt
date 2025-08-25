package com.example.ku_cse_team11_mobileapp.domain

interface CommunityRepository {
    suspend fun getPosts(contentId: Long): List<Post>
    suspend fun addPost(contentId: Long, author: String, content: String): Post
}