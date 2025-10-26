package com.example.firebase_test.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebase_test.pages.LoginPage
import com.example.firebase_test.pages.SignUpPage
import com.example.firebase_test.viewmodels.AuthViewModel

@Composable
fun AppNavigation(modifier: Modifier, authViewModel: AuthViewModel) {

    val navController = rememberNavController()


    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginPage(modifier, authViewModel, navController)
        }

        composable("signup") {
            SignUpPage(modifier, authViewModel, navController)
        }

        composable("home") {
            ContentNavigation(modifier, authViewModel, navController)
        }
    }
}