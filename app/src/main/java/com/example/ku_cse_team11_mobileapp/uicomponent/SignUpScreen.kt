package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lint.kotlin.metadata.Visibility
import com.example.ku_cse_team11_mobileapp.model.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onRegistered: () -> Unit
) {
    val vm = remember { SignUpViewModel() }
    val ctx = LocalContext.current

    var email by rememberSaveable { mutableStateOf("") }
    var nickname by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var pwVisible by rememberSaveable { mutableStateOf(false) }
    var cfVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                title = { Text("회원가입") }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("이메일") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            OutlinedTextField(
                value = nickname, onValueChange = { nickname = it },
                label = { Text("닉네임") }, singleLine = true
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("비밀번호 (6자 이상)") }, singleLine = true,
                visualTransformation = if (pwVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { pwVisible = !pwVisible }) {
                        Icon(
                            if (pwVisible) Icons.Outlined.Info else Icons.Outlined.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            )
            OutlinedTextField(
                value = confirm, onValueChange = { confirm = it },
                label = { Text("비밀번호 확인") }, singleLine = true,
                visualTransformation = if (cfVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { cfVisible = !cfVisible }) {
                        Icon(
                            if (cfVisible) Icons.Outlined.Info else Icons.Outlined.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }
            )

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !vm.isLoading,
                onClick = { vm.register(ctx, email, password, confirm, nickname, onRegistered) }
            ) {
                if (vm.isLoading) CircularProgressIndicator(strokeWidth = 2.dp)
                else Text("가입하기")
            }

            vm.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
