package com.example.firebase_test

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Routes(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelResId: Int,
    @StringRes val contentDescResId: Int
) {
    Home(
        route = "home",
        icon = Icons.Default.Home,
        labelResId = R.string.bottom_nav_home,
        contentDescResId = R.string.bottom_nav_home_desc
    ),
    Workspaces(
        route = "workspaces",
        icon = Icons.AutoMirrored.Filled.List,
        labelResId = R.string.bottom_nav_workspaces,
        contentDescResId = R.string.bottom_nav_workspaces_desc
    ),
    Profile(
        route = "settings",
        icon = Icons.Default.Settings,
        labelResId = R.string.bottom_nav_profile,
        contentDescResId = R.string.bottom_nav_profile_desc
    )
}