package com.example.ku_cse_team11_mobileapp.model.repository

import com.example.ku_cse_team11_mobileapp.model.auth.LoginResponse
import com.example.ku_cse_team11_mobileapp.model.auth.SignUpResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val memberIdFlow: Flow<Long?>
    suspend fun signup(loginId: String, name: String, password: String): SignUpResponse
    suspend fun login(loginId: String, password: String): LoginResponse
    suspend fun logout()
}