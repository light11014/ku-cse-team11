package com.example.ku_cse_team11_mobileapp.model.repository

import android.os.Build
import androidx.annotation.RequiresExtension
import com.example.ku_cse_team11_mobileapp.api.auth.AuthApi
import com.example.ku_cse_team11_mobileapp.model.auth.ErrorResponse
import com.example.ku_cse_team11_mobileapp.model.auth.LoginRequest
import com.example.ku_cse_team11_mobileapp.model.auth.LoginResponse
import com.example.ku_cse_team11_mobileapp.model.auth.SignUpRequest
import com.example.ku_cse_team11_mobileapp.model.auth.SignUpResponse
import com.example.ku_cse_team11_mobileapp.model.data.SessionManager
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import com.google.gson.Gson

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val session: SessionManager
) : AuthRepository {

    override val memberIdFlow: Flow<Long?> = session.memberIdFlow

    override suspend fun signup(loginId: String, name: String, password: String): SignUpResponse {
        return api.signup(SignUpRequest(loginId, name, password))
    }

    override suspend fun login(loginId: String, password: String): LoginResponse {
        return api.login(LoginRequest(loginId, password)).also { res ->
            session.setSession(res.memberId, res.loginId, res.name) // ✅ 세션 저장
        }
    }

    override suspend fun logout() { session.clear() }
}

/** 400 에러 본문을 { "error": "..."} 로 파싱해 사람이 읽을 수 있는 메시지 추출 */
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
fun extractErrorMessage(e: Throwable): String {
    if (e is HttpException) {
        val body = e.response()?.errorBody()?.string()
        if (!body.isNullOrBlank()) {
            runCatching { return Gson().fromJson(body, ErrorResponse::class.java).error }.getOrNull()
        }
        return "요청이 실패했습니다 (${e.code()})"
    }
    return e.message ?: "네트워크 오류"
}