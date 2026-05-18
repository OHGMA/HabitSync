package com.pnm.habitsync.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pnm.habitsync.utils.Resource
import com.pnm.habitsync.viewmodel.AuthViewModel

enum class AuthPage {
    LOGIN, REGISTER
}

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var currentPage by remember { mutableStateOf(AuthPage.LOGIN) }
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        if (authState is Resource.Success) {
            onAuthSuccess()
            viewModel.clearState()
        }
    }

    AuthContent(
        currentPage = currentPage,
        authState = authState,
        onPageToggle = {
            currentPage = it
            viewModel.clearState()
        },
        onLoginClick = { email, password ->
            viewModel.login(email, password)
        },
        onRegisterClick = { username, email, password ->
            viewModel.register(username, email, password)
        }
    )
}

@Composable
fun AuthContent(
    currentPage: AuthPage,
    authState: Resource<*>? = null,
    onPageToggle: (AuthPage) -> Unit,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEBF5FF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Logo Section
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF10B981))
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HabitSync",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = if (currentPage == AuthPage.LOGIN) "Build better habits together" else "Join the community",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                            .padding(4.dp)
                    ) {
                        TabButton(
                            text = "Login",
                            isSelected = currentPage == AuthPage.LOGIN,
                            onClick = { onPageToggle(AuthPage.LOGIN) },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            text = "Sign Up",
                            isSelected = currentPage == AuthPage.REGISTER,
                            onClick = { onPageToggle(AuthPage.REGISTER) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (currentPage == AuthPage.REGISTER) {
                        AuthTextField(
                            value = username,
                            onValueChange = { username = it },
                            placeholder = "Username"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = "your@email.com"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Enter your password",
                        isPassword = true
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Button
                    Button(
                        onClick = {
                            if (currentPage == AuthPage.LOGIN) {
                                onLoginClick(email, password)
                            } else {
                                onRegisterClick(username, email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        enabled = authState !is Resource.Loading
                    ) {
                        if (authState is Resource.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = if (currentPage == AuthPage.LOGIN) "Login" else "Register",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Error Message
                    if (authState is Resource.Error) {
                        Text(
                            text = authState.message ?: "An error occurred",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = modifier,
            contentPadding = PaddingValues(0.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text(text, color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    } else {
        TextButton(
            onClick = onClick,
            modifier = modifier
        ) {
            Text(text, color = Color.Gray)
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            unfocusedContainerColor = Color(0xFFF3F4F6),
            focusedContainerColor = Color(0xFFF3F4F6),
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AuthScreenPreview() {
    HabitSyncTheme {
        AuthContent(
            currentPage = AuthPage.LOGIN,
            onPageToggle = {},
            onLoginClick = { _, _ -> },
            onRegisterClick = { _, _, _ -> }
        )
    }
}
