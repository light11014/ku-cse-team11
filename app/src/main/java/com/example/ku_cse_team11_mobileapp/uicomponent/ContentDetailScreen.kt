package com.example.ku_cse_team11_mobileapp.uicomponent

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.StarRate
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import com.example.ku_cse_team11_mobileapp.model.viewmodel.ContentDetailViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import androidx.core.net.toUri

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    contentId: Int,
    navController: NavHostController,
    repo: ContentRepository = ServiceLocator.repo
) {
    val vm: ContentDetailViewModel = viewModel(
        factory = ContentDetailViewModel.Factory(repo, contentId)
    )
    val state by vm.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("작품 상세") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    FavoriteStar(
                        contentId = contentId,
                        onError = { /* 스낵바/토스트로 표시해도 좋음 */ }
                    )
                }
            )
        }
    ) { inner ->
        when (val s = state) {
            is ContentDetailViewModel.UiState.Loading ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner), contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            is ContentDetailViewModel.UiState.Error ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner), contentAlignment = Alignment.Center
                ) {
                    Text("오류: ${s.message}")
                }

            is ContentDetailViewModel.UiState.Success -> {
                val c = s.data
                val tags = remember(c.tags) { parseTags(c.tags) }

                val favoriteCount by produceState(initialValue = 0, key1 = contentId) {
                    value = runCatching { repo.getFavoriteCount(contentId) }.getOrDefault(0)
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 헤더 이미지
                    item {
                        AsyncImage(
                            model = fixUrl(c.thumbnailUrl),
                            contentDescription = c.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(440.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val link = c.contentUrl
                                    val url = fixUrl(link)
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, url?.toUri()))
                                }
                        )
                    }

                    // 제목/작가/타입/플랫폼
                    item {
                        SectionCard(title = "작품 정보") {
                            Text(
                                c.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(c.authors, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))

                            val typeKo = koContentType(c.contentType)
                            val platformKo = koPlatform(c.platform)

                            FlowChips(
                                items = listOfNotNull(
                                    nb(typeKo),
                                    nb(platformKo),
                                    nb(c.category)
                                )
                            )
                        }
                    }
                    item {
                        val desc = ne(c.description)
                        if (desc.isNotBlank()) {
                            SectionCard(title = "설명") {
                                Text(desc)
                            }
                        }
                    }
                    // 통계 (조회/좋아요/평점)
                    item {
                        val hasStats = (c.views > 0) || (c.likes > 0) || (c.rating > 0.0)
                        if (hasStats) {
                            SectionCard(title = "통계") {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    if (c.views > 0) {
                                        StatChip(
                                            Icons.Filled.Visibility,
                                            "조회수",
                                            formatCount(c.views)
                                        )
                                    }
                                    if (c.likes > 0) {
                                        StatChip(
                                            Icons.Filled.ThumbUp,
                                            "추천수",
                                            formatCount(c.likes.toLong())
                                        )
                                    }
                                    if (c.rating > 0.0) {
                                        StatChip(
                                            Icons.Filled.StarRate,
                                            "평점",
                                            String.format("%.2f", c.rating)
                                        )
                                    }

                                    if (favoriteCount > 0) StatChip(
                                        Icons.Filled.Favorite,
                                        "즐겨찾기",
                                        formatCount(favoriteCount.toLong())
                                    )
                                }
                            }
                        }
                    }

                    // 회차/연재 정보
                    item {
                        val period = nb(c.pubPeriod)
                        val hasEpisodes = c.totalEpisodes > 0
                        val hasPeriod = !period.isNullOrBlank()
                        if (hasEpisodes || hasPeriod) {
                            SectionCard(title = "연재 정보") {
                                val rows = buildList {
                                    if (hasEpisodes) add("총 에피소드" to "${c.totalEpisodes}화")
                                    if (hasPeriod) add("연재 주기" to period!!)
                                }
                                KeyValueRows(rows)
                            }
                        }
                    }

                    // 태그
                    if (tags.isNotEmpty()) {
                        item {
                            SectionCard(title = "태그") {
                                FlowChips(items = tags)
                            }
                        }
                    }

                    val link = c.contentUrl
                    if (!link.isNullOrBlank()) {
                        item {
                            SectionCard(title = "바로가기") {
                                Button(onClick = {
                                    val url = fixUrl(link)
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, url?.toUri()))
                                }) {
                                    Icon(Icons.Filled.Link, contentDescription = null); Spacer(
                                    Modifier.width(8.dp)
                                ); Text("플랫폼에서 보기")
                                }
                            }
                        }
                    }


                    item {
                        SectionCard(title = "커뮤니티") {
                            Text("이 작품의 커뮤니티에서 글을 남겨보세요.")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { navController.navigate("community/$contentId") }) {
                                Text("커뮤니티로 이동")
                            }
                        }
                    }

                    item {
                        SectionCard(title = "티어리스트") {
                            Text("이 작품의 티어를 선택하세요. (S / F / Unknown)")
                            Spacer(Modifier.height(8.dp))
                            TierSelector(
                                contentId = contentId,
                                onChanged = { selected ->
                                    // TODO: 나중에 API 저장 붙일 자리
                                    // e.g. community/ratingsRepo.saveTier(contentId, selected)
                                }
                            )
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }


                }
            }
        }
    }
}

