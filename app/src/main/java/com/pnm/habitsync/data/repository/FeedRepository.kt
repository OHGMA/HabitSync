package com.pnm.habitsync.data.repository

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pnm.habitsync.data.local.FeedDao
import com.pnm.habitsync.data.local.FeedEntity
import com.pnm.habitsync.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FeedRepository(
    private val feedDao: FeedDao,
    private val db: DatabaseReference = FirebaseDatabase.getInstance().reference
) {
    val feed: Flow<List<FeedEntity>> = feedDao.getAllFeedItems()

    suspend fun syncFeed() {
        try {
            // 1. Fetch ALL habits where isPublic == true
            val snapshot = db.child("habits")
                .orderByChild("public")
                .equalTo(true)
                .limitToLast(50)
                .get()
                .await()

            // 2. Fetch users to get usernames (This is how we do a NoSQL "Join" locally!)
            val usersSnapshot = db.child("users").get().await()
            val userMap = mutableMapOf<String, String>()
            for (userChild in usersSnapshot.children) {
                val uid = userChild.key ?: continue
                val name = userChild.child("username").getValue(String::class.java) ?: "Unknown"
                userMap[uid] = name
            }

            val freshFeed = mutableListOf<FeedEntity>()
            val today = DateUtils.getTodayString()

            for (child in snapshot.children) {
                val habitId = child.key ?: continue
                val userId = child.child("userId").getValue(String::class.java) ?: ""
                val title = child.child("title").getValue(String::class.java) ?: ""
                val lastCompleted = child.child("lastCompletedDate").getValue(String::class.java) ?: ""

                // We keep createdAt so Room can sort the newest habits to the top
                val timestamp = child.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()

                val username = userMap[userId] ?: "Unknown User"

                // 3. Evaluate Done vs Missed locally!
                val isDone = lastCompleted == today
                val statusText = if (isDone) "Today" else "Yesterday"

                freshFeed.add(
                    FeedEntity(
                        activityId = habitId,
                        username = username,
                        habitTitle = title,
                        status = statusText, // Will say "Today" or "Yesterday"
                        timestamp = timestamp,
                        isDone = isDone
                    )
                )
            }

            // Save to Room cache
            feedDao.insertFeedItems(freshFeed)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}