package com.example.ku_cse_team11_mobileapp.api.auth

import com.example.ku_cse_team11_mobileapp.model.auth.LoginRequest
import com.example.ku_cse_team11_mobileapp.model.auth.LoginResponse
import com.example.ku_cse_team11_mobileapp.model.auth.SignUpRequest
import com.example.ku_cse_team11_mobileapp.model.auth.SignUpResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/auth/signup")
    suspend fun signup(@Body req: SignUpRequest): SignUpResponse

    @POST("/api/auth/login")
    suspend fun login(@Body req: LoginRequest): LoginResponse
}