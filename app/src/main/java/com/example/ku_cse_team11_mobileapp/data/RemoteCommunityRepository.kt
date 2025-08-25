package com.example.ku_cse_team11_mobileapp.data

import com.example.ku_cse_team11_mobileapp.domain.CommunityRepository
import com.example.ku_cse_team11_mobileapp.domain.Post

class RemoteCommunityRepository(
    private val api: CommunityApi
) : CommunityRepository {

    override suspend fun getPosts(contentId: Long): List<Post> =
        api.getPosts(contentId).map { it.toDomain() }

    override suspend fun addPost(contentId: Long, author: String, content: String): Post =
        api.addPost(contentId, PostRequest(author, content)).toDomain()
}