package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.NotificationRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notification_records WHERE userId = :userId ORDER BY timestampMillis DESC")
  fun getNotificationsForUser(userId: Long): Flow<List<NotificationRecordEntity>>

  @Query("SELECT COUNT(*) FROM notification_records WHERE userId = :userId AND isRead = 0")
  fun getUnreadCount(userId: Long): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationRecordEntity): Long

  @Query("UPDATE notification_records SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: Long)

  @Query("UPDATE notification_records SET isRead = 1 WHERE userId = :userId")
  suspend fun markAllAsRead(userId: Long)

  @Query("DELETE FROM notification_records WHERE userId = :userId")
  suspend fun clearAll(userId: Long)
}
