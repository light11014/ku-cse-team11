// com/example/ku_cse_team11_mobileapp/uicomponent/ContentNodeList.kt
package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.ContentNode
import com.example.ku_cse_team11_mobileapp.model.ui.ContentTypeTab
import com.example.ku_cse_team11_mobileapp.model.ui.PlatformTab
import com.example.ku_cse_team11_mobileapp.model.viewmodel.ContentNodeListViewModel
import com.example.ku_cse_team11_mobileapp.model.viewmodel.SortTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentNodeList(
    navController: NavHostController,
    vm: ContentNodeListViewModel = viewModel(
        factory = ContentNodeListViewModel.Factory(ServiceLocator.repo)
    ),
    modifier: Modifier = Modifier
) {
    var typeIdx by rememberSaveable { mutableIntStateOf(0) }
    val type = ContentTypeTab.entries[typeIdx] // "WEBTOON"/"WEBNOVEL"

    // 정렬 탭(랭킹/티어)
    var sortIdx by rememberSaveable { mutableIntStateOf(0) }
    val sort = SortTab.entries[sortIdx]

    // 콘텐츠 유형에 맞는 플랫폼 목록
    val platformTabs = remember(type.apiParam) { PlatformTab.tabsFor(type.apiParam) }
    var platformIdx by rememberSaveable(type.apiParam) { mutableIntStateOf(0) }
    val platform = platformTabs.getOrNull(platformIdx) ?: PlatformTab.ALL

    // 로드 트리거 (한 번만)
    LaunchedEffect(typeIdx, platformIdx, sortIdx) {
        vm.load(type.apiParam, platform.name, sort) // 항상 non-null 전달
    }
    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        // 탭 섹션 (더보기/접기)

        // 상단: 콘텐츠 타입(큰 탭)
// ⬇ 상단: 콘텐츠 타입(큰 탭) — 균등폭
        TabRow(
            selectedTabIndex = typeIdx,
            modifier = Modifier.fillMaxWidth()
        ) {
            ContentTypeTab.entries.forEachIndexed { idx, t ->
                Tab(
                    selected = typeIdx == idx,
                    onClick = {
                        typeIdx = idx
                        platformIdx = 0
                        sortIdx = 0
                    },
                    text = { Text(t.label) }
                )
            }
        }

// ⬇ 하위탭 1: 정렬(랭킹/티어) — 균등폭
        SecondaryTabRow(
            selectedTabIndex = sortIdx,
            modifier = Modifier.fillMaxWidth()
        ) {
            SortTab.entries.forEachIndexed { idx, s ->
                Tab(
                    selected = sortIdx == idx,
                    onClick = { sortIdx = idx },
                    text = { Text(s.label) }
                )
            }
        }

// ⬇ 하위탭 2: 플랫폼 — 스크롤 가능(폭은 가득)
        SecondaryScrollableTabRow(
            selectedTabIndex = platformIdx,
            edgePadding = 0.dp,                // 양 끝 여백 제거해 꽉 차게
            modifier = Modifier.fillMaxWidth()
        ) {
            platformTabs.forEachIndexed { idx, p ->
                Tab(
                    selected = platformIdx == idx,
                    onClick = { platformIdx = idx },
                    text = { Text(p.label) }
                )
            }
        }


        // 본문
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("오류: ${state.error}")
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    itemsIndexed(state.items) { idx, item ->
                        ContentNode(
                            content = item,
                            onClick = { id -> navController.navigate("content/$id") },
                            rank = idx + 1,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
