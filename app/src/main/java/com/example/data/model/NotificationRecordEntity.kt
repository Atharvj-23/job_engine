package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notification_records")
data class NotificationRecordEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: Long = 1,
  val jobId: Long,
  val jobTitle: String,
  val company: String,
  val alertTitle: String,
  val channelType: String = "BOTH", // "PUSH", "EMAIL", "BOTH"
  val recipientEmail: String,
  val message: String,
  val timestampMillis: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)
