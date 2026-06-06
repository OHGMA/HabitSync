package com.pnm.habitsync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedDao {

    // Returns List<Long> to avoid the KSP Void bug
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedItems(items: List<FeedEntity>): List<Long>

    // Returns a Flow to automatically update the UI
    @Query("SELECT * FROM feed_table ORDER BY timestamp DESC")
    fun getAllFeedItems(): Flow<List<FeedEntity>>

    // We removed clearFeed() because we don't need it right now and KSP is fighting it!
}