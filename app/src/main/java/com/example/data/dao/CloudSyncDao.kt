package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CloudSyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CloudSyncDao {
  @Query("SELECT * FROM cloud_sync_queue WHERE status = 'PENDING' ORDER BY createdAtMillis ASC")
  fun getPendingSyncOperations(): Flow<List<CloudSyncQueueEntity>>

  @Query("SELECT * FROM cloud_sync_queue WHERE status = 'PENDING' ORDER BY createdAtMillis ASC")
  suspend fun getPendingSyncOperationsList(): List<CloudSyncQueueEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun queueOperation(operation: CloudSyncQueueEntity): Long

  @Update
  suspend fun updateOperation(operation: CloudSyncQueueEntity)

  @Delete
  suspend fun removeOperation(operation: CloudSyncQueueEntity)

  @Query("UPDATE cloud_sync_queue SET status = :newStatus, lastAttemptMillis = :attemptTime WHERE id = :id")
  suspend fun updateStatus(id: Long, newStatus: String, attemptTime: Long = System.currentTimeMillis())

  @Query("DELETE FROM cloud_sync_queue WHERE status = 'COMPLETED'")
  suspend fun purgeCompleted()

  @Query("SELECT COUNT(*) FROM cloud_sync_queue WHERE status = 'PENDING'")
  fun getPendingCount(): Flow<Int>
}
