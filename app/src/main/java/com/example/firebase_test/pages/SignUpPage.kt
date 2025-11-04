package com.example.firebase_test.pages

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource // <-- Importante
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.firebase_test.CharacterLimitedOutlinedTextField
import com.example.firebase_test.R // <-- Importante
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
    var isPasswordHidden by remember { mutableStateOf(true) }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(stringResource(id = R.string.signup_title), fontSize = 32.sp)

            Spacer(modifier = Modifier.height(16.dp))

            CharacterLimitedOutlinedTextField(
                value = email,
                onValueChange = { email = it },
                maxLength = 40,
                label = { Text(stringResource(id = R.string.common_email)) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            CharacterLimitedOutlinedTextField(
                value = password,
                onValueChange = { password = it },
                maxLength = 30,
                label = { Text(stringResource(id = R.string.common_password)) },
                singleLine = true,
                trailingIcon = {

                    val icon = if (isPasswordHidden) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    }

                    val description = if (isPasswordHidden) {
                        stringResource(id = R.string.common_show_password)
                    } else {
                        stringResource(id = R.string.common_hide_password)
                    }

                    IconButton(onClick = { isPasswordHidden = !isPasswordHidden }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                },
                visualTransformation = if (isPasswordHidden) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(8.dp))

            CharacterLimitedOutlinedTextField(
                value = firstname,
                onValueChange = { firstname = it },
                maxLength = 40,
                label = { Text(stringResource(id = R.string.common_firstname)) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(8.dp))

            CharacterLimitedOutlinedTextField(
                value = lastname,
                onValueChange = { lastname = it },
                maxLength = 40,
                label = { Text(stringResource(id = R.string.common_lastname)) },
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                authViewmodel.signUp(email, password, firstname, lastname)
            }, enabled = authState.value != AuthState.Loading) {
                Text(stringResource(id = R.string.signup_button_signup))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = {
                navController.navigate("login")
            }) {
                Text(stringResource(id = R.string.signup_navigate_to_login_link))
            }
        }
    })
}