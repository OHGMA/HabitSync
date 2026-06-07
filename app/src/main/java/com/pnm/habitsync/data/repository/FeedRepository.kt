package com.pnm.habitsync.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pnm.habitsync.data.local.FeedDao
import com.pnm.habitsync.data.local.FeedEntity
import com.pnm.habitsync.utils.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedRepository(
    private val feedDao: FeedDao,
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) {
    // SINGLE SOURCE OF TRUTH: The UI only watches Room
    val feed: Flow<List<FeedEntity>> = feedDao.getAllFeedItems()

    // NEW: Real-time background pipe!
    fun startRealtimeSync() {
        val query = db.child("habits")
            .orderByChild("public")
            .equalTo(true)
            .limitToLast(50)

        // This listener stays open forever while the app is running
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                // Jump into a background thread so we don't freeze the UI
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // 1. Fetch users to get usernames
                        val usersSnapshot = db.child("users").get().await()
                        val userMap = mutableMapOf<String, String>()
                        for (userChild in usersSnapshot.children) {
                            val uid = userChild.key ?: continue
                            val name = userChild.child("username").getValue(String::class.java) ?: "Unknown"
                            userMap[uid] = name
                        }

                        val freshFeed = mutableListOf<FeedEntity>()
                        val today = DateUtils.getTodayString()

                        // 2. Process all public habits
                        for (child in snapshot.children) {
                            val habitId = child.key ?: continue
                            val userId = child.child("userId").getValue(String::class.java) ?: ""
                            val title = child.child("title").getValue(String::class.java) ?: ""
                            val lastCompleted = child.child("lastCompletedDate").getValue(String::class.java) ?: ""
                            val timestamp = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()

                            val username = userMap[userId] ?: "Unknown User"
                            val isDone = lastCompleted == today
                            val statusText = if (isDone) "Today" else "Yesterday"

                            freshFeed.add(
                                FeedEntity(
                                    activityId = habitId,
                                    username = username,
                                    habitTitle = title,
                                    status = statusText,
                                    timestamp = timestamp,
                                    isDone = isDone
                                )
                            )
                        }

                        // 3. Shove it directly into Room. The UI will update instantly!
                        feedDao.insertFeedItems(freshFeed)

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
}