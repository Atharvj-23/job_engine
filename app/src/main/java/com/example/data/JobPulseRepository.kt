package com.example.data

import android.content.Context
import com.example.data.cloud.FirestoreJobSyncService
import com.example.data.model.CloudSyncQueueEntity
import com.example.data.model.JobAlertEntity
import com.example.data.model.JobApplicationEntity
import com.example.data.model.JobEntity
import com.example.data.model.JobListingEntity
import com.example.data.model.NotificationRecordEntity
import com.example.data.model.SavedJobEntity
import com.example.data.model.UserProfileEntity
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JobPulseRepository(
  private val database: JobPulseDatabase,
  private val context: Context
) {
  private val jobDao = database.jobDao()
  private val userDao = database.userDao()
  private val alertDao = database.jobAlertDao()
  private val appDao = database.jobApplicationDao()
  private val savedJobDao = database.savedJobDao()
  private val notifDao = database.notificationDao()
  private val cloudSyncDao = database.cloudSyncDao()
  private val jobListingDao = database.jobListingDao()

  init {
    CoroutineScope(Dispatchers.IO).launch {
      seedDatabaseIfEmpty()
      syncJobsWithCloud()
      // Listen to real-time Firestore updates
      FirestoreJobSyncService.listenToCloudJobs { cloudJobs ->
        CoroutineScope(Dispatchers.IO).launch {
          for (cloudJob in cloudJobs) {
            upsertCloudJobLocally(cloudJob)
          }
        }
      }
    }
  }

  private suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
    if (userDao.getUserCount() == 0) {
      userDao.insertUser(SampleData.defaultSeeker)
      userDao.insertUser(SampleData.defaultEmployer)
      userDao.insertUser(SampleData.defaultAdmin)
    }
    if (jobDao.getJobCount() == 0) {
      jobDao.insertJobs(SampleData.initialJobs)
    }
    if (jobListingDao.getJobListingCount() == 0) {
      val listings = SampleData.initialJobs.map { job ->
        JobListingEntity(
          id = job.id,
          title = job.title,
          company = job.company,
          industry = job.industry,
          location = job.location,
          experienceLevel = job.experienceLevel,
          isRemote = job.isRemote,
          jobType = job.jobType,
          salaryText = job.salaryText,
          salaryMin = job.salaryMin,
          salaryMax = job.salaryMax,
          description = job.description,
          requirements = job.requirements,
          employerEmail = job.employerEmail,
          postedDateMillis = job.postedDateMillis
        )
      }
      jobListingDao.insertJobListings(listings)
    }
    if (alertDao.getAlertCount() == 0) {
      alertDao.insertAlerts(SampleData.initialAlerts)
      // Also seed an initial notification record so user sees notification center populated
      val initialNotif = NotificationRecordEntity(
        id = 1,
        userId = 1,
        jobId = 1,
        jobTitle = "Clinical Pharmacist - Oncology Specialist",
        company = "Boston Metro Health System",
        alertTitle = "Pharmacy & Clinical Roles in Boston",
        channelType = "BOTH",
        recipientEmail = "alex.morgan@careermail.com",
        message = "Matching alert 'Pharmacy & Clinical Roles in Boston': New opening posted by Boston Metro Health System ($135K - $170K/yr).",
        timestampMillis = System.currentTimeMillis() - 3600000 * 2,
        isRead = false
      )
      notifDao.insertNotification(initialNotif)
    }
  }

  // Jobs
  val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobs()
  val activeJobs: Flow<List<JobEntity>> = jobDao.getActiveJobs()

  fun getJobById(id: Long): Flow<JobEntity?> = jobDao.getJobById(id)

  fun getJobsByEmployer(email: String): Flow<List<JobEntity>> = jobDao.getJobsByEmployer(email)

  suspend fun postJob(job: JobEntity, activeUserEmail: String?): Long = withContext(Dispatchers.IO) {
    val preparedJob = job.copy(
      employerEmail = activeUserEmail ?: job.employerEmail,
      syncStatus = JobEntity.SYNC_STATUS_PENDING_UPLOAD
    )
    val newId = jobDao.insertJob(preparedJob)
    val createdJob = preparedJob.copy(id = newId)

    // Attempt cloud upload in background
    CoroutineScope(Dispatchers.IO).launch {
      val result = FirestoreJobSyncService.uploadJobToCloud(createdJob)
      result.onSuccess { cloudDocId ->
        jobDao.updateCloudSyncStatus(
          id = newId,
          cloudId = cloudDocId,
          syncStatus = JobEntity.SYNC_STATUS_SYNCED
        )
      }.onFailure {
        cloudSyncDao.queueOperation(
          CloudSyncQueueEntity(
            entityType = "JOB",
            localEntityId = newId,
            operation = "CREATE"
          )
        )
      }
    }

    // Check alerts against the newly posted job!
    checkAlertsForJob(createdJob)
    newId
  }

  suspend fun syncJobsWithCloud() = withContext(Dispatchers.IO) {
    // 1. Upload unsynced local jobs
    val unsynced = jobDao.getUnsyncedJobs()
    for (job in unsynced) {
      val uploadResult = FirestoreJobSyncService.uploadJobToCloud(job)
      uploadResult.onSuccess { docId ->
        jobDao.updateCloudSyncStatus(
          id = job.id,
          cloudId = docId,
          syncStatus = JobEntity.SYNC_STATUS_SYNCED
        )
      }
    }

    // 2. Fetch cloud jobs from Firestore and upsert locally
    val fetchResult = FirestoreJobSyncService.fetchCloudJobs()
    fetchResult.onSuccess { cloudJobs ->
      for (cloudJob in cloudJobs) {
        upsertCloudJobLocally(cloudJob)
      }
    }
  }

  suspend fun upsertCloudJobLocally(cloudJob: JobEntity) = withContext(Dispatchers.IO) {
    val existing = cloudJob.cloudId?.let { jobDao.findJobByCloudId(it) }
    val localId = if (existing == null) {
      jobDao.insertJob(cloudJob)
    } else {
      jobDao.updateJob(cloudJob.copy(id = existing.id))
      existing.id
    }
    val listing = JobListingEntity(
      id = localId,
      title = cloudJob.title,
      company = cloudJob.company,
      industry = cloudJob.industry,
      location = cloudJob.location,
      experienceLevel = cloudJob.experienceLevel,
      isRemote = cloudJob.isRemote,
      jobType = cloudJob.jobType,
      salaryText = cloudJob.salaryText,
      salaryMin = cloudJob.salaryMin,
      salaryMax = cloudJob.salaryMax,
      description = cloudJob.description,
      requirements = cloudJob.requirements,
      employerEmail = cloudJob.employerEmail,
      postedDateMillis = cloudJob.postedDateMillis
    )
    jobListingDao.insertJobListing(listing)
  }

  suspend fun updateJob(job: JobEntity) = withContext(Dispatchers.IO) {
    jobDao.updateJob(job.copy(syncStatus = JobEntity.SYNC_STATUS_PENDING_UPDATE))
    if (job.cloudId != null) {
      CoroutineScope(Dispatchers.IO).launch {
        FirestoreJobSyncService.uploadJobToCloud(job)
      }
    }
  }

  suspend fun deleteJob(job: JobEntity) = withContext(Dispatchers.IO) {
    jobDao.deleteJob(job)
    savedJobDao.deleteSavedJobsByJobId(job.id)
  }

  // Users & Profiles
  fun getUser(id: Long): Flow<UserProfileEntity?> = userDao.getUserById(id)
  val allUsers: Flow<List<UserProfileEntity>> = userDao.getAllUsers()

  suspend fun saveUserProfile(profile: UserProfileEntity): Long = withContext(Dispatchers.IO) {
    if (profile.id == 0L) {
      userDao.insertUser(profile)
    } else {
      userDao.updateUser(profile)
      profile.id
    }
  }

  // Alerts
  fun getAlertsForUser(userId: Long): Flow<List<JobAlertEntity>> = alertDao.getAlertsForUser(userId)

  suspend fun saveAlert(alert: JobAlertEntity): Long = withContext(Dispatchers.IO) {
    val alertId = if (alert.id == 0L) {
      alertDao.insertAlert(alert)
    } else {
      alertDao.updateAlert(alert)
      alert.id
    }
    // Scan existing jobs to see if any match right away
    scanJobsForAlert(alert.copy(id = alertId))
    alertId
  }

  suspend fun deleteAlert(alert: JobAlertEntity) = withContext(Dispatchers.IO) {
    alertDao.deleteAlert(alert)
  }

  suspend fun triggerAlertCheckManual(userId: Long): Int = withContext(Dispatchers.IO) {
    val alerts = alertDao.getActiveAlerts().filter { it.userId == userId }
    val jobs = jobDao.getAllJobs().first()
    var matchCount = 0

    val currentUser = userDao.getUserById(userId).first() ?: SampleData.defaultSeeker

    for (alert in alerts) {
      for (job in jobs) {
        if (doesJobMatchAlert(job, alert)) {
          matchCount++
          dispatchAlertDelivery(alert, job, currentUser)
        }
      }
    }
    matchCount
  }

  private suspend fun checkAlertsForJob(job: JobEntity) {
    val alerts = alertDao.getActiveAlerts()
    for (alert in alerts) {
      if (doesJobMatchAlert(job, alert)) {
        val user = userDao.getUserById(alert.userId).first() ?: SampleData.defaultSeeker
        dispatchAlertDelivery(alert, job, user)
      }
    }
    triggerEmailNotificationForMatchingPreferences(job)
  }

  suspend fun triggerEmailNotificationForMatchingPreferences(job: JobEntity) = withContext(Dispatchers.IO) {
    try {
      val users = userDao.getAllUsers().first()
      for (user in users) {
        if (user.role == "SEEKER" && user.email.isNotBlank()) {
          val matchesIndustry = user.industry.isNotBlank() &&
              job.industry.contains(user.industry, ignoreCase = true)
          val matchesSalary = user.minExpectedSalary == 0 ||
              (job.salaryMax > 0 && job.salaryMax >= user.minExpectedSalary) ||
              (job.salaryMin > 0 && job.salaryMin >= user.minExpectedSalary)

          if (matchesIndustry && matchesSalary) {
            val emailNotif = NotificationRecordEntity(
              userId = user.id,
              jobId = job.id,
              jobTitle = job.title,
              company = job.company,
              alertTitle = "🎯 Preference Match: ${job.industry}",
              channelType = "EMAIL",
              recipientEmail = user.email,
              message = "📧 Email Sent to ${user.email}: '${job.title}' at ${job.company} (${job.salaryText}) matches your saved ${user.industry} preferences.",
              timestampMillis = System.currentTimeMillis(),
              isRead = false
            )
            notifDao.insertNotification(emailNotif)
          }
        }
      }
    } catch (e: Exception) {
      // Ignore
    }
  }

  private suspend fun scanJobsForAlert(alert: JobAlertEntity) {
    if (!alert.isActive) return
    val jobs = jobDao.getAllJobs().first()
    val user = userDao.getUserById(alert.userId).first() ?: SampleData.defaultSeeker
    for (job in jobs) {
      if (doesJobMatchAlert(job, alert)) {
        dispatchAlertDelivery(alert, job, user)
        break // notify for top match when creating
      }
    }
  }

  private fun doesJobMatchAlert(job: JobEntity, alert: JobAlertEntity): Boolean {
    if (!job.isActive) return false

    // Industry match
    if (alert.industry.isNotBlank() && alert.industry != "All") {
      if (!job.industry.contains(alert.industry, ignoreCase = true)) {
        return false
      }
    }

    // Location match
    if (alert.location.isNotBlank() && alert.location != "All") {
      val loc = alert.location.trim()
      val isMatch = job.location.contains(loc, ignoreCase = true) ||
          (loc.equals("Remote", ignoreCase = true) && job.isRemote)
      if (!isMatch) return false
    }

    // Experience match
    if (alert.experienceLevel.isNotBlank() && alert.experienceLevel != "All") {
      if (!job.experienceLevel.contains(alert.experienceLevel, ignoreCase = true)) {
        return false
      }
    }

    // Min salary
    if (alert.minSalary > 0 && job.salaryMax > 0 && job.salaryMax < alert.minSalary) {
      return false
    }

    return true
  }

  private suspend fun dispatchAlertDelivery(
    alert: JobAlertEntity,
    job: JobEntity,
    user: UserProfileEntity
  ) {
    val channel = when {
      alert.pushAlerts && alert.emailAlerts -> "BOTH"
      alert.emailAlerts -> "EMAIL"
      else -> "PUSH"
    }

    // 1. Send real system push notification if enabled
    if (alert.pushAlerts) {
      try {
        NotificationHelper.sendJobAlertNotification(context, alert.title, job)
      } catch (e: Exception) {
        // Ignored or logged
      }
    }

    // 2. Log in Notification records (stores simulated email & push dispatch)
    val record = NotificationRecordEntity(
      userId = user.id,
      jobId = job.id,
      jobTitle = job.title,
      company = job.company,
      alertTitle = alert.title,
      channelType = channel,
      recipientEmail = user.email,
      message = "Alert '${alert.title}': New opening for '${job.title}' at ${job.company} (${job.location}, ${job.salaryText}). " +
          if (alert.emailAlerts) "Email dispatched to ${user.email}." else "Push notification delivered."
    )
    notifDao.insertNotification(record)

    // Update alert lastTriggeredMillis
    alertDao.updateAlert(alert.copy(lastTriggeredMillis = System.currentTimeMillis()))
  }

  // Applications
  fun getApplicationsForUser(userId: Long): Flow<List<JobApplicationEntity>> =
    appDao.getApplicationsForUser(userId)

  fun getApplicationsForJob(jobId: Long): Flow<List<JobApplicationEntity>> =
    appDao.getApplicationsForJob(jobId)

  fun getAllApplications(): Flow<List<JobApplicationEntity>> =
    appDao.getAllApplications()

  fun getApplication(jobId: Long, userId: Long): Flow<JobApplicationEntity?> =
    appDao.getApplication(jobId, userId)

  suspend fun submitApplication(app: JobApplicationEntity): Long = withContext(Dispatchers.IO) {
    val id = appDao.insertApplication(app)
    jobDao.incrementApplicantCount(app.jobId)
    id
  }

  suspend fun updateApplicationStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
    appDao.updateApplicationStatus(id, status)
  }

  // Saved Jobs
  fun getSavedJobsForUser(userId: Long): Flow<List<SavedJobEntity>> =
    savedJobDao.getSavedJobsForUser(userId)

  fun isJobSaved(jobId: Long, userId: Long): Flow<Boolean> =
    savedJobDao.isJobSaved(jobId, userId)

  suspend fun toggleSaveJob(jobId: Long, userId: Long, currentlySaved: Boolean) = withContext(Dispatchers.IO) {
    if (currentlySaved) {
      savedJobDao.deleteSavedJob(jobId, userId)
    } else {
      savedJobDao.insertSavedJob(SavedJobEntity(jobId = jobId, userId = userId))
    }
  }

  // Notifications
  fun getNotifications(userId: Long): Flow<List<NotificationRecordEntity>> =
    notifDao.getNotificationsForUser(userId)

  fun getUnreadNotifCount(userId: Long): Flow<Int> =
    notifDao.getUnreadCount(userId)

  suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
    notifDao.markAsRead(id)
  }

  suspend fun clearNotifications(userId: Long) = withContext(Dispatchers.IO) {
    notifDao.clearAll(userId)
  }

  // JobListing Local Storage
  val allJobListings: Flow<List<JobListingEntity>> = jobListingDao.getAllJobListings()

  fun getJobListingsByIndustry(industry: String): Flow<List<JobListingEntity>> =
    jobListingDao.getJobListingsByIndustry(industry)

  fun getJobListingsByLocation(location: String): Flow<List<JobListingEntity>> =
    jobListingDao.getJobListingsByLocation(location)

  fun getJobListingsByExperience(experienceLevel: String): Flow<List<JobListingEntity>> =
    jobListingDao.getJobListingsByExperience(experienceLevel)

  suspend fun saveJobListing(listing: JobListingEntity): Long = withContext(Dispatchers.IO) {
    jobListingDao.insertJobListing(listing)
  }

  suspend fun deleteJobListing(listing: JobListingEntity) = withContext(Dispatchers.IO) {
    jobListingDao.deleteJobListing(listing)
  }

  suspend fun refreshJobListings(): Int = withContext(Dispatchers.IO) {
    if (jobListingDao.getJobListingCount() == 0) {
      seedDatabaseIfEmpty()
    }
    // Fetch live job postings from Firestore and upsert
    val fetchResult = FirestoreJobSyncService.fetchCloudJobs()
    fetchResult.onSuccess { cloudJobs ->
      for (cloudJob in cloudJobs) {
        upsertCloudJobLocally(cloudJob)
      }
    }
    jobListingDao.getJobListingCount()
  }
}
