package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
    val favIds by repo.favoriteIdsFlow.collectAsState(initial = emptySet()) // 서버/스토어 상태
    val serverFav = favIds.contains(contentId.toLong())

    // ▶️ 낙관적 UI 오버라이드 (null 이면 서버/스토어 상태 사용)
    var override by rememberSaveable(contentId) { mutableStateOf<Boolean?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val isFav = override ?: serverFav

    IconButton(
        onClick = {
            if (loading) return@IconButton
            val before = isFav
            val after = !before

            // 1) 즉시 UI 반영
            override = after
            loading = true

            // 2) 서버 토글 시도
            scope.launch {
                runCatching { repo.toggleFavorite(contentId.toLong()) }
                    .onSuccess {
                        // 성공: store 가 갱신되므로 오버라이드 제거해 서버상태를 따르게
                        override = null
                    }
                    .onFailure { e ->
                        // 실패: UI 롤백
                        override = before
                        onError(e.message ?: "즐겨찾기 처리 실패")
                    }
                loading = false
            }
        },
        modifier = modifier
            .size(28.dp) // 터치 영역 확보
            .zIndex(1f)  // 카드 클릭보다 위에
    ) {
        when {
            loading -> CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            isFav   -> Icon(Icons.Filled.Star,        contentDescription = "즐겨찾기 해제")
            else    -> Icon(Icons.Outlined.StarBorder,contentDescription = "즐겨찾기 추가")
        }
    }
}