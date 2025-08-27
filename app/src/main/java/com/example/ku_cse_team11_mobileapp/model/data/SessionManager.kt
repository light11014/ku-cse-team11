package com.example.ku_cse_team11_mobileapp.model.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

private val Context.dataStore by preferencesDataStore("session")

class SessionManager(private val context: Context) {
    private val KEY_MEMBER_ID = longPreferencesKey("memberId")
    private val KEY_LOGIN_ID  = stringPreferencesKey("loginId")
    private val KEY_NAME      = stringPreferencesKey("name")

    val memberIdFlow: Flow<Long?> = context.dataStore.data.map { it[KEY_MEMBER_ID] }
    val loginIdFlow:  Flow<String?> = context.dataStore.data.map { it[KEY_LOGIN_ID] }
    val nameFlow:     Flow<String?> = context.dataStore.data.map { it[KEY_NAME] }

    suspend fun setSession(memberId: Long, loginId: String, name: String) {
        context.dataStore.edit {
            it[KEY_MEMBER_ID] = memberId
            it[KEY_LOGIN_ID]  = loginId
            it[KEY_NAME]      = name
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}