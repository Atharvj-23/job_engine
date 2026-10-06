package com.example.data.model

/**
 * Data class representing a job listing in the application.
 * Stores core job details such as title, company, industry, location, and experience level.
 */
data class JobListing(
  val id: Long = 0,
  val title: String,
  val company: String,
  val industry: String, // e.g., "Engineering", "Pharmacy", "Healthcare"
  val location: String, // e.g., "Boston, MA", "San Francisco, CA", "Remote"
  val experienceLevel: String, // e.g., "Entry Level", "Mid Level", "Senior"
  val isRemote: Boolean = false,
  val jobType: String = "Full-time",
  val salaryText: String = "",
  val salaryMin: Int = 0,
  val salaryMax: Int = 0,
  val description: String = "",
  val requirements: String = "",
  val employerEmail: String = "",
  val postedDateMillis: Long = System.currentTimeMillis()
)
