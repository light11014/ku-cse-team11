package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class ChatMessage(
    val id: String,
    val author: String,
    val text: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    nodeId: Long,         // 방 식별
    title: String         // 상단에는 제목만 노출
) {
    // 방별로 독립 상태 유지 (nodeId 를 key 로 사용)
    var input by rememberSaveable(nodeId) { mutableStateOf("") }
    var messages by rememberSaveable(nodeId, stateSaver = listSaver(
        save = { list -> list.flatMap { listOf(it.id, it.author, it.text, it.timestamp.toString()) } },
        restore = { flat ->
            flat.chunked(4).map {
                ChatMessage(it[0], it[1], it[2], it[3].toLong())
            }
        }
    )) {
        mutableStateOf(
            listOf(
                ChatMessage("welcome-$nodeId", "운영자", "‘$title’ 커뮤니티에 오신 것을 환영합니다!", System.currentTimeMillis())
            )
        )
    }

    fun send(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val newMsg = ChatMessage(
            id = System.nanoTime().toString(),
            author = "나",
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )
        messages = messages + newMsg
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        bottomBar = {
            Row(Modifier.fillMaxWidth().padding(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("메시지를 입력하세요") },
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { send(input); input = "" }) { Text("전송") }
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier.padding(inner).fillMaxSize(),
            contentPadding = PaddingValues(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Text(msg.author, style = MaterialTheme.typography.labelMedium)
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
                        Text(msg.text, modifier = Modifier.padding(10.dp))
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