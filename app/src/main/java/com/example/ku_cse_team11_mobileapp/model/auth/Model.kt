package com.example.ku_cse_team11_mobileapp.model.auth

data class SignUpRequest(val loginId: String, val name: String, val password: String)
data class SignUpResponse(val memberId: Long, val message: String)

data class LoginRequest(val loginId: String, val password: String)
data class LoginResponse(val memberId: Long, val loginId: String, val name: String, val message: String)

data class ErrorResponse(val error: String)