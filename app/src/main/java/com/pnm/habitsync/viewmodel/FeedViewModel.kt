package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.data.repository.FeedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedViewModel(
    private val repository: FeedRepository
) : ViewModel() {

    // 1. Observe the local Room database
    // stateIn() converts the raw Room Flow into a StateFlow that Compose loves.
    val feed = repository.feed.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // 2. The moment this screen opens, tell Firebase to sync new data into Room!
        syncWithFirebase()
    }

    // NEW: A public function the UI can call every time the tab is opened
    fun refreshFeed() {
        syncWithFirebase()
    }

    private fun syncWithFirebase() {
        viewModelScope.launch {
            repository.syncFeed()
        }
    }
}