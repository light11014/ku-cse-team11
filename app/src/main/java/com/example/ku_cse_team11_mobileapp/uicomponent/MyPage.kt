package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import com.example.ku_cse_team11_mobileapp.model.Tier
import com.example.ku_cse_team11_mobileapp.model.TierStore
import com.example.ku_cse_team11_mobileapp.model.UserStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListScreen(
    nodes: List<CreateNode>,
    favoriteIds: Set<Long>,
    onBack: () -> Unit,
    onToggleFavorite: (Long) -> Unit
) {
    val ctx = LocalContext.current
    val name by remember { UserStore.nameFlow(ctx) }.collectAsState(initial = "")
    val tierMap by remember { TierStore.tierMapFlow(ctx) }.collectAsState(initial = emptyMap())

    // 데이터 준비
    val favoriteNodes = remember(favoriteIds, nodes) { nodes.filter { it.id in favoriteIds } }
    val tierGroups: Map<Tier, List<CreateNode>> = remember(tierMap, nodes) {
        tierMap.entries.groupBy({ it.value }, { it.key })
            .mapValues { (_, ids) -> nodes.filter { n -> ids.contains(n.id) } }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                title = { Text("마이리스트") }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // --- 프로필 ---
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = name.ifBlank { "게스트" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text("즐겨찾기 ${favoriteNodes.size}개 · 티어 지정 ${tierMap.size}개")
                    }
                }
            }

            // --- 즐겨찾기 섹션 ---
            item { SectionHeader("즐겨찾기") }
            if (favoriteNodes.isEmpty()) {
                item { EmptyText("즐겨찾기한 작품이 없습니다.") }
            } else {
                items(favoriteNodes, key = { it.id }) { node ->
                    NodeRowItem(
                        node = node,
                        right = {
                            IconButton(onClick = { onToggleFavorite(node.id) }) {
                                Icon(Icons.Outlined.Menu, contentDescription = "즐겨찾기")
                            }
                        }
                    )
                }
            }

            // --- 티어 섹션: S→F 순으로 그룹 표시 ---
            item { SectionHeader("티어") }
            val order = listOf(Tier.S, Tier.A, Tier.B, Tier.C, Tier.D, Tier.E, Tier.F)
            order.forEach { t ->
                val list = tierGroups[t].orEmpty()
                if (list.isNotEmpty()) {
                    item { TierHeader(t, list.size) }
                    items(list, key = { it.id }) { node ->
                        NodeRowItem(node = node)
                    }
                }
            }
            if (tierMap.isEmpty()) {
                item { EmptyText("아직 티어를 매긴 작품이 없습니다.") }
            }
        }
    }
}

@Composable private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable private fun TierHeader(tier: Tier, count: Int) {
    Text(
        text = "${tier.name} Tier · ${count}개",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable private fun EmptyText(msg: String) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(msg, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable private fun NodeRowItem(
    node: CreateNode,
    right: (@Composable () -> Unit)? = null
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = node.thumbnailUrl,
                contentDescription = "${node.title} 표지",
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(node.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text("저자: ${node.author}", style = MaterialTheme.typography.bodySmall)
            }
            if (right != null) right()
        }
    }
}
