package com.example.ku_cse_team11_mobileapp.graph

import android.net.Uri

sealed class Screen(val route: String) {
    data object NodeList : Screen("node_list")
    data object Detail : Screen("detail/{id}") {
        fun route(id: Long) = "detail/$id"
    }
    data object Community : Screen("community/{id}/{title}") {
        fun route(id: Long, title: String) = "community/$id/${Uri.encode(title)}"
    }
    data object Search : Screen("search")

    data object TierRandom : Screen("tier_random")
    data object Tier : Screen("tier/{id}") {
        fun route(id: Long) = "tier/$id"
    }

    data object MyList : Screen("my_list") // ★ 추가
}
