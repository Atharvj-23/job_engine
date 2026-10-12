package com.example.data.model

/**
 * Data class representing a job listing in the application.
 * Stores core job details such as title, company, industry, location, and experience level.
 */
data class JobListing(
  val id: Long = 0,
  val cloudId: String? = null,
  val title: String,
  val company: String,
  val industry: String = "Engineering", // e.g., "Engineering", "Pharmacy", "Healthcare"
  val location: String = "Boston, MA", // e.g., "Boston, MA", "San Francisco, CA", "Remote"
  val experienceLevel: String = "Mid Level", // e.g., "Entry Level", "Mid Level", "Senior"
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
  fun toFirestoreMap(): Map<String, Any?> {
    return mapOf(
      "title" to title,
      "company" to company,
      "industry" to industry,
      "location" to location,
      "experienceLevel" to experienceLevel,
      "isRemote" to isRemote,
      "jobType" to jobType,
      "salaryText" to salaryText,
      "salaryMin" to salaryMin,
      "salaryMax" to salaryMax,
      "description" to description,
      "requirements" to requirements,
      "employerEmail" to employerEmail,
      "postedDateMillis" to postedDateMillis
    )
  }

  companion object {
    fun fromFirestoreMap(id: String, data: Map<String, Any?>): JobListing {
      return JobListing(
        cloudId = id,
        title = data["title"] as? String ?: "",
        company = data["company"] as? String ?: "",
        industry = data["industry"] as? String ?: "Engineering",
        location = data["location"] as? String ?: "Remote",
        experienceLevel = data["experienceLevel"] as? String ?: "Mid Level",
        isRemote = data["isRemote"] as? Boolean ?: false,
        jobType = data["jobType"] as? String ?: "Full-time",
        salaryText = data["salaryText"] as? String ?: "",
        salaryMin = (data["salaryMin"] as? Number)?.toInt() ?: 0,
        salaryMax = (data["salaryMax"] as? Number)?.toInt() ?: 0,
        description = data["description"] as? String ?: "",
        requirements = data["requirements"] as? String ?: "",
        employerEmail = data["employerEmail"] as? String ?: "",
        postedDateMillis = (data["postedDateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
      )
    }
  }
}
