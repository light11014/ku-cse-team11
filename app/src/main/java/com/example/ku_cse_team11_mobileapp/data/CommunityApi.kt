package com.example.ku_cse_team11_mobileapp.data

import com.example.ku_cse_team11_mobileapp.domain.Post
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface CommunityApi {
    @GET("/api/community/{contentId}/posts")
    suspend fun getPosts(@Path("contentId") contentId: Long): List<PostDto>

    @POST("/api/community/{contentId}/posts")
    suspend fun addPost(@Path("contentId") contentId: Long, @Body post: PostRequest): PostDto
}

data class PostDto(
    val id: Long,
    val author: String,
    val content: String,
    val createdAt: String
)

data class PostRequest(
    val author: String,
    val content: String
)

fun PostDto.toDomain() = Post(
    id = id,
    author = author,
    content = content,
    createdAt = createdAt
)
