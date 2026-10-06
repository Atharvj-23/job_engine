package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.JobEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
  @Query("SELECT * FROM jobs ORDER BY postedDateMillis DESC")
  fun getAllJobs(): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE isActive = 1 ORDER BY postedDateMillis DESC")
  fun getActiveJobs(): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE id = :id")
  fun getJobById(id: Long): Flow<JobEntity?>

  @Query("SELECT * FROM jobs WHERE employerEmail = :employerEmail ORDER BY postedDateMillis DESC")
  fun getJobsByEmployer(employerEmail: String): Flow<List<JobEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJob(job: JobEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJobs(jobs: List<JobEntity>)

  @Update
  suspend fun updateJob(job: JobEntity)

  @Delete
  suspend fun deleteJob(job: JobEntity)

  @Query("UPDATE jobs SET applicantCount = applicantCount + 1 WHERE id = :jobId")
  suspend fun incrementApplicantCount(jobId: Long)

  @Query("SELECT COUNT(*) FROM jobs")
  suspend fun getJobCount(): Int

  // --- Cloud Storage & Firestore Integration ---

  @Query("SELECT * FROM jobs WHERE cloudId = :cloudId LIMIT 1")
  fun getJobByCloudId(cloudId: String): Flow<JobEntity?>

  @Query("SELECT * FROM jobs WHERE cloudId = :cloudId LIMIT 1")
  suspend fun findJobByCloudId(cloudId: String): JobEntity?

  @Query("SELECT * FROM jobs WHERE syncStatus != 'SYNCED' ORDER BY updatedAtMillis ASC")
  fun getUnsyncedJobsFlow(): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE syncStatus != 'SYNCED' ORDER BY updatedAtMillis ASC")
  suspend fun getUnsyncedJobs(): List<JobEntity>

  @Query("UPDATE jobs SET cloudId = :cloudId, syncStatus = :syncStatus, lastSyncedAtMillis = :syncedAt WHERE id = :id")
  suspend fun updateCloudSyncStatus(id: Long, cloudId: String, syncStatus: String, syncedAt: Long = System.currentTimeMillis())

  @Query("UPDATE jobs SET syncStatus = :syncStatus, updatedAtMillis = :updatedAt WHERE id = :id")
  suspend fun markSyncStatus(id: Long, syncStatus: String, updatedAt: Long = System.currentTimeMillis())

  @Query("DELETE FROM jobs WHERE cloudId = :cloudId")
  suspend fun deleteByCloudId(cloudId: String)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCloudJobs(jobs: List<JobEntity>)
}
