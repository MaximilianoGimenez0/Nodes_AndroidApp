package com.example.firebase_test.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase_test.pages.LoginPage
import com.example.firebase_test.pages.SignUpPage
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.SettingsViewModel

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController, startDestination = "login", modifier = modifier.fillMaxSize()
    ) {
        composable("login") {
            LoginPage(Modifier.fillMaxSize(), authViewModel, navController)
        }

        composable("signup") {
            SignUpPage(Modifier.fillMaxSize(), authViewModel, navController)
        }

        composable("home") {
            ContentNavigation(Modifier.fillMaxSize(), authViewModel, settingsViewModel, navController)
        }
    }
}