/* -------------------- 재사용 가능한 작은 컴포저블들 -------------------- */

@Composable
private fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    ElevatedAssistChip(
        onClick = {},
        label = { Text("$label: $value") },
        leadingIcon = { Icon(icon, contentDescription = label) }
    )
}

@Composable
private fun KeyValueRows(rows: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { (k, v) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(k, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(v, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* -------------------- 유틸 -------------------- */

@Composable
private fun FlowRowWrap(content: @Composable () -> Unit) {
    Row { content() }
}

// 기존 parseTags 를 아래로 교체
private fun parseTags(raw: String?): List<String> {
    if (raw.isNullOrBlank()) return emptyList()

    // 전체가 "N/A" 류면 바로 비우기
    val whole = raw.trim()
    val isWholeNa = whole.equals("N/A", true)
            || whole.equals("NA", true)
            || whole.equals("null", true)
            || whole == "없음"
    if (isWholeNa) return emptyList()

    // 구분자: , | / · # 개행 등 (공백 하나만으로는 자르지 않음)
    val tokens = whole.split(Regex("[,|/·#\\r\\n]+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        // 개별 토큰에 섞여 들어온 N/A 류도 제거
        .filterNot {
            it.equals("N/A", true) ||
                    it.equals("NA", true) ||
                    it.equals("null", true) ||
                    it == "없음"
        }
        .distinct()

    return tokens
}

private fun formatCount(n: Long): String {
    return when {
        n < 1_000 -> n.toString()
        n < 1_000_000 -> String.format("%.1fK", n / 1_000.0)
        n < 1_000_000_000 -> String.format("%.1fM", n / 1_000_000.0)
        else -> String.format("%.1fB", n / 1_000_000_000.0)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "정보 없음"
    return runCatching {
        val dt = OffsetDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }.getOrElse { raw }
}

public fun fixUrl(url: String?): String? =
    url?.let { if (it.startsWith("//")) "https:$it" else it }

private fun nb(value: Any?): String? {
    val s = value as? String ?: return null
    return s.ifBlank { null }
}

/** String? 를 항상 non-null String으로 */
private fun ne(value: Any?): String = (value as? String).orEmpty()

private fun koContentType(raw: String?): String? = when (raw) {
    "WEBTOON" -> "웹툰"
    "WEBNOVEL" -> "웹소설"
    else -> raw
}

private fun koPlatform(raw: String?): String? = when (raw) {
    "KAKAO_WEBTOON" -> "카카오웹툰"
    "KAKAO_PAGE" -> "카카오페이지"
    "NAVER_WEBTOON" -> "네이버웹툰"
    "NOVELPIA" -> "노벨피아"
    "MUNPIA" -> "문피아"
    else -> raw
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowChips(items: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { text ->
            AssistChip(
                onClick = {},
                label = { Text(text, maxLines = 1) }, // 칩 내부 텍스트는 1줄(너무 길면 칩 안에서만 … 처리)
                modifier = Modifier
            )
        }
    }
}