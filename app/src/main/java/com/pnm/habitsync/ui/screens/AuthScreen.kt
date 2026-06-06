package com.pnm.habitsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pnm.habitsync.viewmodel.AuthState
import com.pnm.habitsync.viewmodel.AuthViewModel
import androidx.compose.foundation.clickable

@Composable
fun AuthScreen(
    // We pass the ViewModel here. viewModel() automatically creates it or grabs the existing one.
    viewModel: AuthViewModel = viewModel(),
    // This function will be triggered when login is completely successful
    onAuthSuccess: () -> Unit
) {
    // 1. STATE OBSERVATION
    // This is the magic of MVVM. The UI "collects" the state. If state changes, the UI redraws!
    val authState by viewModel.authState.collectAsState()

    // Local UI state to toggle between Login and Sign Up mode
    var isLoginMode by remember { mutableStateOf(true) }

    // Local state for the text fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") } // Only used for Sign Up

    // 2. SIDE EFFECTS
    // LaunchedEffect runs safely outside the normal UI drawing loop.
    // We use it here to navigate away ONLY when the state becomes Authenticated.
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            onAuthSuccess()
        }
    }

    // 3. THE UI LAYOUT
    // Matching your light blue background from the design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F0F8)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo Placeholder
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF10B981)), // Green brand color
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(50.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("HabitSync", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Build better habits together", color = Color.Gray)

            Spacer(modifier = Modifier.height(32.dp))

            // The White Card containing the form
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {

                    // Toggle Switch (Login | Sign Up)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        TabButton(
                            text = "Login",
                            isSelected = isLoginMode,
                            modifier = Modifier.weight(1f)
                        ) { isLoginMode = true }

                        TabButton(
                            text = "Sign Up",
                            isSelected = !isLoginMode,
                            modifier = Modifier.weight(1f)
                        ) { isLoginMode = false }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Username Field (Only show if in Sign Up mode)
                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("your@email.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Enter your password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Show error message if there is one
                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // The Action Button
                    Button(
                        onClick = {
                            if (isLoginMode) viewModel.login(email, password)
                            else viewModel.register(email, password, username)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Green
                        shape = RoundedCornerShape(12.dp),
                        enabled = authState !is AuthState.Loading // Disable button while loading
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(if (isLoginMode) "Login" else "Sign Up", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// A small helper component for the Toggle tabs
@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    val textColor = if (isSelected) Color.Black else Color.Gray

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(vertical = 12.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.SemiBold)
    }
}