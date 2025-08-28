package com.example.ku_cse_team11_mobileapp.api.model

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.ku_cse_team11_mobileapp.BuildConfig
import com.example.ku_cse_team11_mobileapp.api.auth.AuthApi
import com.example.ku_cse_team11_mobileapp.api.db.ContentApi
import com.example.ku_cse_team11_mobileapp.api.favorite.FavoriteApi
import com.example.ku_cse_team11_mobileapp.api.favorite.FavoritesStore
import com.example.ku_cse_team11_mobileapp.model.community.CommunityStore
import com.example.ku_cse_team11_mobileapp.model.data.SessionManager
import com.example.ku_cse_team11_mobileapp.model.repository.AuthRepository
import com.example.ku_cse_team11_mobileapp.model.repository.AuthRepositoryImpl
import com.example.ku_cse_team11_mobileapp.model.repository.CommunityRepository
import com.example.ku_cse_team11_mobileapp.model.repository.CommunityRepositoryLocal
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepositoryImpl
import com.example.ku_cse_team11_mobileapp.model.repository.FavoritesRepository
import com.example.ku_cse_team11_mobileapp.model.repository.FavoritesRepositoryImpl

object ServiceLocator {
    lateinit var app: Application
        private set

    fun init(app: Application) {
        this.app = app
    }

    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 필요시 BODY
    }

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(logger)
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // ex) "http://10.0.2.2:8080/"
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ContentApi by lazy { retrofit.create(ContentApi::class.java) }
    val repo: ContentRepository by lazy { ContentRepositoryImpl(api) }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val session: SessionManager by lazy { SessionManager(app) }

    // 기존 ContentRepository도 여기에 있음
    val authRepo: AuthRepository by lazy { AuthRepositoryImpl(authApi, session) }

    val favoriteApi: FavoriteApi by lazy { retrofit.create(FavoriteApi::class.java) }
    val favoritesStore: FavoritesStore by lazy { FavoritesStore(app) }
    val favoritesRepo: FavoritesRepository by lazy {
        FavoritesRepositoryImpl(favoriteApi, favoritesStore, session, repo /* ContentRepository */)
    }
    val communityStore: CommunityStore by lazy { CommunityStore(app) }
    val communityRepo: CommunityRepository by lazy {
        CommunityRepositoryLocal(
            communityStore,
            session
        )
    }

    suspend fun logoutAll() {
        // 쿠키/헤더 쓰면 여기서 OkHttp cookieJar clear 등도 해주기
        favoritesStore.clear()
        session.logout()
    }
}