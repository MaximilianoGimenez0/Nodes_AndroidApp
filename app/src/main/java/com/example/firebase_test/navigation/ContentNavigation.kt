package com.example.firebase_test.navigation


import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.firebase_test.MainBottomBar
import com.example.firebase_test.pages.FriendsPage
import com.example.firebase_test.pages.HomePage
import com.example.firebase_test.pages.WorkspacesPage
import com.example.firebase_test.pages.SettingsPage
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.UserViewModel
import com.example.firebase_test.viewmodels.UserViewModelFactory
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import com.example.firebase_test.viewmodels.WorkspacesViewModelFactory
import com.example.firebase_test.workspaces.WorkspaceDetailScreen
import com.example.firebase_test.workspaces.WorkspaceDetailsContent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


@Composable
fun ContentNavigation(
    modifier: Modifier, authViewModel: AuthViewModel, navController: NavController
) {
    val contentNavController = rememberNavController()

    val authState = authViewModel.authState.observeAsState()

    val firestore = Firebase.firestore
    val auth = Firebase.auth

    val userViewmodel: UserViewModel = viewModel(
        factory = UserViewModelFactory()
    )

    val workspacesViewModel: WorkspacesViewModel = viewModel(
        factory = WorkspacesViewModelFactory(firestore, auth)
    )


    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    Scaffold(modifier = modifier, bottomBar = {
        MainBottomBar(contentNavController)
    }, content = { innerPadding ->
        NavHost(navController = contentNavController, startDestination = "home") {
            navigation("chat/{id}", route = "workspace_graph") {
                composable(
                    "chat/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->
                    val workspaceId = backStackEntry.arguments?.getString("id")

                    if (workspaceId != null) {
                        WorkspaceDetailScreen(
                            workspaceId,
                            workspacesViewModel,
                            contentNavController,
                            modifier = Modifier.padding(innerPadding),
                            authViewModel
                        )
                    }

                }

                composable(
                    "info/{id}", arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) { backStackEntry ->

                    val workspaceId = backStackEntry.arguments?.getString("id")

                    if (workspaceId != null) {
                        WorkspaceDetailsContent(
                            workspacesViewModel,
                            modifier = Modifier.padding(innerPadding),
                            contentNavController = contentNavController
                        )
                    }

                }
            }


            composable("home") {
                HomePage(
                    modifier.padding(innerPadding), authViewModel, contentNavController
                )
            }

            composable("friends") { FriendsPage(modifier.padding(innerPadding)) }

            composable("workspaces") {
                WorkspacesPage(
                    modifier.padding(innerPadding), contentNavController, workspacesViewModel
                )
            }

            composable("settings") {
                SettingsPage(
                    modifier.padding(innerPadding),
                    authViewModel,
                    navController,
                    userViewmodel,
                    contentNavController
                )
            }


        }
    })

}


