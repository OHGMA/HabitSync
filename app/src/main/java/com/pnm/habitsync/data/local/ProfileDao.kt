package com.pnm.habitsync.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pnm.habitsync.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("SELECT * FROM profile_table WHERE id = 'local_user'")
    fun getProfile(): Flow<UserProfile?>
}