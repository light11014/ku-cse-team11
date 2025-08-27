package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.model.community.CommunityPost
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun postsFlow(contentId: Int): Flow<List<CommunityPost>>
    suspend fun addPost(contentId: Int, title: String, body: String, authorOverride: String? = null)
}