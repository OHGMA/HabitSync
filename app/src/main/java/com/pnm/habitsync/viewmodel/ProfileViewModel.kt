package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.data.model.UserProfile
import com.pnm.habitsync.data.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    // Read from Room, and provide a default "Loading" profile if Room is empty
    val userProfile = repository.localProfile
        .map { it ?: UserProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    init {
        // Start Firebase sync when the ViewModel is created
        repository.startRealtimeSync()
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.logout()
            withContext(Dispatchers.Main) {
                onLogoutComplete()
            }
        }
    }
}