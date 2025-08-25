package com.example.ku_cse_team11_mobileapp.model

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ku_cse_team11_mobileapp.graph.NavHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun AppEntry(initialNodes: List<CreateNode>) {
    val ctx = LocalContext.current

    // ❶ 즉시 값(기본은 빈문자열)으로 시작 → 스플래시 최소화
    var decided by rememberSaveable { mutableStateOf(false) }
    var goMain by rememberSaveable { mutableStateOf(false) }

    // ❷ DataStore Flow 구독
    val token by remember(ctx) { TokenStore.accessFlow(ctx) }
        .collectAsState(initial = "") // 처음엔 바로 "" 들어옴

    // ❸ 500ms 내에 토큰 오면 메인, 아니면 로그인으로 '결정'
    LaunchedEffect(token) {
        if (!decided) {
            withTimeoutOrNull(500) { /* 여유 주기 */ delay(1) }
            goMain = token.isNotBlank()
            decided = true
        }
    }

    if (!decided) {
        // 아주 짧은 스플래시만 노출
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (goMain) {
        NavHost(initialNodes)
    } else {
        LoginScreen(onLoggedIn = { goMain = true })
    }
}