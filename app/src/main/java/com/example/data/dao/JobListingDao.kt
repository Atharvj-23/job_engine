package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.JobListingEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for JobListingEntity local database operations.
 */
@Dao
interface JobListingDao {
  @Query("SELECT * FROM job_listings ORDER BY postedDateMillis DESC")
  fun getAllJobListings(): Flow<List<JobListingEntity>>

  @Query("SELECT * FROM job_listings WHERE industry = :industry ORDER BY postedDateMillis DESC")
  fun getJobListingsByIndustry(industry: String): Flow<List<JobListingEntity>>

  @Query("SELECT * FROM job_listings WHERE location = :location ORDER BY postedDateMillis DESC")
  fun getJobListingsByLocation(location: String): Flow<List<JobListingEntity>>

  @Query("SELECT * FROM job_listings WHERE experienceLevel = :experienceLevel ORDER BY postedDateMillis DESC")
  fun getJobListingsByExperience(experienceLevel: String): Flow<List<JobListingEntity>>

  @Query("SELECT * FROM job_listings WHERE id = :id LIMIT 1")
  fun getJobListingById(id: Long): Flow<JobListingEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJobListing(jobListing: JobListingEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJobListings(jobListings: List<JobListingEntity>)

  @Update
  suspend fun updateJobListing(jobListing: JobListingEntity)

  @Delete
  suspend fun deleteJobListing(jobListing: JobListingEntity)

  @Query("DELETE FROM job_listings WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("SELECT COUNT(*) FROM job_listings")
  suspend fun getJobListingCount(): Int
}
