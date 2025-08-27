package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.community.CommunityPost
import com.example.ku_cse_team11_mobileapp.model.repository.CommunityRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    contentId: Int,
    navController: NavHostController,
    repo: CommunityRepository = ServiceLocator.communityRepo
) {
    val posts by repo.postsFlow(contentId).collectAsStateWithLifecycle(initialValue = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("작품 커뮤니티") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "글 작성")
                    }
                }
            )
        }
    ) { inner ->
        if (posts.isEmpty()) {
            Box(Modifier.padding(inner).fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("첫 글을 작성해보세요!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(inner)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { p -> PostCard(p) }
            }
        }

        if (showDialog) {
            WritePostDialog(
                onDismiss = { showDialog = false },
                onSubmit = { title, body ->
                    showDialog = false
                    // ✅ Composable 바깥이므로 LaunchedEffect 대신 launch 사용
                    scope.launch {
                        repo.addPost(contentId, title, body, authorOverride = null)
                    }
                }
            )
        }
    }
}

@Composable
private fun PostCard(p: CommunityPost) {
    Card {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${p.author} • ${formatTime(p.createdAt)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (p.body.isNotBlank()) Text(p.body)
        }
    }
}

@Composable
private fun WritePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("글 작성") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    label = { Text("제목") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = body, onValueChange = { body = it },
                    label = { Text("내용(선택)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) onSubmit(title.trim(), body.trim())
                },
                enabled = title.isNotBlank()
            ) { Text("등록") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}


private fun formatTime(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
