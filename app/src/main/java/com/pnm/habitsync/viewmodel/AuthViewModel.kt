package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow<Resource<Any>?>(null)
    val authState: StateFlow<Resource<Any>?> = _authState

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            // Simulating a network call
            delay(2000)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                _authState.value = Resource.Success("Success")
            } else {
                _authState.value = Resource.Error("All fields must be filled")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.Loading
            delay(2000)
            if (email.isNotEmpty() && password.isNotEmpty()) {
                _authState.value = Resource.Success("Success")
            } else {
                _authState.value = Resource.Error("Email and Password cannot be empty")
            }
        }
    }

    fun clearState() {
        _authState.value = null
    }
}
