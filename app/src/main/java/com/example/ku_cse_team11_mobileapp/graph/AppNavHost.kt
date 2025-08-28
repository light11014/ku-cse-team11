package com.example.ku_cse_team11_mobileapp.graph

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.uicomponent.CommunityScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.ContentDetailScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.LoginScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.MainScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.MyPageScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.SearchScreen
import com.example.ku_cse_team11_mobileapp.uicomponent.SignUpScreen

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val memberId by ServiceLocator.session.memberIdFlow.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(memberId) {
        if (memberId == null) navController.navigate("login") { popUpTo(0) }
        else navController.navigate("home") { popUpTo(0) }
    }

    NavHost(navController, startDestination = "gate") {
        composable("gate") { /* 비움: 위 LaunchedEffect가 라우팅 */ }

        composable("login")  { LoginScreen(navController) }
        composable("signup") { SignUpScreen(navController) }

        composable("home") {
            MainScreen(navController = navController)
        }

        composable(
            route = "search" // 필요하면 쿼리 파라미터도 붙일 수 있음: search?contentType={ct}&platform={pf}
        ) {
            SearchScreen(navController = navController)
        }

        composable(
            route = "content/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStack ->
            val id = backStack.arguments?.getInt("id") ?: return@composable
            ContentDetailScreen(
                contentId = id,
                navController = navController
            )
        }
        composable("mypage") { MyPageScreen(navController) }

        composable(
            route = "community/{contentId}",
            arguments = listOf(navArgument("contentId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("contentId") ?: return@composable
            CommunityScreen(contentId = id, navController = navController)
        }
    }
}