package com.pnm.habitsync.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "profile_table")
data class UserProfile(
    @PrimaryKey val id: String = "local_user", // Always overwrite the same row
    val username: String = "Loading...",
    val email: String = "Loading...",
    val totalHabits: Int = 0,
    val highestStreak: Int = 0
)