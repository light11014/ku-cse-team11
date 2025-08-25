package com.example.ku_cse_team11_mobileapp.model.community


import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.ku_cse_team11_mobileapp.BuildConfig
import com.example.ku_cse_team11_mobileapp.data.AuthApi
import com.example.ku_cse_team11_mobileapp.data.CommunityApi
import com.example.ku_cse_team11_mobileapp.model.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor

object ApiClient {
    private val logging by lazy {
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
    }
    private fun okHttp(context: Context) = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addInterceptor(AuthInterceptor(context))
        .build()

    fun retrofit(context: Context): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttp(context))
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun community(context: Context) = retrofit(context).create(CommunityApi::class.java)
    fun auth(context: Context) = retrofit(context).create(AuthApi::class.java)
}

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val access = runBlocking { TokenStore.loadAccess(context) }
        val req = if (!access.isNullOrBlank())
            chain.request().newBuilder().addHeader("Authorization", "Bearer $access").build()
        else chain.request()
        return chain.proceed(req)
    }
}