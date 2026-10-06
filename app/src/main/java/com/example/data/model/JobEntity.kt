package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "jobs",
  indices = [
    Index(value = ["cloudId"], unique = true)
  ]
)
data class JobEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val cloudId: String? = null, // Cloud Firestore Document ID
  val title: String,
  val company: String,
  val industry: String, // "Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech"
  val location: String, // e.g. "Boston, MA", "San Francisco, CA", "Remote"
  val isRemote: Boolean = false,
  val jobType: String = "Full-time", // "Full-time", "Part-time", "Contract", "Internship"
  val experienceLevel: String = "Mid Level", // "Entry Level", "Mid Level", "Senior", "Lead / Principal"
  val salaryMin: Int = 0,
  val salaryMax: Int = 0,
  val salaryText: String = "",
  val description: String = "",
  val responsibilities: String = "",
  val requirements: String = "",
  val benefits: String = "",
  val postedDateMillis: Long = System.currentTimeMillis(),
  val employerEmail: String = "",
  val isActive: Boolean = true,
  val applicantCount: Int = 0,
  // Cloud sync tracking
  val syncStatus: String = "SYNCED", // "SYNCED", "PENDING_UPLOAD", "PENDING_UPDATE", "PENDING_DELETE"
  val lastSyncedAtMillis: Long = 0L,
  val updatedAtMillis: Long = System.currentTimeMillis()
) {
  companion object {
    const val SYNC_STATUS_SYNCED = "SYNCED"
    const val SYNC_STATUS_PENDING_UPLOAD = "PENDING_UPLOAD"
    const val SYNC_STATUS_PENDING_UPDATE = "PENDING_UPDATE"
    const val SYNC_STATUS_PENDING_DELETE = "PENDING_DELETE"
  }
}
