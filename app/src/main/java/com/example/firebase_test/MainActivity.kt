package com.example.firebase_test

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.firebase_test.navigation.AppNavigation
import com.example.firebase_test.ui.theme.DarkColorScheme
import com.example.firebase_test.ui.theme.LightColorScheme
import com.example.firebase_test.viewmodels.AuthViewModel
import com.example.firebase_test.viewmodels.AuthViewModelFactory
import com.example.firebase_test.viewmodels.SettingsViewModel
import com.example.firebase_test.viewmodels.SettingsViewModelFactory
import com.example.firebase_test.workspaces.MyApplication

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val context = LocalContext.current

            val application = context.applicationContext as Application

            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModelFactory(
                    application = application
                )
            )

            val settingsManager = (context.applicationContext as MyApplication).settingsManager
            val settingsViewModelFactory = remember(settingsManager) {
                SettingsViewModelFactory(settingsManager)
            }
            val settingsViewModel: SettingsViewModel = viewModel(factory = settingsViewModelFactory)

            val settings by settingsViewModel.appSettings.collectAsState()

            LaunchedEffect(settings.language) {
                LocaleHelper.setLocale(settings.language)
            }

            val systemIsDark = isSystemInDarkTheme()
            val darkTheme: Boolean = when (settings.theme) {
                "Claro" -> false
                "Oscuro" -> true
                else -> systemIsDark
            }

            val colors = if (darkTheme) {
                DarkColorScheme
            } else {
                LightColorScheme
            }

            val baseDensity = LocalDensity.current

            val newDensity = remember(settings.fontSize, baseDensity) {
                val fontScale = when (settings.fontSize) {
                    0 -> 0.85f // Pequeña
                    1 -> 1.0f  // Mediana (Default)
                    2 -> 1.15f // Grande
                    else -> 1.0f
                }

                Density(
                    density = baseDensity.density, fontScale = fontScale
                )
            }

            MaterialTheme(
                colorScheme = colors
            ) {
                CompositionLocalProvider(LocalDensity provides newDensity) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavigation(
                            modifier = Modifier.fillMaxSize(),
                            authViewModel = authViewModel,
                            settingsViewModel = settingsViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
                if (isGranted) {
                    Log.d("Permissions", "Permiso de notificación CONCEDIDO")
                } else {
                    Log.w("Permissions", "Permiso de notificación DENEGADO")
                }
            })

        LaunchedEffect(key1 = Unit) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

object LocaleHelper {

    fun setLocale(languageSetting: String) {
        val languageTag = when (languageSetting) {
            "Inglés" -> "en"
            "Portugués" -> "pt"
            else -> "es" // Español por defecto
        }

        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageTag)

        if (AppCompatDelegate.getApplicationLocales() != appLocale) {
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}