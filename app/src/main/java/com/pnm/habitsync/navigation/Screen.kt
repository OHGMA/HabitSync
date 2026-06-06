package com.pnm.habitsync.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// A sealed class restricts what our routes can be.
// We also attach the title and icon here so the Bottom Navigation can easily read them.
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Habits : Screen("habits", "Habits", Icons.Default.List)
    object CreateHabit : Screen("create_habit", "Create Habit", Icons.Default.Add)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}