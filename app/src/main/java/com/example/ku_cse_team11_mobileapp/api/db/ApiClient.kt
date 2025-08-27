package com.example.ku_cse_team11_mobileapp.api.db

import android.content.Context
import com.example.ku_cse_team11_mobileapp.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object ApiClient {
    private val json = Json {
        ignoreUnknownKeys = true   // category, ageRating, pubPeriod 등 추가 필드 무시
        explicitNulls = false
        isLenient = true
    }

    private fun okHttp(): OkHttpClient = OkHttpClient.Builder().build()

    fun retrofit(context: Context): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttp())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    fun content(context: Context): ContentApi =
        retrofit(context).create(ContentApi::class.java)
}