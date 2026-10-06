package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.JobApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobApplicationDao {
  @Query("SELECT * FROM job_applications WHERE applicantId = :applicantId ORDER BY appliedAtMillis DESC")
  fun getApplicationsForUser(applicantId: Long): Flow<List<JobApplicationEntity>>

  @Query("SELECT * FROM job_applications WHERE jobId = :jobId ORDER BY appliedAtMillis DESC")
  fun getApplicationsForJob(jobId: Long): Flow<List<JobApplicationEntity>>

  @Query("SELECT * FROM job_applications ORDER BY appliedAtMillis DESC")
  fun getAllApplications(): Flow<List<JobApplicationEntity>>

  @Query("SELECT * FROM job_applications WHERE jobId = :jobId AND applicantId = :applicantId LIMIT 1")
  fun getApplication(jobId: Long, applicantId: Long): Flow<JobApplicationEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertApplication(application: JobApplicationEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertApplications(applications: List<JobApplicationEntity>)

  @Update
  suspend fun updateApplication(application: JobApplicationEntity)

  @Query("UPDATE job_applications SET status = :newStatus WHERE id = :id")
  suspend fun updateApplicationStatus(id: Long, newStatus: String)

  @Query("SELECT COUNT(*) FROM job_applications")
  suspend fun getApplicationCount(): Int
}
