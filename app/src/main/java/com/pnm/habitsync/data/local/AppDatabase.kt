package com.pnm.habitsync.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Version number must increase if you ever change the variables in FeedEntity later!
@Database(entities = [FeedEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun feedDao(): FeedDao

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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}