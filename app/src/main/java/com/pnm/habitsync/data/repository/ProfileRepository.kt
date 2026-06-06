package com.pnm.habitsync.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pnm.habitsync.data.local.AppDatabase
import com.pnm.habitsync.data.local.ProfileDao
import com.pnm.habitsync.data.model.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    private val profileDao: ProfileDao, // We injected this in the previous step!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val appDatabase: AppDatabase
) {
    // 1. SINGLE SOURCE OF TRUTH: UI listens to Room
    val localProfile: Flow<UserProfile?> = profileDao.getProfile()

    // 2. BACKGROUND SYNC
    fun startRealtimeSync() {
        val uid = auth.currentUser?.uid ?: return

        // We listen to the user's habits to calculate stats in real-time
        db.child("habits").orderByChild("userId").equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(habitsSnapshot: DataSnapshot) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            // Calculate Stats
                            val totalHabits = habitsSnapshot.childrenCount.toInt()
                            var totalStreaks = 0
                            for (habit in habitsSnapshot.children) {
                                val streak = habit.child("streakCount").getValue(Int::class.java) ?: 0
                                totalStreaks += streak
                            }

                            // Fetch Name & Email
                            val userSnapshot = db.child("users").child(uid).get().await()
                            val username = userSnapshot.child("username").getValue(String::class.java) ?: "Unknown"
                            val email = userSnapshot.child("email").getValue(String::class.java) ?: "Unknown"

                            // Save to Room Database!
                            val updatedProfile = UserProfile(
                                id = "local_user",
                                username = username,
                                email = email,
                                totalHabits = totalHabits,
                                highestStreak = totalStreaks
                            )
                            profileDao.insertProfile(updatedProfile)

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
    }

    // Keep the logout function exactly the same!
    suspend fun logout() {
        auth.signOut()
        appDatabase.clearAllTables() // Extremely important for security
    }
}