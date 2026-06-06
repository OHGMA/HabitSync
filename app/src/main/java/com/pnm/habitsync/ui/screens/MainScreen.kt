package com.pnm.habitsync.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pnm.habitsync.data.local.AppDatabase
import com.pnm.habitsync.data.local.FeedDao
import com.pnm.habitsync.data.local.ProfileDao
import com.pnm.habitsync.data.repository.FeedRepository
import com.pnm.habitsync.data.repository.ProfileRepository
import com.pnm.habitsync.navigation.Screen
import com.pnm.habitsync.viewmodel.FeedViewModel
import com.pnm.habitsync.viewmodel.ProfileViewModel

@Composable
fun MainScreen(feedDao: FeedDao,
               appDatabase: AppDatabase, // NEW: Needed to clear cache on logout
               profileDao: ProfileDao,
               onLogoutRequest: () -> Unit // NEW: Tells RootNavigation to go to AuthScreen
) { // <- Accept the DAO from MainActivity
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Habits, Screen.Profile)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            // HOME SCREEN WITH CUSTOM VIEWMODEL INJECTION
            composable(Screen.Home.route) {
                // 1. Create the repository
                val repository = remember { FeedRepository(feedDao) }

                // 2. Create the ViewModel using a Factory
                val feedViewModel: FeedViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return FeedViewModel(repository) as T
                        }
                    }
                )

                // 3. Pass it to the UI
                HomeScreen(viewModel = feedViewModel)
            }

            // HABITS SCREEN
            composable(Screen.Habits.route) {
                HabitsScreen(
                    onCreateHabitClick = { navController.navigate(Screen.CreateHabit.route) }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val repo = ProfileRepository(
                                appDatabase = appDatabase,
                                profileDao = profileDao
                            )
                            return ProfileViewModel(repo) as T
                        }
                    }
                )

                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogoutClick = { onLogoutRequest() }
                )
            }

            // CREATE HABIT SCREEN
            composable(Screen.CreateHabit.route) {
                CreateHabitScreen(
                    onBack = { navController.popBackStack() },
                    onHabitCreated = { navController.popBackStack() }
                )
            }
        }
    }
}