package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.ContentNode
import com.example.ku_cse_team11_mobileapp.model.viewmodel.SearchFilters
import com.example.ku_cse_team11_mobileapp.model.viewmodel.SearchViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    vm: SearchViewModel = viewModel(factory = SearchViewModel.Factory(ServiceLocator.repo))
) {
    val s by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("검색") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (s.results.isNotEmpty()) {
                        TextButton(onClick = { vm.clearResults() }) { Text("초기화") } // 아래 4) 참고
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SearchFiltersForm(
                filters = s.filters,
                onChange = { vm.setFilters(it) },       // ✅ 필터 통째로 반영
                onSearch = { vm.searchFirstPage() }     // ✅ 그 다음 검색 호출
            )
            Divider()

            // 결과
            when {
                s.isLoading && s.results.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }

                s.results.isEmpty() ->
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("검색 결과가 없습니다")
                    }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(s.results) { item ->
                            ContentNode(
                                content = item,
                                onClick = { id -> navController.navigate("content/$id") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            if (!s.last) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    Button(onClick = { vm.loadNextPage() }, enabled = !s.isLoading) {
                                        if (s.isLoading) {
                                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                            Spacer(Modifier.width(8.dp))
                                        }
                                        Text("더 보기")
                                    }
                                }
                            } else {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text("마지막 페이지입니다", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFiltersForm(
    filters: SearchFilters,
    onChange: (SearchFilters) -> Unit,
    onSearch: () -> Unit
) {
    var keyword by rememberSaveable { mutableStateOf(filters.keyword.orEmpty()) }
    var contentType by rememberSaveable { mutableStateOf(filters.contentType) } // "WEBTOON"/"WEBNOVEL"
    var platform by rememberSaveable { mutableStateOf(filters.platform) }       // "KAKAO_WEBTOON" 등
    var minEp by rememberSaveable { mutableStateOf(filters.minEpisode?.toString().orEmpty()) }
    var maxEp by rememberSaveable { mutableStateOf(filters.maxEpisode?.toString().orEmpty()) }
    var size by rememberSaveable { mutableStateOf(filters.size.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = keyword,
            onValueChange = { keyword = it },
            label = { Text("제목 키워드 (2자 이상)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 간단 드롭다운 유틸은 기존에 만든 ExposedDropdownTextField 사용
        ExposedDropdownTextField(
            label = "콘텐츠 유형",
            value = contentType,
            options = listOf(null, "WEBTOON", "WEBNOVEL")
        ) { selected -> contentType = selected }

        ExposedDropdownTextField(
            label = "플랫폼",
            value = platform,
            options = listOf(null, "NAVER_WEBTOON", "KAKAO_WEBTOON", "KAKAO_PAGE", "NOVELPIA")
        ) { selected -> platform = selected }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minEp,
                onValueChange = { minEp = it.filter(Char::isDigit) },
                label = { Text("최소 에피소드") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxEp,
                onValueChange = { maxEp = it.filter(Char::isDigit) },
                label = { Text("최대 에피소드") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = size,
                onValueChange = { size = it.filter(Char::isDigit).ifEmpty { "20" } },
                label = { Text("페이지 크기") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(160.dp)
            )
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                val newFilters = filters.copy(
                    keyword = keyword.ifBlank { null },          // 공백이면 null
                    contentType = contentType,                   // "WEBTOON"/"WEBNOVEL" or null
                    platform = platform,                         // "KAKAO_WEBTOON" 등 or null
                    minEpisode = minEp.toIntOrNull(),
                    maxEpisode = maxEp.toIntOrNull(),
                    page = 0,
                    size = size.toIntOrNull() ?: 20,
                    lang = "kr"
                )

                onChange(newFilters)    // ✅ 먼저 상태에 반영
                onSearch()
            }) { Text("검색") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownTextField(
    label: String,
    value: String?,                  // 현재 선택 값 (null이면 "전체"로 표시)
    options: List<String?>,          // 선택 옵션
    onSelected: (String?) -> Unit    // ← 반드시 인자 1개를 받도록!
) {
    var expanded by remember { mutableStateOf(false) }
    val display = value ?: "전체"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(
                    text = { Text(opt ?: "전체") },
                    onClick = {
                        onSelected(opt)     // ← 선택값을 콜백으로 전달
                        expanded = false
                    }
                )
            }
        }
    }
}