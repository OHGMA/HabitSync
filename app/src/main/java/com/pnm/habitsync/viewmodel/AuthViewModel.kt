package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.data.model.User
import com.pnm.habitsync.data.repository.AuthRepository
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Define the possible states our Authentication screen can be in
sealed class AuthState {
    object Idle : AuthState() // Waiting for the user to click something
    object Loading : AuthState() // Showing a loading spinner
    data class Authenticated(val user: User) : AuthState() // Success! Navigate to Home.
    data class Error(val message: String) : AuthState() // Show a red error message
}

class AuthViewModel(
    // We pass the repository in. (For a production app we'd use Dependency Injection like Hilt,
    // but for this 5-week project, default parameters are perfectly fine!)
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    // 2. State Management setup
    // _authState is private and mutable. Only the ViewModel can change it.
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    // authState is public and read-only. The UI will "collect" (observe) this.
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // When the app starts, check if Firebase remembers the user from last time
        checkIfUserIsLoggedIn()
    }

    private fun checkIfUserIsLoggedIn() {
        val currentUser = repository.currentUser
        if (currentUser != null) {
            // Skip the login screen and go straight to Authenticated!
            _authState.value = AuthState.Authenticated(
                User(uid = currentUser.uid, email = currentUser.email ?: "")
            )
        }
    }

    // 3. The function called by the Login Button
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        // Tell the UI to show a loading spinner
        _authState.value = AuthState.Loading

        // viewModelScope.launch runs this in the background so the app doesn't freeze
        viewModelScope.launch {
            val result = repository.login(email, password)
            // Update the state based on what the Repository replies with
            _authState.value = when (result) {
                is Resource.Success -> AuthState.Authenticated(result.data)
                is Resource.Error -> AuthState.Error(result.message)
                is Resource.Loading -> AuthState.Loading // Usually handled before, but safe to include
            }
        }
    }

    // 4. The function called by the Register Button
    fun register(email: String, password: String, username: String) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _authState.value = AuthState.Error("Please fill out all fields")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            val result = repository.register(email, password, username)
            _authState.value = when (result) {
                is Resource.Success -> AuthState.Authenticated(result.data)
                is Resource.Error -> AuthState.Error(result.message)
                is Resource.Loading -> AuthState.Loading
            }
        }
    }
}