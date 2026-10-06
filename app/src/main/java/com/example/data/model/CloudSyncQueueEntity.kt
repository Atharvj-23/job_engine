package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cloud_sync_queue")
data class CloudSyncQueueEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val entityType: String = "JOB", // "JOB", "APPLICATION", "ALERT"
  val localEntityId: Long,
  val cloudId: String? = null,
  val operation: String, // "CREATE", "UPDATE", "DELETE"
  val payloadJson: String = "",
  val retryCount: Int = 0,
  val lastAttemptMillis: Long = 0L,
  val status: String = "PENDING", // "PENDING", "IN_PROGRESS", "FAILED", "COMPLETED"
  val createdAtMillis: Long = System.currentTimeMillis()
)
