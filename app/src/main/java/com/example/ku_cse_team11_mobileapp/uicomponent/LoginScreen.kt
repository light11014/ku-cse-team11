package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ku_cse_team11_mobileapp.model.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateSignUp: () -> Unit
) {
    val vm = remember { LoginViewModel() }
    val ctx = LocalContext.current
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("로그인") }) }) { inner ->
        Column(
            Modifier.padding(inner).padding(16.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(value = email, onValueChange = { email = it },
                label = { Text("이메일") }, singleLine = true)
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("비밀번호") }, singleLine = true, visualTransformation = PasswordVisualTransformation()
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.isLoading && email.isNotBlank() && password.isNotBlank(),
                onClick = { vm.login(ctx, email, password, onLoggedIn) }
            ) { if (vm.isLoading) CircularProgressIndicator(strokeWidth = 2.dp) else Text("로그인") }

            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* nav to SignUp */ onNavigateSignUp() }
            ) { Text("계정이 없으신가요? 회원가입") }

        }
    }
}