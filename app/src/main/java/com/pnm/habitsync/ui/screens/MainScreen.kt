package com.pnm.habitsync.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pnm.habitsync.navigation.Screen

@Composable
fun MainScreen() {
    // This controller remembers where the user is and handles the back button
    val navController = rememberNavController()

    // Put our screens in a list so we can loop through them for the bottom bar
    val items = listOf(Screen.Home, Screen.Habits, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Get the current route so we know which tab to highlight
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                // Loop through our screens and create a NavigationBarItem for each
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        // Check if the current destination matches this tab
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination to avoid a massive backstack
                                // (If you click Home -> Profile -> Home -> Profile, pressing back shouldn't take 4 clicks)
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid opening the same screen twice
                                launchSingleTop = true
                                // Restore state when re-selecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // NavHost is the container that swaps out our screens based on the route
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding) // Important: Prevents content from going under the bottom bar
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Habits.route) { HabitsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}