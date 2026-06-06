package com.pnm.habitsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pnm.habitsync.data.model.Habit
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.tasks.await

class HabitRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) {
    // Helper to get the currently logged-in user's ID
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Creates a new habit in the Firebase Realtime Database
     */
    suspend fun createHabit(title: String, description: String, category: String, isPublic: Boolean): Resource<Boolean> {
        val uid = currentUserId ?: return Resource.Error("User not logged in")

        return try {
            // 1. Create a new empty "slot" in the "habits" node to get a unique ID
            val newHabitRef = db.child("habits").push()
            val habitId = newHabitRef.key ?: return Resource.Error("Failed to generate ID")

            // 2. Create the Habit object
            val habit = Habit(
                id = habitId,
                userId = uid,
                title = title,
                description = description,
                category = category,
                public = isPublic
            )

            // 3. Save it to Firebase
            newHabitRef.setValue(habit).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create habit")
        }
    }

    /**
     * Fetches all habits belonging to the current user
     */
    suspend fun getMyHabits(): Resource<List<Habit>> {
        val uid = currentUserId ?: return Resource.Error("User not logged in")

        return try {
            // Query Firebase: Give me all items in "habits" where "userId" equals my uid
            val snapshot = db.child("habits")
                .orderByChild("userId")
                .equalTo(uid)
                .get()
                .await()

            // Convert the Firebase JSON response into a list of Kotlin Habit objects
            val habits = snapshot.children.mapNotNull { it.getValue(Habit::class.java) }

            // Sort by creation date so the newest ones show up first (or last, depending on preference)
            Resource.Success(habits.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load habits")
        }
    }

    /**
     * Marks a habit as completed for today and calculates the new streak
     */
    suspend fun markHabitCompleted(habit: Habit): Resource<Boolean> {
        val uid = currentUserId ?: return Resource.Error("User not logged in")
        val today = com.pnm.habitsync.utils.DateUtils.getTodayString()
        val yesterday = com.pnm.habitsync.utils.DateUtils.getYesterdayString()

        // 1. Check if they already did it today
        if (habit.lastCompletedDate == today) {
            return Resource.Error("Already completed today!")
        }

        // 2. Calculate the new streak
        val newStreak = if (habit.lastCompletedDate == yesterday) {
            habit.streakCount + 1 // Streak continues!
        } else {
            1 // They missed a day, streak resets back to 1
        }

        return try {
            // 3. Create a map of only the fields we want to update
            val updates = mapOf<String, Any>(
                "streakCount" to newStreak,
                "lastCompletedDate" to today
            )

            // 4. Send the update to Firebase
            // Path: habits/{habitId}
            db.child("habits").child(habit.id).updateChildren(updates).await()

            // NOTE: For the Social Feed later, we would also add to "feed_activity" here!

            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update habit")
        }
    }
}