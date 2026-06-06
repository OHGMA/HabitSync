package com.pnm.habitsync // Adjust this!

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pnm.habitsync.data.local.AppDatabase
import com.pnm.habitsync.ui.screens.AuthScreen
import com.pnm.habitsync.ui.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RootNavigation()
        }
    }
}

@Composable
fun RootNavigation() {
    val rootNavController = rememberNavController()

    // Create the Room Database instance here!
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    val feedDao = database.feedDao()
    val profileDao = database.profileDao()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    println("Notification permission granted!")
                } else {
                    println("Notification permission denied!")
                }
            }
        )

        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // This NavHost sits above everything else in the app.
    NavHost(navController = rootNavController, startDestination = "auth") {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    // When login succeeds, go to "main" and destroy the "auth" screen
                    // so the user can't press back to go to the login screen.
                    rootNavController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                feedDao = feedDao,
                profileDao = profileDao,
                appDatabase = database, // Pass the database instance
                onLogoutRequest = {
                    // Navigate back to the Auth screen and destroy the Main screen history
                    rootNavController.navigate("auth") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}