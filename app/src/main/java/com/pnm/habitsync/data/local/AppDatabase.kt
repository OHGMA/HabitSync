package com.pnm.habitsync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pnm.habitsync.data.model.Habit
import com.pnm.habitsync.data.model.UserProfile

// Version number must increase if you ever change the variables in FeedEntity later!
@Database(entities = [FeedEntity::class, Habit::class, UserProfile::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedDao(): FeedDao
    abstract fun habitDao(): HabitDao
    abstract fun profileDao(): ProfileDao

    companion object {
        // Volatile ensures that any changes to this variable are immediately visible to other threads
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Standard Singleton pattern to ensure we only open ONE connection to the database
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitsync_database"
                )
                    .fallbackToDestructiveMigration(false) // Data is lost if version is changed
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}