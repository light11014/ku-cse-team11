package com.example.ku_cse_team11_mobileapp.uicomponent

import android.content.Intent
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
                    FavoriteStar(
                        contentId = contentId,
                        onError = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                    )
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },

        // ✅ 커뮤니티 이동 FAB
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate("community/$contentId") },
                icon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                text  = { Text("커뮤니티 글쓰기") }
            )
        }
    ) { inner ->
        when (val s = state) {
            is ContentDetailViewModel.UiState.Loading -> Box(
                Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is ContentDetailViewModel.UiState.Error -> Box(
                Modifier.fillMaxSize().padding(inner),
                contentAlignment = Alignment.Center
            ) { Text("오류: ${s.message}") }

            is ContentDetailViewModel.UiState.Success -> {
                val c = s.data
                val tags = parseTags(c.tags)

                // ✅ 내 티어를 상단에서 상태로 보관 → 화면 즉시 반영
                val myTierFromApi: Tier = parseTier(c.myTier)
                var myTier by rememberSaveable(contentId) { mutableStateOf(myTierFromApi) }
                LaunchedEffect(myTierFromApi) {
                    myTier = myTierFromApi
                }
                // ✅ 선택 직후 차트에 즉시 반영(옵티미스틱 카운트 조정)
                val displayRating = remember(c.stats?.rating, myTierFromApi, myTier) {
                    optimisticRatingMap(c.stats?.rating ?: emptyMap(), myTierFromApi, myTier)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(inner),
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
                                .height(500.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    // 작품 정보 + 우측 큰 티어 배지
                    item {
                        SectionCard(title = " ") {
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
                            }

                            Spacer(Modifier.height(8.dp))

                            val badgeTier = remember(c.avgTier) { normalizeTierLabel(c.avgTier) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        c.title,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(c.authors, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (badgeTier != null) {
                                    TierBadgeLarge(
                                        tier = badgeTier,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 설명
                    c.description?.takeIf { it.isNotBlank() }?.let { desc ->
                        item { SectionCard(title = "설명") { Text(desc) } }
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

                    // 바로가기
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

                    // 메타
                    val metaRows = listOf(
                        "조회수" to formatCount(c.views),
                        "추천수" to c.likes.toString()
                    )
                    item { SectionCard(title = "메타데이터") { KeyValueRows(metaRows) } }

                    // ✅ 티어 요약(내 티어는 즉시 반영: myTier 사용)

                    // ✅ 티어 분포(옵티미스틱 반영)
                    item {
                        SectionCard(title = "티어 분포") {
                            TierBarChart(
                                rating = displayRating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(196.dp)
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                    item {
                        SectionCard(title = "티어 요약") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(onClick = {}, label = { Text("내 티어: ${myTier.label}") })
                                c.avgTier?.takeIf { it.isNotBlank() }?.let {
                                    AssistChip(onClick = {}, label = { Text("평균 티어: $it") })
                                }
                            }
                        }
                    }


                    // ✅ 티어 선택(선택 즉시 myTier 반영 + 서버 전송)
                    item {
                        SectionCard(title = "티어리스트 등록") {
                            TierSelector(
                                selected = myTier,
                                onSelect = { chosen ->
                                    myTier = chosen
                                    if (memberId == null) {
                                        scope.launch { snackbarHostState.showSnackbar("로그인이 필요합니다.") }
                                        return@TierSelector
                                    }
                                    scope.launch {
                                        runCatching {
                                            repo.postTier(contentId, memberId!!, chosen.name)
                                        }.onSuccess {
                                            snackbarHostState.showSnackbar("티어 등록: ${chosen.label}")
                                            // 서버 반영 후 최신 데이터 다시 받고 싶으면↓
                                            // vm.load(memberId, lang)
                                        }.onFailure { e ->
                                            snackbarHostState.showSnackbar(e.message ?: "요청 실패")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(150.dp)) }
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
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (title.isNotBlank()) Text(title, style = MaterialTheme.typography.titleMedium)
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
    val order = listOf("S", "A", "B", "C", "D", "F")
    val data = order.map { it to (rating[it] ?: 0) }
    val max = data.maxOfOrNull { it.second } ?: 0

    BoxWithConstraints(modifier) {
        // 라벨(등급+숫자)와 간격을 위해 확보할 높이
        val labelSpace = 44.dp
        // 부모가 높이를 제시하면 그에 맞춰 막대영역 계산, 아니면 기본값 사용
        val barAreaHeight =
            if (constraints.hasBoundedHeight)
                (maxHeight - labelSpace).coerceAtLeast(100.dp)
            else
                150.dp

        Column {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { (tier, count) ->
                    val frac = if (max > 0) (count / max.toFloat()).coerceIn(0f, 1f) else 0f
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier
                                .height(barAreaHeight)
                                .fillMaxWidth()
                        ) {
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
}

@Composable
private fun barColorForTier(tier: String): Color = when (tier.uppercase()) {
    "S" -> Color(0xFF2ECC71)
    "A" -> Color(0xFFF1C40F)
    "B" -> Color(0xFF3498DB)
    "C" -> Color(0xFF9B59B6)
    "D" -> Color(0xFFE67E22)
    "F" -> Color(0xFFE74C3C)
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

private fun normalizeTierLabel(raw: String?): String? {
    val t = raw?.trim()?.uppercase().orEmpty()
    if (t.isEmpty()) return null
    return when (t) {
        "S","A","B","C","D","F" -> t
        "NONE","NULL","N/A"     -> "N"
        else                    -> t
    }
}

// ✅ 내 기존 티어→새 티어로 카운트 조정(옵티미스틱)
private fun optimisticRatingMap(
    original: Map<String, Int>,
    prev: Tier,
    curr: Tier
): Map<String, Int> {
    fun k(t: Tier) = when (t) {
        else -> t.name
    }
    val m = original.toMutableMap()
    return m
}

/* ====== TierBadgeLarge / TierSelector, barColorForTier 등은 기존 그대로 사용 ====== */

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

fun parseTier(s: String?): Tier =
    Tier.entries.firstOrNull { it.name.equals(s ?: "", true) } ?: Tier.S