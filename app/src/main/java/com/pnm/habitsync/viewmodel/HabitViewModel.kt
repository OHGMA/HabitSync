package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.data.model.Habit
import com.pnm.habitsync.data.repository.HabitRepository
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    // Automatically convert Room's Flow into Compose State
    val habits = repository.localHabits.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // State for the list of habits
    private val _habits = MutableStateFlow<List<Habit>>(emptyList())

    // State for loading/errors (useful for showing spinners or snackbars)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Turn on the Firebase "background pipe" when the screen opens
        repository.startRealtimeSync()
    }

    /**
     * Asks the repository for habits and updates the StateFlow
     */
    fun loadHabits() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = repository.getMyHabits()) {
                is Resource.Success -> {
                    _habits.value = result.data
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    /**
     * Creates a new habit and refreshes the list
     */
    fun createHabit(title: String, description: String, category: String, isPublic: Boolean, onSuccess: () -> Unit) {
        if (title.isBlank()) {
            _errorMessage.value = "Title cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.createHabit(title, description, category, isPublic)

            when (result) {
                is Resource.Success -> {
                    // Refresh the list from Firebase so the new habit appears
                    loadHabits()
                    onSuccess() // Tell the UI to navigate back or close the bottom sheet
                }
                is Resource.Error -> {
                    _errorMessage.value = result.message
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    /**
     * Called when the user taps the habit item to complete it
     */
    fun completeHabit(habit: Habit) {
        viewModelScope.launch {
            // We don't show a big loading spinner for this, it should feel instant
            val result = repository.markHabitCompleted(habit)

            when (result) {
                is Resource.Success -> {
                    // Refreshes the list from Firebase so the UI instantly shows the new Streak and Checkmark!
                    loadHabits()
                }
                is Resource.Error -> {
                    // We can optionally show a toast here if they already completed it
                    _errorMessage.value = result.message
                }
                else -> {}
            }
        }
    }
}