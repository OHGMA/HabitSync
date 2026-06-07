package com.pnm.habitsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pnm.habitsync.data.model.Habit
import com.pnm.habitsync.data.repository.HabitRepository
import com.pnm.habitsync.utils.DateUtils
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    // 1. SINGLE SOURCE OF TRUTH: Automatically convert Room's Flow into Compose State
    val habits = repository.localHabits.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // 2. Turn on the Firebase "background pipe" when the screen opens
        repository.startRealtimeSync()

        // 3. SILENT CLEANER: Watch the local Room data and clean up dead streaks automatically
        viewModelScope.launch {
            repository.localHabits.collect { currentHabits ->
                resetStreaks(currentHabits)
            }
        }
    }

    /**
     * Checks if a user missed a day. If they did, it silently resets the streak to 0.
     */
    private fun resetStreaks(habitsList: List<Habit>) {
        val today = DateUtils.getTodayString()
        val yesterday = DateUtils.getYesterdayString()

        for (habit in habitsList) {
            // If the habit wasn't done today AND wasn't done yesterday, the streak is broken.
            // We check streakCount > 0 so we don't accidentally cause an infinite update loop!
            if (habit.lastCompletedDate.isNotEmpty() &&
                habit.lastCompletedDate != today &&
                habit.lastCompletedDate != yesterday &&
                habit.streakCount > 0) {

                val brokenHabit = habit.copy(streakCount = 0)

                // Update silently in the background
                viewModelScope.launch(Dispatchers.IO) {
                    repository.updateHabit(brokenHabit)
                }
            }
        }
    }

    /**
     * Creates a new habit.
     * (We don't need to manually refresh anymore; Room will do it automatically!)
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
                is Resource.Success -> onSuccess()
                is Resource.Error -> _errorMessage.value = result.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    /**
     * Called when the user taps the habit item to complete it.
     * Evaluates if the streak should grow, or reset to 1.
     */
    fun completeHabit(habit: Habit) {
        val today = DateUtils.getTodayString()
        val yesterday = DateUtils.getYesterdayString()

        // Prevent double-clicking if they already did it today
        if (habit.lastCompletedDate == today) return

        // Evaluate the strict streak math
        val newStreak = if (habit.lastCompletedDate == yesterday) {
            habit.streakCount + 1 // They did it yesterday! Keep the fire burning.
        } else {
            1 // They missed yesterday. Start over at 1.
        }

        // Package the new data
        val updatedHabit = habit.copy(
            lastCompletedDate = today,
            streakCount = newStreak
        )

        // Send to repository (This updates Firebase AND Room automatically)
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateHabit(updatedHabit)
        }
    }
}