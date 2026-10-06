package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val jobId: Long,
  val applicantId: Long = 1,
  val applicantName: String,
  val applicantEmail: String,
  val applicantHeadline: String = "",
  val resumeNote: String = "",
  val status: String = "APPLIED", // "APPLIED", "REVIEWING", "INTERVIEW", "ACCEPTED", "DECLINED"
  val appliedAtMillis: Long = System.currentTimeMillis()
)
