package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity corresponding to JobListing to persist job details in the local database.
 */
@Entity(
  tableName = "job_listings",
  indices = [
    Index(value = ["industry"]),
    Index(value = ["location"]),
    Index(value = ["experienceLevel"])
  ]
)
data class JobListingEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val company: String,
  val industry: String, // e.g. "Engineering", "Pharmacy", "Healthcare"
  val location: String, // e.g. "Boston, MA", "Remote"
  val experienceLevel: String, // e.g. "Entry Level", "Mid Level", "Senior"
  val isRemote: Boolean = false,
  val jobType: String = "Full-time",
  val salaryText: String = "",
  val salaryMin: Int = 0,
  val salaryMax: Int = 0,
  val description: String = "",
  val requirements: String = "",
  val employerEmail: String = "",
  val postedDateMillis: Long = System.currentTimeMillis()
) {
  fun toJobListing(): JobListing = JobListing(
    id = id,
    title = title,
    company = company,
    industry = industry,
    location = location,
    experienceLevel = experienceLevel,
    isRemote = isRemote,
    jobType = jobType,
    salaryText = salaryText,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    description = description,
    requirements = requirements,
    employerEmail = employerEmail,
    postedDateMillis = postedDateMillis
  )

  fun toJobEntity(): JobEntity = JobEntity(
    id = id,
    title = title,
    company = company,
    industry = industry,
    location = location,
    isRemote = isRemote,
    jobType = jobType,
    experienceLevel = experienceLevel,
    salaryMin = salaryMin,
    salaryMax = salaryMax,
    salaryText = salaryText,
    description = description,
    responsibilities = "",
    requirements = requirements,
    benefits = "",
    employerEmail = employerEmail,
    postedDateMillis = postedDateMillis
  )

  companion object {
    fun fromJobListing(listing: JobListing): JobListingEntity = JobListingEntity(
      id = listing.id,
      title = listing.title,
      company = listing.company,
      industry = listing.industry,
      location = listing.location,
      experienceLevel = listing.experienceLevel,
      isRemote = listing.isRemote,
      jobType = listing.jobType,
      salaryText = listing.salaryText,
      salaryMin = listing.salaryMin,
      salaryMax = listing.salaryMax,
      description = listing.description,
      requirements = listing.requirements,
      employerEmail = listing.employerEmail,
      postedDateMillis = listing.postedDateMillis
    )
  }
}
