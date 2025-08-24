package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.R
import java.util.Locale

/* ---------- 공용 유틸 ---------- */

private fun Long.toCompact(): String = when {
    this >= 1_000_000 -> String.format(Locale.US, "%.1fM", this / 1_000_000.0)
    this >= 1_000     -> String.format(Locale.US, "%.1fk", this / 1_000.0)
    else              -> this.toString()
}

@Composable
private fun valueOrNA(value: String?): String =
    value?.takeIf { it.isNotBlank() } ?: "정보 없음"

@Composable
private fun CoverImage(url: String?, contentDesc: String, modifier: Modifier = Modifier) {
    val inPreview = LocalInspectionMode.current
    if (inPreview || url.isNullOrBlank()) {
        Image(
            painter = painterResource(id = R.mipmap.ic_launcher),
            contentDescription = contentDesc,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = url,
            contentDescription = contentDesc,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String?, emphasize: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .widthIn(min = 96.dp)
                .padding(end = 8.dp)
        )
        Text(
            text = valueOrNA(value),
            style = if (emphasize) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/* ---------- 상세 화면 ---------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    node: CreateNode,
    isFavorite: Boolean,                  // ★ 추가
    onToggleFavorite: (Long) -> Unit,
    onGoCommunity: (id: Long, title: String) -> Unit
) {
    val uri = LocalUriHandler.current
    Scaffold(
        topBar = { TopAppBar(
            title = { Text(node.title) },
            actions = {
                IconButton(onClick = { onToggleFavorite(node.id) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )},
        bottomBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { node.contentUrl?.let { uri.openUri(it) } }
                ) { Text("작품 보러가기") }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { onGoCommunity(node.id, node.title) }
                ) { Text("커뮤니티로 이동") }
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1) 썸네일 / 기본 타이틀 카드
            SectionCard("표지 & 타이틀") {
                Row {
                    CoverImage(
                        url = node.thumbnailUrl,
                        contentDesc = "${node.title} 표지",
                        modifier = Modifier
                            .size(width = 120.dp, height = 170.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        InfoRow("제목", node.title, emphasize = true)
                        InfoRow("작가", node.author, emphasize = true)
                    }
                }
            }

            // 2) 분류 카드 (열거형/상태)
            SectionCard("분류") {
                InfoRow("타입", node.type.name)
                InfoRow("플랫폼", node.platform.name)
            }

            // 3) 카운트/통계 카드
            SectionCard("통계") {
                InfoRow("조회수", node.views.toCompact())
                InfoRow("좋아요", node.likes.toCompact())
                InfoRow("평점", if (node.rating > 0.0) String.format(Locale.US, "%.1f", node.rating) else "정보 없음")
                InfoRow("회차 수", node.episodeCount?.toString())
            }

            // 4) 설명/링크 카드
            SectionCard("설명 & 링크") {
                InfoRow("설명", node.description ?: "정보 없음")
                InfoRow("작품 링크", node.contentUrl)
            }

            // 5) 날짜 카드
            SectionCard("날짜") {
                InfoRow("연재 시작일", node.publishDate)
                InfoRow("생성일", node.createdAt)
                InfoRow("수정일", node.updatedAt)
            }

            // 6) 태그/분류 카드
            SectionCard("태그/분류 정보") {
                val tags = remember(node.tags) { node.tags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } }
                InfoRow("장르", node.genre)
                InfoRow("연령등급", node.ageRating)
                InfoRow("업데이트 주기", node.updateFrequency)
                Spacer(Modifier.height(4.dp))
                Text("태그", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                if (tags.isNullOrEmpty()) {
                    Text("정보 없음", style = MaterialTheme.typography.bodyMedium)
                } else {
                    FlowChips(tags)
                }
            }
        }
    }
}

/* 간단한 칩 나열 */
@Composable
private fun FlowChips(items: List<String>) {
    // 간단 버전: 줄바꿈 없이 좌우 스크롤 없는 일렬. 필요시 FlowRow로 교체 가능
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { tag ->
            AssistChip(onClick = {}, label = { Text(tag) })
        }
    }
}
