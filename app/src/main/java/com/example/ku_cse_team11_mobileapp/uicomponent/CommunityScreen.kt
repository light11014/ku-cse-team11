package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ku_cse_team11_mobileapp.model.community.CommunityViewModel
import com.example.ku_cse_team11_mobileapp.model.community.CommunityViewModelFactory
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    nodeId: Long,
    title: String
) {
    val ctx = LocalContext.current
    val vm: CommunityViewModel = viewModel(
        factory = CommunityViewModelFactory(nodeId, ctx)
    )

    var input by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.loadPosts() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("$title 커뮤니티") }) },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("새 글을 입력하세요") }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        vm.addPost(author = "me", content = input)
                        input = ""
                    }
                }) { Text("등록") }
            }
        }
    ) { inner ->
        when {
            vm.isLoading && vm.posts.isEmpty() -> {
                Box(Modifier.padding(inner).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            vm.error != null && vm.posts.isEmpty() -> {
                Box(Modifier.padding(inner).fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("불러오기에 실패했습니다: ${vm.error}")
                }
            }
            else -> {
                LazyColumn(Modifier.padding(inner).fillMaxSize()) {
                    items(vm.posts, key = { it.id }) { post ->
                        Card(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Column(Modifier.padding(12.dp)) {
                                Text(post.author, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(post.content, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    post.createdAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewCommunityScreen(){
    CommunityScreen(1, "dummy title")
}