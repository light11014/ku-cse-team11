package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import java.util.Locale

@Composable
fun Node(
    node: CreateNode,
    modifier: Modifier = Modifier,
    isFavorite: Boolean,
    onToggleFavorite: (Long) -> Unit,
    onClick: (CreateNode) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = { onClick(node) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box {
            Row(
                modifier = Modifier.padding(12.dp)
            ) {
                // 표지 이미지
                AsyncImage(
                    model = node.thumbnailUrl,
                    contentDescription = "${node.title} 표지",
                    modifier = Modifier
                        .size(width = 100.dp, height = 140.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 텍스트 영역
                Column(
                    modifier = Modifier.weight(1f) // 남은 공간 차지
                ) {
                    Text(
                        text = node.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "저자: ${node.author}",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    node.description?.let {
                        Text(
                            text = it,
                            fontSize = 12.sp,
                            maxLines = 3
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // 하단 메타 정보
                    Row {
                        Text(text = "조회수 ${node.views.toCompactString()}", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "추천 ${node.likes.toCompactString()}", fontSize = 12.sp)
                    }

                }
            }
            IconButton(
                onClick = { onToggleFavorite(node.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun Long.toCompactString(): String {
    return when {
        this >= 1_000_000 -> String.format(Locale.US, "%.1fM", this / 1_000_000.0)
        this >= 1_000 -> String.format(Locale.US, "%.1fk", this / 1_000.0)
        else -> this.toString()
    }
}
