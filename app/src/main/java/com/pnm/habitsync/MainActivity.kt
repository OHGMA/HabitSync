package com.pnm.habitsync // Adjust this!

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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
            // This is the file we made in Phase 2 with the Bottom Navigation!
            MainScreen(feedDao = feedDao)
        }
    }
}