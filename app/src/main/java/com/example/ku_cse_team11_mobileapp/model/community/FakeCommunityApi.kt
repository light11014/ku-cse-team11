package com.example.ku_cse_team11_mobileapp.model.community

import com.example.ku_cse_team11_mobileapp.data.CommunityApi
import com.example.ku_cse_team11_mobileapp.data.PostDto
import com.example.ku_cse_team11_mobileapp.data.PostRequest
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class FakeCommunityApi(
    private val networkDelayMs: Long = 300L
) : CommunityApi {

    private val idGen = AtomicLong(1000L)
    private val store = ConcurrentHashMap<Long, MutableList<PostDto>>()

    override suspend fun getPosts(contentId: Long): List<PostDto> {
        delay(networkDelayMs)
        return store[contentId]?.toList() ?: emptyList()
    }

    override suspend fun addPost(contentId: Long, body: PostRequest): PostDto {
        delay(networkDelayMs)
        val now = System.currentTimeMillis()
        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(now)
        val post = PostDto(
            id = idGen.incrementAndGet(),
            author = body.author.ifBlank { "익명" },
            content = body.content,
            createdAt = createdAt
        )
        store.computeIfAbsent(contentId) { mutableListOf() }.add(0, post)
        return post
    }
}
