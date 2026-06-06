package com.pnm.habitsync.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// @Entity tells Room to create a SQLite table for this class
@Entity(tableName = "feed_table")
data class FeedEntity(
    @PrimaryKey val activityId: String, // Firebase's unique ID acts as our primary key
    val username: String,
    val habitTitle: String,
    val status: String, // e.g., "completed", "missed"
    val timestamp: Long,
    val isDone: Boolean // Used to show the green check or red X in the UI
)