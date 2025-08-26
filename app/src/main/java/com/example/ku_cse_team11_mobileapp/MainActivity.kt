package com.example.ku_cse_team11_mobileapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ku_cse_team11_mobileapp.graph.NavHost
import com.example.ku_cse_team11_mobileapp.previewProvider.CreateNodeListPreviewProvider
import com.example.ku_cse_team11_mobileapp.ui.theme.Kucseteam11mobileappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Kucseteam11mobileappTheme {
                NavHost(initialNodes = CreateNodeListPreviewProvider().values.first())
            }
        }
    }
}
