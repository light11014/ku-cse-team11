// nav/AppNavHost.kt
package com.example.ku_cse_team11_mobileapp.graph

import SearchScreen
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ku_cse_team11_mobileapp.model.CreateNode
import com.example.ku_cse_team11_mobileapp.model.FavoriteStore
import com.example.ku_cse_team11_mobileapp.model.Tier
import com.example.ku_cse_team11_mobileapp.uicomponent.CommunityScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.DetailScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.MyListScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.NodeList
import com.example.ku_cse_team11_mobileapp.uicomponent.TierListScreen
import kotlinx.coroutines.launch

@Composable
fun NavHost(initialNodes: List<CreateNode>) {
    val nav = rememberNavController()
    val nodes by remember { mutableStateOf(initialNodes) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tierMap = remember { mutableStateMapOf<Long, Tier>() }
    // ★ DataStore에서 즐겨찾기 Set<Long> 구독 (앱 재시작 후에도 복원)
    val favoriteIds by FavoriteStore.favoritesFlow(context)
        .collectAsState(initial = emptySet())

    // ★ 토글 액션 → DataStore 반영
    val onToggleFavorite: (Long) -> Unit = { id ->
        scope.launch { FavoriteStore.toggle(context, id) }
    }
    NavHost(navController = nav, startDestination = Screen.NodeList.route) {

        composable(Screen.NodeList.route) {
            NodeList(
                nodes = nodes,
                favoriteIds = favoriteIds,            // ★ 전달
                onToggleFavorite = onToggleFavorite,
                onNodeClick = { node ->
                    nav.navigate(Screen.Detail.route(node.id))
                },
                onSearchClick = { nav.navigate(Screen.Search.route) },
                onOpenTierList = { nav.navigate(Screen.TierRandom.route) },
                onOpenMyList = { nav.navigate(Screen.MyList.route) }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                nodes = nodes,
                favoriteIds = favoriteIds,
                onToggleFavorite = onToggleFavorite,
                onNodeClick = { node -> nav.navigate(Screen.Detail.route(node.id)) },
                onBack = { nav.navigateUp() }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments!!.getLong("id")
            val node = nodes.firstOrNull { it.id == id }
            if (node == null) {
                // 아주 단순한 에러 처리
                Text("작품을 찾을 수 없습니다.")
            } else {
                DetailScreen(
                    node = node,
                    isFavorite = node.id in favoriteIds,     // ← 반영
                    onToggleFavorite = onToggleFavorite,
                    onGoCommunity = { nid, title ->
                        nav.navigate(Screen.Community.route(nid, title))
                    }
                )
            }
        }

        composable(
            route = Screen.Community.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nodeId = backStackEntry.arguments!!.getLong("id")
            val title = backStackEntry.arguments!!.getString("title")!!
            CommunityScreen(nodeId = nodeId, title = title)
        }

        composable(Screen.TierRandom.route) {
            val novels = nodes.filter { it.type.name.contains("NOVEL", ignoreCase = true) }
            val pool = novels.ifEmpty { nodes }
            val pick = remember { pool.random() }
            LaunchedEffect(pick.id) {
                nav.navigate(Screen.Tier.route(pick.id)) {
                    popUpTo(Screen.NodeList.route) { inclusive = false }
                }
            }
        }

// ★ 특정 노드에 대해 티어 매기기 화면
        composable(
            route = Screen.Tier.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments!!.getLong("id")
            val node = nodes.firstOrNull { it.id == id }
            if (node != null) {
                TierListScreen(
                    node = node,
                    selected = null, // 이미 선택된 티어를 불러오고 싶다면 map에서 꺼내기
                    onBack = { nav.popBackStack() },
                    onConfirm = { tier ->
                        // TODO: 티어 저장 로직 (ex. DataStore or remember map)
                        nav.popBackStack()
                    }
                )
            } else {
                Text("작품을 찾을 수 없습니다.")
            }
        }
        composable(Screen.MyList.route) {
            MyListScreen(
                nodes = nodes,
                favoriteIds = favoriteIds,
                onBack = { nav.navigateUp() },
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}
