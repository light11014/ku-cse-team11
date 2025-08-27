package com.example.ku_cse_team11_mobileapp.uicomponent

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.model.viewmodel.SignUpViewModel

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavHostController,
    vm: SignUpViewModel = viewModel(factory = SignUpViewModel.Factory(ServiceLocator.authRepo))
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("회원가입") }) }) { inner ->
        Column(Modifier.padding(inner).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = ui.loginId, onValueChange = vm::updateLoginId,
                label = { Text("아이디") }, singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ui.name, onValueChange = vm::updateName,
                label = { Text("이름") }, singleLine = true, modifier = Modifier.fillMaxWidth()
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
            ) { Text("회원가입") }

            TextButton(onClick = { navController.navigateUp() }) { Text("뒤로가기") }

            if (ui.message == "SIGNUP_OK") {
                // 가입 성공 → 로그인 화면으로
                LaunchedEffect(ui.message) { navController.navigate("login") { popUpTo("signup") { inclusive = true } } }
            }
        }
    }
}