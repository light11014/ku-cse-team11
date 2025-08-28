package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.ContentNode
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(navController: NavHostController) {
    val session = ServiceLocator.session
    val name by session.nameFlow.collectAsState(initial = "")
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<ContentSummary>>(emptyList()) }
    val repo = ServiceLocator.favoritesRepo
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching {
            repo.refreshFromServerIfPossible()
            repo.listFavoriteSummaries(lang = "kr")
        }.onSuccess { items = it }
            .onFailure { error = it.message }
        loading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("마이페이지") },
                actions = {
                    TextButton(onClick = { showDialog = true }) {
                        Text("로그아웃")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${name?.ifBlank { "회원" }} 님!",
                style = MaterialTheme.typography.titleLarge
            )

            // 즐겨찾기 목록 ... (기존)ㅁ
            Text(
                text = "즐겨찾기 목록",
                style = MaterialTheme.typography.titleMedium
            )
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("로그아웃") },
                    text = { Text("정말 로그아웃 하시겠어요?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            scope.launch {
                                ServiceLocator.logoutAll()
                                // 네비게이션: 로그인 화면/그래프 시작점으로 이동
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }) { Text("로그아웃") }
                    },
                    dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } }
                )
            }
            if (loading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            } else if (error != null) {
                Text("오류: $error", color = MaterialTheme.colorScheme.error)
            } else if (items.isEmpty()) {
                Text("즐겨찾기한 작품이 없습니다.")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items) { c ->
                        ContentNode(
                            content = c,
                            onClick = { navController.navigate("content/${c.id}") })
                    }
                }
            }
        }
    }
}
