package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    val type = ContentTypeTab.entries[typeIdx] // "WEBTOON" / "WEBNOVEL"

    // ▶ 콘텐츠 유형에 맞는 플랫폼 목록
    val platformTabs = remember(type.apiParam) { PlatformTab.tabsFor(type.apiParam) }

    // ▶ 유형 바뀌면 플랫폼 인덱스 초기화
    var platformIdx by rememberSaveable(type.apiParam) { mutableIntStateOf(0) }

    // ▶ 안전하게 인덱스 보정 (혹시 저장된 값이 범위를 벗어났을 때)
    if (platformIdx > platformTabs.lastIndex) platformIdx = 0

    val platform = platformTabs[platformIdx]

    // 최초 및 탭 변경 시 로드
    LaunchedEffect(type.apiParam, platformIdx) {
        vm.load(type.apiParam, platform.apiParam) // ✅ apiParam을 넘겨요
    }

    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        // 큰 탭: 콘텐츠 타입
        ScrollableTabRow(selectedTabIndex = typeIdx, edgePadding = 8.dp) {
            ContentTypeTab.entries.forEachIndexed { idx, t ->
                Tab(
                    selected = typeIdx == idx,
                    onClick = { typeIdx = idx }, // platformIdx는 key로 0으로 초기화됨
                    text = { Text(t.label) }
                )
            }
        }

        // 작은 탭: 플랫폼  ✅ 여기를 platformTabs로!
        SecondaryScrollableTabRow(selectedTabIndex = platformIdx, edgePadding = 8.dp) {
            platformTabs.forEachIndexed { idx, p ->
                Tab(
                    selected = platformIdx == idx,
                    onClick = { platformIdx = idx },
                    text = { Text(p.label) }
                )
            }
        }

        // 내용
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("오류: ${state.error}")
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(140.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(state.items) {idx, item ->
                        ContentNode(
                            content = item,
                            rank = idx + 1,
                            onClick = { id -> navController.navigate("content/$id") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
