package com.example.firebase_test.pages

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.firebase_test.viewmodels.AuthState
import com.example.firebase_test.viewmodels.AuthViewModel
import androidx.compose.runtime.collectAsState
import com.example.firebase_test.RequestNotificationPermission
// --- IMPORTS AÑADIDOS ---
import androidx.compose.ui.res.stringResource
import com.example.firebase_test.R


@Composable
fun HomePage(
    modifier: Modifier,
    authViewmodel: AuthViewModel,
    navController: NavController,
    contentNavController: NavController
) {

    val authState = authViewmodel.authState.observeAsState()
    val currentUser = authViewmodel.userProfile.collectAsState()
    val context = LocalContext.current
    val appVersion = remember { getAppVersion(context) }

    RequestNotificationPermission()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.UnAuthenticated -> {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }

            else -> Unit
        }
    }

    if (authState.value is AuthState.Authenticated) {

        val userName = currentUser.value?.firstName?.split(" ")?.firstOrNull() ?: "Usuario"

        Greetings(
            modifier = modifier,
            userName = userName,
            appVersion = appVersion,
            navController = navController
        )

    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greetings(
    modifier: Modifier, userName: String, appVersion: String, navController: NavController
) {
    val inspirationTips = listOf(
        Triple(
            Icons.Filled.Lightbulb,
            stringResource(R.string.home_tip_brainstorm_title),
            stringResource(R.string.home_tip_brainstorm_desc)
        ), Triple(
            Icons.Filled.PhotoCamera,
            stringResource(R.string.home_tip_media_title),
            stringResource(R.string.home_tip_media_desc)
        ), Triple(
            Icons.Filled.NotificationsActive,
            stringResource(R.string.home_tip_reminders_title),
            stringResource(R.string.home_tip_reminders_desc)
        ), Triple(
            Icons.AutoMirrored.Filled.Notes,
            stringResource(R.string.home_tip_private_title),
            stringResource(R.string.home_tip_private_desc)
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title), fontWeight = FontWeight.Bold) })
        }) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = stringResource(R.string.home_welcome_icon_desc),
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        stringResource(R.string.home_welcome_message, userName),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.home_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            item {
                Text(
                    stringResource(R.string.home_tips_header),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, start = 8.dp)
                )
            }

            items(inspirationTips) { (icon, title, description) ->
                InspirationCard(
                    icon = icon, title = title, description = description
                )
            }

            item {
                AppVersionFooter(appVersion = appVersion, modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}

@Composable
fun InspirationCard(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon, contentDescription = null,
                modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AppVersionFooter(appVersion: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_footer_version, appVersion),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun getAppVersion(context: Context): String {
    return try {
        val packageInfo =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION") context.packageManager.getPackageInfo(
                    context.packageName, 0
                )
            }
        "v${packageInfo.versionName}"
    } catch (e: Exception) {
        context.getString(R.string.default_version)
    }
}