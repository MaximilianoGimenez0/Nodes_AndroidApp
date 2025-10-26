package com.example.firebase_test

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController

@Composable
fun MainBottomBar(navController: NavController) {

    val startDestination = Routes.Home
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        Routes.entries.forEachIndexed { index, destination ->
            NavigationBarItem(selected = selectedDestination == index, onClick = {
                navController.navigate(route = destination.route)
                selectedDestination = index
            }, icon = {
                Icon(
                    destination.icon, contentDescription = destination.contentDescription
                )
            }, label = { Text(destination.label) })
        }

    }

}
