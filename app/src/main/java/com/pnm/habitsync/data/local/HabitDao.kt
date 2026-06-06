package com.pnm.habitsync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pnm.habitsync.data.model.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Query("SELECT * FROM habits_table ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>> // UI listens to this!
}