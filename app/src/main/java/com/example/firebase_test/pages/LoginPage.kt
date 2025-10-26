package com.example.firebase_test.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel

@Composable
fun LoginPage(modifier: Modifier, authViewmodel: AuthViewModel, navController: NavController) {

    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    val context = LocalContext.current
    val authState = authViewmodel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Authenticated -> navController.navigate("home")
            is AuthState.Error -> Toast.makeText(
                context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT
            ).show()

            else -> Unit
        }
    }

    Scaffold(modifier = modifier, content = { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Iniciar Sesión", fontSize = 32.sp)

            Spacer(modifier = modifier.height(16.dp))

            OutlinedTextField(value = email, onValueChange = {
                email = it
            }, label = {
                Text("Email")
            })

            Spacer(modifier = modifier.height(8.dp))

            OutlinedTextField(value = password, onValueChange = {
                password = it
            }, label = {
                Text("Contraseña")
            })

            Spacer(modifier = modifier.height(16.dp))

            Button(
                onClick = {
                    authViewmodel.login(email, password)
                }, enabled = authState.value != AuthState.Loading
            ) {
                Text("Login")
            }

            Spacer(modifier = modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("signup")
            }) {
                Text("No tengo una cuenta, registrarme")
            }
        }
    })


}