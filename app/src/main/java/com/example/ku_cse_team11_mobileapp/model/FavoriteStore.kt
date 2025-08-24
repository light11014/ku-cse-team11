package com.example.ku_cse_team11_mobileapp.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "favorites_prefs"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

object FavoriteStore {
    private val KEY_FAVORITE_SET = stringSetPreferencesKey("favorite_ids")

    /** Flow로 즐겨찾기 ID 집합(Long) 제공 */
    fun favoritesFlow(context: Context): Flow<Set<Long>> =
        context.dataStore.data.map { prefs ->
            (prefs[KEY_FAVORITE_SET] ?: emptySet())
                .mapNotNull { it.toLongOrNull() }
                .toSet()
        }

    /** 토글 저장 (있으면 제거, 없으면 추가) */
    suspend fun toggle(context: Context, id: Long) {
        context.dataStore.edit { prefs ->
            val cur = prefs[KEY_FAVORITE_SET] ?: emptySet()
            prefs[KEY_FAVORITE_SET] = if (id.toString() in cur) {
                cur - id.toString()
            } else {
                cur + id.toString()
            }
        }
    }

    /** 전체 교체 저장이 필요할 때 */
    suspend fun setAll(context: Context, ids: Set<Long>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FAVORITE_SET] = ids.map { it.toString() }.toSet()
        }
    }
}