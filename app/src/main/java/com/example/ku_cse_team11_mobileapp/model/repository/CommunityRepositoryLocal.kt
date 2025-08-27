package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.model.community.CommunityPost
import com.example.ku_cse_team11_mobileapp.model.community.CommunityStore
import com.example.ku_cse_team11_mobileapp.model.data.SessionManager
import kotlinx.coroutines.flow.first


class CommunityRepositoryLocal(
    private val store: CommunityStore,
    private val session: SessionManager
) : CommunityRepository {

    override fun postsFlow(contentId: Int) = store.postsFlow(contentId)

    override suspend fun addPost(contentId: Int, title: String, body: String, authorOverride: String?) {
        val sessionName = session.nameFlow.first()         // String?

        val name = nonBlankOrNull(authorOverride)          // 먼저 파라미터 우선
            ?: nonBlankOrNull(sessionName)                 // 세션 이름
            ?: "익명"

        val now = System.currentTimeMillis()
        val post = CommunityPost(
            id = now,
            contentId = contentId,
            author = name,
            title = title.trim(),
            body = body.trim(),
            createdAt = now
        )
        store.addPost(contentId, post)
        // 나중에 서버 붙이면 여기서 POST 호출 후 성공 시 store 반영하도록 변경하면 됨.
    }
}

private fun nonBlankOrNull(s: String?): String? =
    s?.trim()?.takeIf { it.isNotEmpty() }