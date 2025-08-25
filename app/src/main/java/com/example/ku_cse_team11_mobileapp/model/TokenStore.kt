package com.example.ku_cse_team11_mobileapp.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore("auth_prefs")
private val KEY_ACCESS = stringPreferencesKey("access_token")
private val KEY_REFRESH = stringPreferencesKey("refresh_token")

object TokenStore {
    suspend fun save(context: Context, access: String?, refresh: String?) {
        context.dataStore.edit {
            if (access != null) it[KEY_ACCESS] = access else it.remove(KEY_ACCESS)
            if (refresh != null) it[KEY_REFRESH] = refresh else it.remove(KEY_REFRESH)
        }
    }
    suspend fun loadAccess(context: Context) =
        context.dataStore.data.map { it[KEY_ACCESS] }.first()
    suspend fun loadRefresh(context: Context) =
        context.dataStore.data.map { it[KEY_REFRESH] }.first()
    suspend fun clear(context: Context) { save(context, null, null) }
    fun accessFlow(context: Context) =
        context.dataStore.data.map { it[KEY_ACCESS].orEmpty() }  // 없으면 ""
}
