package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import kotlinx.coroutines.launch

@Composable
fun FavoriteStar(
    contentId: Int,
    modifier: Modifier = Modifier,
    onError: (String) -> Unit = {}
) {
    val repo = ServiceLocator.favoritesRepo
    val favIds by repo.favoriteIdsFlow.collectAsState(initial = emptySet())
    val serverFav = favIds.contains(contentId.toLong())

    var override by rememberSaveable(contentId) { mutableStateOf<Boolean?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isFav = override ?: serverFav

    // 상태별 아이콘 색
    val starColor by animateColorAsState(
        targetValue = if (isFav) Color(0xFFFFC107)  // Amber 500
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "starColor"
    )

    IconButton(
        onClick = {
            if (loading) return@IconButton
            val before = isFav
            val after = !before
            override = after
            loading = true
            scope.launch {
                runCatching { repo.toggleFavorite(contentId.toLong()) }
                    .onSuccess { override = null }
                    .onFailure {
                        override = before
                        onError(it.message ?: "즐겨찾기 처리 실패")
                    }
                loading = false
            }
        },
        modifier = modifier
            .size(28.dp)
            .zIndex(1f)
    ) {
        when {
            loading -> CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            isFav   -> Icon(Icons.Filled.Star, contentDescription = "즐겨찾기 해제", tint = starColor)
            else    -> Icon(Icons.Outlined.StarBorder, contentDescription = "즐겨찾기 추가", tint = starColor)
        }
    }
}
