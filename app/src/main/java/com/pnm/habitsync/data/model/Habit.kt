package com.pnm.habitsync.data.model

// Represents a single Habit
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits_table")
data class Habit(
    @PrimaryKey val id: String = "", // Room will use this as the unique row ID
    val userId: String = "",
    val title: String = "",
    val category: String = "",
    val description: String = "",
    val streakCount: Int = 0,
    val lastCompletedDate: String = "",
    val public: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)