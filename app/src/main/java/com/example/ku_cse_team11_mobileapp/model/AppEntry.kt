package com.example.ku_cse_team11_mobileapp.model

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ku_cse_team11_mobileapp.graph.NavHost
import com.example.ku_cse_team11_mobileapp.uicomponent.LoginScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.SignUpScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun AppEntry(initialNodes: List<CreateNode>) {
    val ctx = LocalContext.current

    // --- DataStore Flow: 즉시 ""로 시작해 스플래시 최소화 ---
    val token by remember(ctx) {
        TokenStore.accessFlow(ctx) // fun accessFlow(ctx): Flow<String>
    }.collectAsState(initial = "")

    // 인증 라우팅 (login/signup) 상태
    var authRoute by rememberSaveable { mutableStateOf("login") }

    // 결정 상태 (스플래시 최소화)
    var decided by rememberSaveable { mutableStateOf(false) }
    var goMain by rememberSaveable { mutableStateOf(false) }

    // 500ms 내 토큰 판단
    LaunchedEffect(token) {
        if (!decided) {
            withTimeoutOrNull(500) { delay(1) }
            goMain = token.isNotBlank()
            decided = true
        }
    }

    // 200ms 넘길 때만 로딩 노출 (체감 개선)
    val showSpinner by produceState(initialValue = false) {
        delay(200); value = true
    }

    when {
        !decided -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (showSpinner) CircularProgressIndicator()
            }
        }
        goMain -> {
            // 메인 네비게이션
            NavHost(initialNodes)
        }
        else -> {
            // 인증 플로우 (회원가입/로그인)
            when (authRoute) {
                "login" -> LoginScreen(
                    onLoggedIn = { goMain = true },
                    onNavigateSignUp = { authRoute = "signup" }
                )
                "signup" -> SignUpScreen(
                    onBack = { authRoute = "login" },
                    onRegistered = { goMain = true } // 가입 후 자동 로그인 처리 가정
                )
            }
        }
    }
}

