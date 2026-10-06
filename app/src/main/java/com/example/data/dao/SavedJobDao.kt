package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedJobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedJobDao {
  @Query("SELECT * FROM saved_jobs WHERE userId = :userId ORDER BY savedAtMillis DESC")
  fun getSavedJobsForUser(userId: Long): Flow<List<SavedJobEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE jobId = :jobId AND userId = :userId)")
  fun isJobSaved(jobId: Long, userId: Long): Flow<Boolean>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSavedJob(savedJob: SavedJobEntity)

  @Query("DELETE FROM saved_jobs WHERE jobId = :jobId AND userId = :userId")
  suspend fun deleteSavedJob(jobId: Long, userId: Long)

  @Query("DELETE FROM saved_jobs WHERE jobId = :jobId")
  suspend fun deleteSavedJobsByJobId(jobId: Long)
}
