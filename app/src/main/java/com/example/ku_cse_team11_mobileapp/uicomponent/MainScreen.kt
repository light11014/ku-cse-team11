package com.example.ku_cse_team11_mobileapp.uicomponent


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController
) {
    Scaffold(
        // 시스템 인셋으로 가로/아래쪽까지 여백 생기는 것 방지
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text("Union") },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Filled.Search, contentDescription = "검색")
                    }
                    IconButton(onClick = { navController.navigate("mypage") }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "마이페이지")
                    }
                }
            )
        }
    ) { inner ->
        // TopAppBar 높이만 패딩 적용 (좌/우/하 없음)
        ContentNodeList(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = inner.calculateTopPadding())
        )
    }
}
