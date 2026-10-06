package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_alerts")
data class JobAlertEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val userId: Long = 1,
  val title: String,
  val industry: String = "All", // e.g. "Pharmacy", "Engineering", "Healthcare", "All"
  val location: String = "", // e.g. "Boston", "Remote", "" (any)
  val experienceLevel: String = "All", // "All", "Entry Level", "Mid Level", "Senior"
  val minSalary: Int = 0,
  val emailAlerts: Boolean = true,
  val pushAlerts: Boolean = true,
  val isActive: Boolean = true,
  val createdAtMillis: Long = System.currentTimeMillis(),
  val lastTriggeredMillis: Long = 0L
)
