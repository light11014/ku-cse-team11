package com.example.ku_cse_team11_mobileapp.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.api.model.ContentSummary
import com.example.ku_cse_team11_mobileapp.uicomponent.FavoriteStar

@Composable
fun ContentNode(
    content: ContentSummary,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp) // 작은 카드
            .height(220.dp)
            .padding(8.dp)
            .clickable { onClick(content.id) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 썸네일 이미지
            AsyncImage(
                model = content.thumbnailUrl,
                contentDescription = content.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            FavoriteStar(
                contentId = content.id,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )
            // 조회수 & 추천수 (이미지 위에 오른쪽 하단 배치)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .background(
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(topStart = 8.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Views",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "${content.views / 10000}만",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 2.dp, end = 6.dp)
                )
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = "Likes",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            // 제목 & 작가 (아래쪽 배경 살짝 반투명)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(6.dp)
            ) {
                Text(
                    text = content.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = content.authors,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
