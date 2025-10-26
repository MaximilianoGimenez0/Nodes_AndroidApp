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
fun SignUpPage(modifier: Modifier, authViewmodel: AuthViewModel, navController: NavController) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var firstname by remember {
        mutableStateOf("")
    }

    var lastname by remember {
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
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Registrarse", fontSize = 32.sp)

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

            Spacer(modifier = modifier.height(8.dp))

            OutlinedTextField(value = firstname, onValueChange = {
                firstname = it
            }, label = {
                Text("Nombre")
            })

            Spacer(modifier = modifier.height(8.dp))

            OutlinedTextField(value = lastname, onValueChange = {
                lastname = it
            }, label = {
                Text("Apellido")
            })

            Spacer(modifier = modifier.height(16.dp))

            Button(onClick = {
                authViewmodel.signUp(email, password, firstname, lastname)
            }, enabled = authState.value != AuthState.Loading) {
                Text("SignUp")
            }

            Spacer(modifier = modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("login")
            }) {
                Text("Ya tengo una cuenta, iniciar sesión")
            }
        }
    })


}