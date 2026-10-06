package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val email: String,
  val fullName: String,
  val role: String = "SEEKER", // "SEEKER" or "EMPLOYER"
  val headline: String = "",
  val industry: String = "Engineering",
  val experienceLevel: String = "Mid Level",
  val location: String = "",
  val phone: String = "",
  val bio: String = "",
  val skills: String = "",
  val companyName: String = "",
  val companyWebsite: String = "",
  val companyBio: String = "",
  val avatarUrl: String = "",
  val resumeSnippet: String = "",
  val preferredJobType: String = "Full-time",
  val preferredWorkMode: String = "Remote",
  val minExpectedSalary: Int = 0,
  val portfolioUrl: String = "",
  val fullResumeText: String = "",
  val atsScore: Int = 0,
  val atsAnalysisJson: String = ""
)
