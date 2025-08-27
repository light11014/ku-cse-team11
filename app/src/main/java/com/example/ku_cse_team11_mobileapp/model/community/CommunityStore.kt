package com.example.ku_cse_team11_mobileapp.model.community

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.communityDs by preferencesDataStore("community")

class CommunityStore(private val context: Context) {
    private fun keyFor(contentId: Int) = stringPreferencesKey("posts_cid_$contentId")
    private val gson = Gson()
    private val listType = object : TypeToken<List<CommunityPost>>() {}.type

    fun postsFlow(contentId: Int): Flow<List<CommunityPost>> =
        context.communityDs.data.map { prefs ->
            val raw = prefs[keyFor(contentId)]
            if (raw.isNullOrBlank()) emptyList()
            else runCatching { gson.fromJson<List<CommunityPost>>(raw, listType) }.getOrDefault(emptyList())
        }

    suspend fun addPost(contentId: Int, post: CommunityPost) {
        context.communityDs.edit { prefs ->
            val k = keyFor(contentId)
            val cur = prefs[k]?.let { runCatching { gson.fromJson<List<CommunityPost>>(it, listType) }.getOrNull() } ?: emptyList()
            val next = listOf(post) + cur   // 최신이 위로 오도록 prepend
            prefs[k] = gson.toJson(next)
        }
    }
}