package com.example.ku_cse_team11_mobileapp.api.favorite


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

private val Context.ds by preferencesDataStore("favorites")

class FavoritesStore(private val context: Context) {
    private val KEY_IDS = stringSetPreferencesKey("favorite_ids")

    val idsFlow: Flow<Set<Long>> =
        context.ds.data.map { (it[KEY_IDS] ?: emptySet()).map(String::toLong).toSet() }

    suspend fun setAll(ids: Set<Long>) {
        context.ds.edit { it[KEY_IDS] = ids.map(Long::toString).toSet() }
    }

    suspend fun add(id: Long) {
        context.ds.edit { pref ->
            val cur = (pref[KEY_IDS] ?: emptySet()).toMutableSet()
            cur += id.toString()
            pref[KEY_IDS] = cur
        }
    }

    suspend fun remove(id: Long) {
        context.ds.edit { pref ->
            val cur = (pref[KEY_IDS] ?: emptySet()).toMutableSet()
            cur -= id.toString()
            pref[KEY_IDS] = cur
        }
    }
}