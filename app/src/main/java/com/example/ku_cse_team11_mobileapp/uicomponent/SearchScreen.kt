// SearchScreen.kt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import com.example.ku_cse_team11_mobileapp.uicomponent.Node
private enum class SearchScope { ALL, TITLE, AUTHOR, TAGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    nodes: List<CreateNode>,
    favoriteIds: Set<Long>,
    onToggleFavorite: (Long) -> Unit,
    onNodeClick: (CreateNode) -> Unit,
    onBack: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var scope by rememberSaveable { mutableStateOf(SearchScope.ALL) }
    val q = remember(query) { query.trim().lowercase() }

    val filtered by remember(nodes, q, scope) {
        derivedStateOf {
            if (q.isEmpty()) nodes
            else {
                when (scope) {
                    SearchScope.ALL -> nodes.filter { n ->
                        n.title.lowercase().contains(q) ||
                                n.author.lowercase().contains(q) ||
                                (n.tags ?: "").lowercase().contains(q) ||
                                (n.description ?: "").lowercase().contains(q)
                    }
                    SearchScope.TITLE -> nodes.filter { it.title.lowercase().contains(q) }
                    SearchScope.AUTHOR -> nodes.filter { it.author.lowercase().contains(q) }
                    SearchScope.TAGS -> nodes.filter { (it.tags ?: "").lowercase().contains(q) }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("검색어를 입력하세요") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Outlined.Close, contentDescription = "지우기")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* 키보드 내리기 원하면 FocusManager로 */ })
                    )
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // 🔘 검색 범위 토글 (태그 / 제목 / 작가 / 전체)
            SearchScopeSelector(
                scope = scope,
                onChange = { scope = it },
                trailingCount = filtered.size
            )

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다.")
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
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScopeSelector(
    scope: SearchScope,
    onChange: (SearchScope) -> Unit,
    trailingCount: Int
) {
    val items = listOf(
        SearchScope.ALL to "전체",
        SearchScope.TITLE to "제목",
        SearchScope.AUTHOR to "작가",
        SearchScope.TAGS to "태그"
    )

    Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEachIndexed { index, (value, label) ->
                SegmentedButton(
                    selected = scope == value,
                    onClick = { onChange(value) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size),
                    modifier = Modifier.weight(1f) // ★ 네 개를 동일 폭으로 꽉 차게
                ) {
                    Text(label, maxLines = 1)
                }
            }
        }

        // 오른쪽 정렬 결과 개수(옵션)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            Spacer(Modifier.weight(1f))
            AssistChip(onClick = {}, label = { Text("$trailingCount") })
        }
    }
}
