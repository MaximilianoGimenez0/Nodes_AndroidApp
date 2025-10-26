package com.example.firebase_test.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel


@Composable
fun SettingsPage(modifier: Modifier, authViewmodel: AuthViewModel, navController: NavController) {
    val authState = authViewmodel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings")

        Spacer(modifier = modifier.height(16.dp))

        Button(onClick = {
            authViewmodel.signOut()
            navController.navigate("login")
        }) { Text("Logout") }
    }
}