package com.example.firebase_test

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Routes(
    val route: String, val label: String, val icon: ImageVector, val contentDescription: String
) {
    Home("home", "Inicio", Icons.Outlined.Home, "Inicio"), Sessions(
        "workspaces", "Espacios", Icons.Outlined.Schedule, "Sesiones activas"
    ),
    Settings("settings", "Configuración", Icons.Outlined.Settings, "Configuración de la app"),
}
