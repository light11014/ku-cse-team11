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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(navController: NavHostController) {
    val session = ServiceLocator.session
    val name by session.nameFlow.collectAsState(initial = "")
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<ContentSummary>>(emptyList()) }
    val repo = ServiceLocator.favoritesRepo

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
        topBar = { TopAppBar(title = { Text("마이페이지") }) }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "안녕하세요, ${name?.ifBlank { "회원" }} 님!", style = MaterialTheme.typography.titleLarge)

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
                        FavoriteCard(
                            item = c,
                            onClick = { navController.navigate("content/${c.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteCard(item: ContentSummary, onClick: () -> Unit) {
    Card(onClick = onClick) {
        Column(Modifier.fillMaxWidth()) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )
            Column(Modifier.padding(8.dp)) {
                Text(item.title, maxLines = 1)
                Text(item.authors, maxLines = 1, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}