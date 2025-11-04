package com.example.firebase_test.navigation


import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
// --- IMPORTA TU NUEVA PANTALLA ---
import com.example.firebase_test.pages.HomePage
import com.example.firebase_test.pages.WorkspacesPage
import com.example.firebase_test.pages.SettingsPage
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.SettingsViewModel
import com.example.firebase_test.viewmodels.UserViewModel
import com.example.firebase_test.viewmodels.UserViewModelFactory
import com.example.firebase_test.viewmodels.WorkspacesViewModel
import com.example.firebase_test.viewmodels.WorkspacesViewModelFactory
import com.example.firebase_test.workspaces.WorkspaceDetailScreen
import com.example.firebase_test.workspaces.WorkspaceDetailsContent
import com.example.firebase_test.workspaces.entries.MapScreen

@Composable
fun ContentNavigation(
    modifier: Modifier,
    authViewModel: AuthViewModel,
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val contentNavController = rememberNavController()

    val authState = authViewModel.authState.observeAsState()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    val factory = WorkspacesViewModelFactory(application)
    val workspacesViewModel: WorkspacesViewModel = viewModel(factory = factory)

    val userViewmodel: UserViewModel = viewModel(
        factory = UserViewModelFactory(
            application
        )
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
        NavHost(
            navController = contentNavController,
            startDestination = "home",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
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
                            modifier = Modifier.fillMaxSize(),
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
                            modifier = Modifier.fillMaxSize(),
                            contentNavController = contentNavController
                        )
                    }

                }
            }

            composable("home") {
                HomePage(
                    Modifier.fillMaxSize(), authViewModel, navController, contentNavController
                )
            }


            composable("workspaces") {
                WorkspacesPage(
                    Modifier.fillMaxSize(), contentNavController, workspacesViewModel
                )
            }

            composable("settings") {
                SettingsPage(
                    Modifier.fillMaxSize(),
                    authViewModel,
                    settingsViewModel,
                    navController,
                    userViewmodel,
                    contentNavController
                )
            }


            composable(
                route = "map/{lat}/{lng}",
                arguments = listOf(
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lng") { type = NavType.StringType })
            ) { backStackEntry ->

                val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull() ?: 0.0
                val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull() ?: 0.0


                MapScreen(latitude = lat, longitude = lng, contentNavController)
            }

        }
    })

}