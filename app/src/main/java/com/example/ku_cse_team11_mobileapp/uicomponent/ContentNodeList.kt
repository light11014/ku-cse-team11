package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
    var platformIdx by rememberSaveable { mutableIntStateOf(0) }

    val type = ContentTypeTab.entries[typeIdx]
    val platform = PlatformTab.entries[platformIdx]

    // 최초 및 탭 변경 시 로드
    LaunchedEffect(typeIdx, platformIdx) {
        vm.load(type.apiParam, platform.name) // platform.name: "ALL", "KAKAO_WEBTOON", ...
    }

    val state by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        // 큰 탭(콘텐츠 타입)
        ScrollableTabRow(
            selectedTabIndex = typeIdx,
            edgePadding = 8.dp
        ) {
            ContentTypeTab.entries.forEachIndexed { idx, t ->
                Tab(
                    selected = typeIdx == idx,
                    onClick = { typeIdx = idx },
                    text = { Text(t.label) }
                )
            }
        }

        // 작은 탭(플랫폼)
        SecondaryScrollableTabRow(
            selectedTabIndex = platformIdx,
            edgePadding = 8.dp
        ) {
            PlatformTab.entries.forEachIndexed { idx, p ->
                Tab(
                    selected = platformIdx == idx,
                    onClick = { platformIdx = idx },
                    text = { Text(p.label) }
                )
            }
        }

        // 내용
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
                // 세로 1열
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.items) { item ->
                        // 여기서 ContentNode 재사용
                        ContentNode(
                            content = item,  // ContentSummary
                            onClick = { id -> navController.navigate("content/$id") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}