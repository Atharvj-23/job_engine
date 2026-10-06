package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.JobAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JobAlertDao {
  @Query("SELECT * FROM job_alerts WHERE userId = :userId ORDER BY createdAtMillis DESC")
  fun getAlertsForUser(userId: Long): Flow<List<JobAlertEntity>>

  @Query("SELECT * FROM job_alerts WHERE isActive = 1")
  suspend fun getActiveAlerts(): List<JobAlertEntity>

  @Query("SELECT * FROM job_alerts WHERE isActive = 1 ORDER BY createdAtMillis DESC")
  fun getActiveAlertsFlow(): Flow<List<JobAlertEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlert(alert: JobAlertEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAlerts(alerts: List<JobAlertEntity>)

  @Update
  suspend fun updateAlert(alert: JobAlertEntity)

  @Delete
  suspend fun deleteAlert(alert: JobAlertEntity)

  @Query("SELECT COUNT(*) FROM job_alerts")
  suspend fun getAlertCount(): Int
}
