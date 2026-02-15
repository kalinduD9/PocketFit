package com.kalindu.pocketfit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kalindu.pocketfit.ui.navigation.AppNavigation
import com.kalindu.pocketfit.ui.theme.PocketFitTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PocketFitTheme {
                AppNavigation()
            }
        }
    }
}

