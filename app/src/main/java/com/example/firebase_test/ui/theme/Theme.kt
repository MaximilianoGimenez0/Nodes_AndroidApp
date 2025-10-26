package com.example.firebase_test.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


// --- MODO OSCURO ---
val DarkColorScheme = darkColorScheme(
    primary = ElegantViolet80,
    onPrimary = ElegantViolet20,
    primaryContainer = ElegantViolet30,
    onPrimaryContainer = ElegantViolet90,

    secondary = ElegantBlue80,
    onSecondary = ElegantBlue20,
    secondaryContainer = ElegantBlue30,
    onSecondaryContainer = ElegantBlue90,

    tertiary = ElegantSky80,
    onTertiary = ElegantSky30,
    tertiaryContainer = ElegantSky40,
    onTertiaryContainer = ElegantSky90,

    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,

    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)


// --- MODO CLARO ---
val LightColorScheme = lightColorScheme(
    primary = ElegantViolet40,
    onPrimary = Color.White,
    primaryContainer = ElegantViolet90,
    onPrimaryContainer = ElegantViolet10,

    secondary = ElegantBlue40,
    onSecondary = Color.White,
    secondaryContainer = ElegantBlue90,
    onSecondaryContainer = ElegantBlue10,

    tertiary = ElegantSky40,
    onTertiary = Color.White,
    tertiaryContainer = ElegantSky90,
    onTertiaryContainer = ElegantSky10,

    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,

    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,

    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)


@Composable
fun FireBase_TestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Mantenemos la opción de Dynamic Color
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Usamos el color de fondo para la barra de estado para una apariencia inmersiva
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Asume que tienes un archivo Typography.kt
        content = content
    )
}