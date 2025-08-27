package com.example.ku_cse_team11_mobileapp.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userStore by preferencesDataStore("user_prefs")
private val KEY_NAME = stringPreferencesKey("name")

object UserStore {
    fun nameFlow(context: Context) = context.userStore.data.map { it[KEY_NAME].orEmpty() }
    suspend fun saveName(context: Context, name: String) {
        context.userStore.edit { it[KEY_NAME] = name }
    }
    suspend fun loadName(context: Context): String =
        context.userStore.data.map { it[KEY_NAME].orEmpty() }.first()
}