package com.example.ku_cse_team11_mobileapp.uicomponent

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.repository.ContentRepository
import com.example.ku_cse_team11_mobileapp.model.viewmodel.ContentDetailViewModel
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ContentDetailScreen(
    contentId: Int,
    navController: NavHostController,
    repo: ContentRepository = ServiceLocator.repo,
    lang: String? = "kr"
) {
    val vm: ContentDetailViewModel = viewModel(
        key = "detail-$contentId",
        factory = ContentDetailViewModel.Factory(repo, contentId, lang)
    )
    val state by vm.uiState.collectAsStateWithLifecycle()

    // 로그인 세션에서 memberId 관찰 → 값 바뀌면 상세 재조회
    val memberId by ServiceLocator.session.memberIdFlow.collectAsStateWithLifecycle(initialValue = null)
    LaunchedEffect(memberId) { vm.load(memberId, lang) }

    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                    // 별 토글 (내 즐겨찾기)
                    FavoriteStar(
                        contentId = contentId,
                        onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { inner ->
        when (val s = state) {
            is ContentDetailViewModel.UiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is ContentDetailViewModel.UiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) { Text("오류: ${s.message}") }

            is ContentDetailViewModel.UiState.Success -> {
                val c = s.data
                val tags = parseTags(c.tags)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 썸네일
                    item {
                        AsyncImage(
                            model = fixUrl(c.thumbnailUrl),
                            contentDescription = c.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    // 작품 정보
                    item {
                        SectionCard(title = "작품 정보") {
                            val badgeTier = remember(s.data.avgTier) {
                                normalizeTierLabel(s.data.avgTier)
                            }
                            Text(
                                c.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(c.authors, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                mapContentTypeKo(c.contentType)?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                                mapPlatformKo(c.platform)?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                                c.category?.takeIf { it.isNotBlank() }?.let {
                                    AssistChip(onClick = {}, label = { Text(it) })
                                }
                                c.ageRating?.takeIf { it.isNotBlank() }?.let {
                                    AssistChip(onClick = {}, label = { Text("연령등급: $it") })
                                }
                                if (badgeTier != null) {
                                    TierBadgeLarge(
                                        tier = badgeTier,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }

                    }

                    // 연재 정보
                    val hasEpisodeInfo = (c.totalEpisodes > 0) || !c.pubPeriod.isNullOrBlank()
                    if (hasEpisodeInfo) {
                        item {
                            SectionCard(title = "연재 정보") {
                                val rows = buildList {
                                    if (c.totalEpisodes > 0) add("총 에피소드" to "${c.totalEpisodes}화")
                                    if (!c.pubPeriod.isNullOrBlank()) add("연재 주기" to c.pubPeriod!!)
                                }
                                KeyValueRows(rows)
                            }
                        }
                    }

                    // 태그 (줄바꿈)
                    if (tags.isNotEmpty()) {
                        item {
                            SectionCard(title = "태그") {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    tags.forEach { t ->
                                        AssistChip(onClick = {}, label = { Text(t, maxLines = 1) })
                                    }
                                }
                            }
                        }
                    }

                    // 설명
                    c.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        item {
                            SectionCard(title = "설명") { Text(desc) }
                        }
                    }

                    // 외부 링크
                    c.contentUrl?.takeIf { it.isNotBlank() }?.let { link ->
                        item {
                            SectionCard(title = "바로가기") {
                                Button(onClick = {
                                    val url = fixUrl(link) ?: link
                                    ctx.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                                }) {
                                    Icon(Icons.Filled.Link, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("플랫폼에서 보기")
                                }
                            }
                        }
                    }

                    // 메타 (있을 때만)
                    val metaRows = buildList {
                        c.createdAt?.takeIf { it.isNotBlank() }?.let { add("생성일" to formatDate(it)) }
                        c.updatedAt?.takeIf { it.isNotBlank() }?.let { add("수정일" to formatDate(it)) }
                        add("ID" to c.id.toString())
                        c.language?.takeIf { it.isNotBlank() }?.let { add("언어" to it) }
                        add("조회수" to formatCount(c.views))
                        add("추천수" to c.likes.toString())
                    }
                    if (metaRows.isNotEmpty()) {
                        item { SectionCard(title = "메타데이터") { KeyValueRows(metaRows) } }
                    }

                    // 내 티어 / 평균 티어 / 참여 수
                    item {
                        SectionCard(title = "티어 요약") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                c.myTier?.takeIf { it.isNotBlank() }?.let {
                                    AssistChip(onClick = {}, label = { Text("내 티어: $it") })
                                }
                                c.avgTier?.takeIf { it.isNotBlank() }?.let {
                                    AssistChip(onClick = {}, label = { Text("평균 티어: $it") })
                                }
                                Text(
                                    "참여 ${c.stats?.ratingCount ?: 0}명",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 티어 분포 그래프
                    item {
                        SectionCard(title = "티어 분포") {
                            TierBarChart(
                                rating = c.stats?.rating ?: emptyMap(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                    }

                    // 커뮤니티
                    item {
                        SectionCard(title = "커뮤니티") {
                            Text("이 작품의 커뮤니티에서 글을 남겨보세요.")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { navController.navigate("community/$contentId") }) {
                                Text("커뮤니티로 이동")
                            }
                        }
                    }

                    // 티어 등록 (선택 시 서버 전송)
                    item {
                        SectionCard(title = "티어리스트 등록") {
                            val mid = memberId
                            TierSelector(
                                contentId = contentId,
                                onChanged = { chosen ->
                                    if (mid == null) {
                                        scope.launch { snackbarHostState.showSnackbar("로그인이 필요합니다.") }
                                        return@TierSelector
                                    }
                                    scope.launch {
                                        runCatching {
                                            // repo.postTier: 이전에 구현한 API 호출 사용
                                            repo.postTier(contentId, mid, toApiTier(chosen.name))
                                        }.onSuccess { res ->
                                            snackbarHostState.showSnackbar("티어 등록: ${res.tier} (score=${res.score})")
                                            // 등록 후 상세 재조회(내 티어/통계 갱신)
                                            vm.load(memberId = mid, lang = lang)
                                        }.onFailure { e ->
                                            snackbarHostState.showSnackbar(e.message ?: "요청 실패")
                                        }
                                    }
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

/* -------------------- 재사용 Card/Row/Chip -------------------- */

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

/* -------------------- 티어 분포 간단 바차트 -------------------- */
@Composable
private fun TierBarChart(
    rating: Map<String, Int>,
    modifier: Modifier = Modifier
) {
    val order = listOf("S", "A", "B", "C", "D")
    val data = order.map { it to (rating[it] ?: 0) }
    val max = data.maxOfOrNull { it.second } ?: 0
    val barAreaHeight = 120.dp

    Column(modifier) {
        // 막대 영역
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { (tier, count) ->
                val frac = if (max > 0) (count / max.toFloat()).coerceIn(0f, 1f) else 0f

                Column(
                    Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 막대 컨테이너(고정 높이)
                    Box(
                        Modifier
                            .height(barAreaHeight)
                            .fillMaxWidth()
                    ) {
                        // 실제 바(아래 정렬 + 비율 높이)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(frac)
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(6.dp))
                                .background(barColorForTier(tier))
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(tier, style = MaterialTheme.typography.labelMedium)
                    Text(
                        "$count",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun barColorForTier(tier: String): Color = when (tier.uppercase()) {
    "S" -> Color(0xFF7C4DFF)   // 보라
    "A" -> Color(0xFF42A5F5)   // 파랑
    "B" -> Color(0xFF26A69A)   // 청록
    "C" -> Color(0xFFFFCA28)   // 노랑
    "D" -> Color(0xFFEF5350)   // 빨강
    else -> MaterialTheme.colorScheme.primary
}

/* -------------------- 유틸 -------------------- */

private fun parseTags(tags: String?): List<String> =
    tags?.split(',', ' ', '·', '|', '/', '#')
        ?.map { it.trim() }
        ?.filter { it.isNotBlank() && !it.equals("N/A", true) }
        ?.distinct()
        ?: emptyList()

@RequiresApi(Build.VERSION_CODES.O)
private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "정보 없음"
    return runCatching {
        val dt = OffsetDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }.getOrElse { raw }
}

private fun formatCount(n: Long): String =
    when {
        n < 1_000 -> n.toString()
        n < 1_000_000 -> String.format("%.1fK", n / 1_000.0)
        n < 1_000_000_000 -> String.format("%.1fM", n / 1_000_000.0)
        else -> String.format("%.1fB", n / 1_000_000_000.0)
    }

private fun fixUrl(url: String?): String? =
    url?.let { if (it.startsWith("//")) "https:$it" else it }

/** 영어 코드 → 한글 라벨 */
private fun mapContentTypeKo(type: String?): String? = when (type?.uppercase()) {
    "WEBNOVEL" -> "웹소설"
    "WEBTOON"  -> "웹툰"
    else       -> null
}

private fun mapPlatformKo(p: String?): String? = when (p?.uppercase()) {
    "KAKAO_PAGE"    -> "카카오페이지"
    "KAKAO_WEBTOON" -> "카카오웹툰"
    "NAVER_WEBTOON" -> "네이버웹툰"
    "NOVELPIA"      -> "노벨피아"
    "MUNPIA", "MOONPIA", "MUNIPIA" -> "문피아"
    "ALL"           -> "전체"
    else            -> p
}

/** UI 선택값 → API 전송용 티어 문자열 */
private fun toApiTier(ui: String): String = when (ui.uppercase()) {
    "S","A","B","C","D","F" -> ui.uppercase()
    "UNKNOWN","UN","U","?"  -> "UNKNOWN"
    else -> ui.uppercase()
}

private fun normalizeTierLabel(raw: String?): String? {
    val t = raw?.trim()?.uppercase().orEmpty()
    if (t.isEmpty()) return null
    return when (t) {
        "S","A","B","C","D","F" -> t
        "NONE","NULL","N/A"     -> "N"   // None은 N으로 축약 표시
        else                    -> t     // 혹시 모르는 값은 있는 그대로
    }
}

@Composable
private fun TierBadgeLarge(
    tier: String,
    modifier: Modifier = Modifier
) {
    val color = when (tier) {
        "S" -> Color(0xFF7C4DFF)   // 보라
        "A" -> Color(0xFF42A5F5)   // 파랑
        "B" -> Color(0xFF26A69A)   // 청록
        "C" -> Color(0xFFFFCA28)   // 노랑
        "D" -> Color(0xFFEF5350)   // 빨강
        "F" -> Color(0xFFB71C1C)   // 짙은 빨강
        "N" -> Color(0xFF9E9E9E)   // 회색(None)
        else -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 큰 원형 배지
        Box(
            modifier = Modifier
                .size(80.dp)                    // ← 크기 조절 포인트
                .clip(RoundedCornerShape(40.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tier,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )
        }
        // 라벨(선택)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "TIER",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
