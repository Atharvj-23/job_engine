package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_jobs")
data class SavedJobEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val jobId: Long,
  val userId: Long = 1,
  val savedAtMillis: Long = System.currentTimeMillis()
)
