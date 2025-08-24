package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import com.example.ku_cse_team11_mobileapp.model.Platform
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NodeList(
    nodes: List<CreateNode>,
    favoriteIds: Set<Long>,                 // ★ 추가
    onToggleFavorite: (Long) -> Unit,
    onNodeClick: (CreateNode) -> Unit,
    onSearchClick: () -> Unit,
    onLoadMore: (() -> Unit)? = null
) {
    // 탭: [전체], [즐겨찾기], + 플랫폼들
    val platforms = remember { Platform.entries }
    val tabItems = remember { listOf<Any>("ALL", "FAVORITE") + platforms }

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // 현재 탭에 맞춰 필터링
    val filtered by remember(nodes, selectedTabIndex, favoriteIds) {
        derivedStateOf {
            when (val tab = tabItems[selectedTabIndex]) {
                "ALL" -> nodes
                "FAVORITE" -> nodes.filter { it.id in favoriteIds }
                is Platform -> nodes.filter { it.platform == tab }
                else -> nodes
            }
        }
    }

    // 한 화면에 탭 4개 고정 폭
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val tabWidth = remember(screenWidthDp) { (screenWidthDp / 4f).dp }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("작품") },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Outlined.Search, contentDescription = "검색")
                }
            }
        )
        // 가로 스크롤 가능한 탭바, 각 탭 폭 = 화면/4 → 항상 4개만 보임
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 0.dp,
            divider = {},
        ) {
            tabItems.forEachIndexed { index, tab ->
                val label = when (tab) {
                    "ALL" -> "전체"
                    "FAVORITE" -> "즐겨찾기"
                    is Platform -> tab.toKoreanLabel()
                    else -> ""
                }
                Tab(
                    modifier = Modifier.width(tabWidth),
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(label, maxLines = 1) }
                )
            }
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("표시할 작품이 없습니다.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(filtered, key = { it.id }) { node ->
                    Node(
                        node = node,
                        modifier = Modifier.fillMaxWidth(),
                        isFavorite = node.id in favoriteIds,
                        onToggleFavorite = onToggleFavorite,
                        onClick = onNodeClick
                    )
                }

                if (onLoadMore != null) {
                    item {
                        LaunchedEffect(filtered.size) { onLoadMore() }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

private fun Platform.toKoreanLabel(): String = when (this) {
    Platform.NAVERWEBTOON -> "네이버웹툰"
    Platform.NOVELPIA     -> "노벨피아"
    Platform.KAKAOPAGE    -> "카카오페이지"
    Platform.MOONPIA      -> "문피아"
    Platform.KAKAOWEBTOON -> "카카오웹툰"
    Platform.NAVERSERIES  -> "네이버시리즈"
}
