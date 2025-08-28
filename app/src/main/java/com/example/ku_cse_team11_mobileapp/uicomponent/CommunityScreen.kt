package com.example.ku_cse_team11_mobileapp.uicomponent

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.viewmodel.CommunityViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.text.input.ImeAction

// uicomponent/CommunityScreen.kt (핵심 부분만 발췌)
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    contentId: Int,
    navController: NavHostController
) {
    val vm: CommunityViewModel = viewModel(
        factory = CommunityViewModel.Factory(
            ServiceLocator.repo,
            ServiceLocator.session,
            contentId
        )
    )
    val s by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(s.error) { s.error?.let { snackbar.showSnackbar(it) } }

    val listState = rememberLazyListState()
    // 무한 스크롤 트리거는 그대로…
    LaunchedEffect(listState, s.visibleItems) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { it ?: -1 }
            .distinctUntilChanged()
            .filter {
                it >= s.visibleItems.lastIndex - 3 &&
                        s.canLoadMore && !s.isLoading && !s.isRefreshing
            }
            .collect { vm.loadMore() }
    }

    val refreshState = rememberSwipeRefreshState(isRefreshing = s.isRefreshing)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("커뮤니티") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },

        // ✅ 하단 입력 바
        bottomBar = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding() // 키보드 뜰 때 위로
            ) {
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = s.input,
                        onValueChange = vm::setInput,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("글을 남겨보세요") },
                        maxLines = 4,
                        keyboardActions = KeyboardActions(onDone = {
                            if (s.input.isNotBlank() && !s.isLoading && !s.isRefreshing) vm.submit()
                        }),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = vm::submit,
                        enabled = s.input.isNotBlank() && !s.isLoading && !s.isRefreshing
                    ) { Text("작성") }
                }
            }
        }
    ) { inner ->
        // 본문: 리스트 + 당겨서 새로고침
        SwipeRefresh(
            state = refreshState,
            onRefresh = { vm.refresh() },
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            if (s.visibleItems.isEmpty() && !s.isLoading && !s.isRefreshing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("아직 작성된 글이 없습니다.")
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp) // ✅ 하단 입력바 높이만큼 여유
                ) {
                    items(s.visibleItems, key = { it.id }) { c ->
                        CommentCard(
                            author = "작성자 #${c.memberId}",
                            time = formatTime(c.createdAt),
                            body = c.body
                        )
                    }
                    if ((s.isLoading || s.isRefreshing) && s.visibleItems.isNotEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentCard(author: String, time: String, body: String) {
    ElevatedCard {
        Column(Modifier
            .fillMaxWidth()
            .padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(author, fontWeight = FontWeight.Medium)
                Text(time, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(body, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTime(raw: String?): String =
    runCatching {
        OffsetDateTime.parse(raw ?: return "방금").format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }.getOrElse { raw ?: "방금" }