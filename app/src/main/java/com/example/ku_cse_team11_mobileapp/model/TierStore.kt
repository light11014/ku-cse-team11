package com.example.ku_cse_team11_mobileapp.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.tierStore by preferencesDataStore("tier_prefs")
private val KEY_TIER_SET = stringSetPreferencesKey("tiers")
// 저장 형식: "contentId:Tier" 예) "42:S"

object TierStore {

    /** 전체 티어를 Map<contentId, Tier> 로 스트림 제공 */
    fun tierMapFlow(context: Context): Flow<Map<Long, Tier>> =
        context.tierStore.data.map { prefs ->
            val set = prefs[KEY_TIER_SET].orEmpty()
            set.mapNotNull { entry ->
                val idx = entry.indexOf(':')
                if (idx <= 0) null
                else {
                    val id = entry.substring(0, idx).toLongOrNull() ?: return@mapNotNull null
                    val t = runCatching { Tier.valueOf(entry.substring(idx + 1)) }.getOrNull()
                        ?: return@mapNotNull null
                    id to t
                }
            }.toMap()
        }

    /** 특정 작품에 티어 저장/업데이트 */
    suspend fun setTier(context: Context, contentId: Long, tier: Tier) {
        context.tierStore.edit { prefs ->
            val set = prefs[KEY_TIER_SET].orEmpty().toMutableSet()
            // 기존 같은 id 항목 제거
            set.removeAll { it.startsWith("$contentId:") }
            set.add("$contentId:${tier.name}")
            prefs[KEY_TIER_SET] = set
        }
    }

    /** 특정 작품 티어 제거(원하면) */
    suspend fun clearTier(context: Context, contentId: Long) {
        context.tierStore.edit { prefs ->
            val set = prefs[KEY_TIER_SET].orEmpty().toMutableSet()
            set.removeAll { it.startsWith("$contentId:") }
            prefs[KEY_TIER_SET] = set
        }
    }
}