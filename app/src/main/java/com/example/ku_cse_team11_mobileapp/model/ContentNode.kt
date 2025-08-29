package com.example.ku_cse_team11_mobileapp.model

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.ku_cse_team11_mobileapp.R
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary

@Composable
fun ContentNode(
    content: ContentSummary,
    onClick: (Int) -> Unit,
    rank: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .height(220.dp)
            .padding(8.dp)
            .clickable { onClick(content.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            // 이미지 (crossfade + url 보정)
            val ctx = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(ctx)
                    .data(fixUrl(content.thumbnailUrl))
                    .crossfade(true)
                    .build(),
                contentDescription = content.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // ✅ 좌측 상단 등수 뱃지 (크게 + 컬러)
            rank?.let { r ->
                RankBadge(
                    rank = r,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                )
            }

            // 우상단: Tier 배지 (S/A/B/C/D/F/None)
            TierBadge(
                tier = content.tier,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )

            platformIconRes(content.platform)?.let { resId ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 6.dp, top = 38.dp) // 랭크가 여기면 top = 28.dp 로 조정
                        .background(
                            color = Color.Black.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 3.dp)
                ) {
                    Image(
                        painter = painterResource(resId),
                        contentDescription = "플랫폼",
                        modifier = Modifier.size(18.dp) // ← 작게
                    )
                }
            }
            // 하단 스크림(그라데이션)로 글씨 가독성 ↑
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(70.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.4f to Color(0x66000000),
                            1f to Color(0xCC000000)
                        )
                    )
            )

            // 조회수/추천수 (오른쪽 하단 오버레이)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp)
                    .offset(y = (-60).dp) // 스크림 위 여백
                    .background(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "조회수",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )

                Text(
                    text = if (content.views != 0L) compactCount(content.views) else "-",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, end = 8.dp)
                )
            }

            // 하단: 제목/작가
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = content.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = content.authors,
                    color = Color(0xFFEEEEEE),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RankBadge(rank: Int, modifier: Modifier = Modifier) {
    // 눈에 잘 띄는 칩
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF111111).copy(alpha = 0.7f),
        tonalElevation = 0.dp,
        shadowElevation = 2.dp
    ) {
        Text(
            text = "#$rank",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
@Composable
private fun TierBadge(tier: String?, modifier: Modifier = Modifier) {
    val label = when (tier?.trim()?.uppercase()) {
        "S","A","B","C","D","F" -> tier.uppercase()
        "NONE", null, "" -> "N" // None일 땐 N으로 단축 표기
        else -> tier.uppercase()
    }
    val color = when (label) {
        "S" -> Color(0xFF7C4DFF)   // 보라
        "A" -> Color(0xFF42A5F5)   // 파랑
        "B" -> Color(0xFF26A69A)   // 청록
        "C" -> Color(0xFFFFCA28)   // 노랑
        "D" -> Color(0xFFEF5350)   // 빨강
        "F" -> Color(0xFFB71C1C)   // 짙은 빨강
        "N" -> Color(0xFF9E9E9E)   // 회색 (None)
        else -> MaterialTheme.colorScheme.primary
    }

    // 동그란 마크 + 문자
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/* ----------------- Utils ----------------- */

private fun formatCount(n: Long): String =
    when {
        n < 1_000 -> n.toString()
        n < 1_000_000 -> String.format("%.1fK", n / 1_000.0)
        n < 1_000_000_000 -> String.format("%.1fM", n / 1_000_000.0)
        else -> String.format("%.1fB", n / 1_000_000_000.0)
    }
@Composable
@Stable
private fun rankBadgeColors(rank: Int): Pair<Color, Color> = when (rank) {
    1 -> Color(0xFFFFD54F) to Color(0xFF5D4037) // Gold bg, dark text
    2 -> Color(0xFFCFD8DC) to Color(0xFF263238) // Silver bg
    3 -> Color(0xFFBCAAA4) to Color(0xFF3E2723) // Bronze bg
    else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
}

private fun compactCount(n: Long): String = when {
    n < 1_000 -> n.toString()
    n < 1_000_000 -> String.format("%.1fK", n / 1_000.0)
    n < 1_000_000_000 -> String.format("%.1fM", n / 1_000_000.0)
    else -> String.format("%.1fB", n / 1_000_000_000.0)
}

private fun fixUrl(url: String?): String? =
    url?.let { if (it.startsWith("//")) "https:$it" else it }

@DrawableRes
private fun platformIconRes(platform: String?): Int? = when (platform?.trim()?.uppercase()) {
    "KAKAO_PAGE"    -> R.drawable.kakaopage_image
    "KAKAO_WEBTOON" -> R.drawable.kakaowebtoon_image
    "NAVER_WEBTOON" -> R.drawable.naverwebtoon_image
    "NOVELPIA"      -> R.drawable.novelpia_image
    "MUNPIA", "MOONPIA" -> R.drawable.munpia_image
    "SERIES"        -> R.drawable.series_image
    "WEBNOVEL" -> R.drawable.webnovel_image
    else -> null
}