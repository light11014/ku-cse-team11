package com.example.ku_cse_team11_mobileapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import com.example.ku_cse_team11_mobileapp.api.model.ServiceLocator
import com.example.ku_cse_team11_mobileapp.graph.AppNavHost
import com.example.ku_cse_team11_mobileapp.ui.theme.Kucseteam11mobileappTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ServiceLocator.init(application)
        enableEdgeToEdge()
        setContent {
            Kucseteam11mobileappTheme {
                AppNavHost()
            }
        }
    }
}
