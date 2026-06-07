package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.data.repository.FeedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class FeedViewModel(
    private val repository: FeedRepository
) : ViewModel() {

    // Automatically convert Room's Flow into Compose State
    val feed = repository.feed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Open the Firebase pipe the moment the app starts!
        repository.startRealtimeSync()
    }
}