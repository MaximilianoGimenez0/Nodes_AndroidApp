package com.example.firebase_test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.firebase_test.navigation.AppNavigation
import com.example.firebase_test.viewmodels.AuthViewModel
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.firebase_test.ui.theme.DarkColorScheme
import com.example.firebase_test.ui.theme.LightColorScheme

class MainActivity : ComponentActivity() {

    val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val darkTheme: Boolean = isSystemInDarkTheme()

            val colors = if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }

            MaterialTheme(
                colorScheme = colors
            ) {
                AppNavigation(modifier = Modifier, authViewModel = authViewModel)
            }
        }
    }
}

