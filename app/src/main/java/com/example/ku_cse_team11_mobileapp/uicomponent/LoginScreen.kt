package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    vm: LoginViewModel = viewModel(factory = LoginViewModel.Factory(ServiceLocator.authRepo))
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("로그인") }) }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Union",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold
            )
            OutlinedTextField(
                value = ui.loginId, onValueChange = vm::updateLoginId,
                label = { Text("아이디") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ui.password, onValueChange = vm::updatePassword,
                label = { Text("비밀번호") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (ui.error != null) Text(ui.error!!, color = MaterialTheme.colorScheme.error)
            if (ui.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            Button(
                onClick = { vm.submit() },
                enabled = !ui.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text("로그인") }

            TextButton(onClick = { navController.navigate("signup") }) { Text("회원가입으로 이동") }

            if (ui.message == "LOGIN_OK") {
                // 로그인 성공 → 홈으로
                LaunchedEffect(ui.message) { navController.navigate("home") { popUpTo("login") { inclusive = true } } }
            }
        }
    }
}