package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import com.example.ku_cse_team11_mobileapp.model.Tier
import com.example.ku_cse_team11_mobileapp.model.TierStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TierListScreen(
    node: CreateNode,
    selected: Tier?,
    onBack: () -> Unit,
    onConfirm: (Tier) -> Unit // 저장 후 돌아갈 때 외부에 알려주고 싶으면 유지
) {
    var tier by remember { mutableStateOf(selected ?: Tier.A) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("티어리스트") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbar) },
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onBack
                ) { Text("취소") }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // ★ TierStore에 영구 저장
                        scope.launch {
                            TierStore.setTier(ctx, node.id, tier)
                            snackbar.showSnackbar("‘${node.title}’ ${tier.name} 티어로 저장했어요")
                            onConfirm(tier) // 외부에도 알림(필요 없으면 제거 가능)
                        }
                    }
                ) { Text("확인") }
            }
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 작품 요약 ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AsyncImage(
                    model = node.thumbnailUrl,
                    contentDescription = "${node.title} 표지",
                    modifier = Modifier.size(96.dp)
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        node.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("저자: ${node.author}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // --- S~F 2열 그리드 ---
            val items = listOf(Tier.S, Tier.A, Tier.B, Tier.C, Tier.D, Tier.E, Tier.F)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false
            ) {
                items(items, key = { it.name }) { item ->
                    FilterChip(
                        selected = tier == item,
                        onClick = { tier = item },
                        label = { Text(item.name) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            AssistChip(
                onClick = {},
                label = { Text("현재 선택: ${tier.name}") }
            )
        }
    }
}