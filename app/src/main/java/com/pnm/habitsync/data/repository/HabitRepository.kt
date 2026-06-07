package com.pnm.habitsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pnm.habitsync.data.local.HabitDao
import com.pnm.habitsync.data.model.Habit
import com.pnm.habitsync.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HabitRepository(
    private val habitDao: HabitDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) {
    val localHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    fun startRealtimeSync() {
        val uid = auth.currentUser?.uid ?: return

        db.child("habits").orderByChild("userId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val habitsList = mutableListOf<Habit>()
                    for (child in snapshot.children) {
                        child.getValue(Habit::class.java)?.let { habitsList.add(it) }
                    }

                    // Save Firebase data straight into the local database!
                    // This automatically triggers `localHabits` above and updates the UI!
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                        habitDao.insertHabits(habitsList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if needed
                }
            })
    }

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

    // Pushes any updated habit straight to Firebase.
    // Your background listener will automatically catch this and update Room!
    suspend fun updateHabit(habit: Habit) {
        db.child("habits").child(habit.id).setValue(habit).await()
    }
}