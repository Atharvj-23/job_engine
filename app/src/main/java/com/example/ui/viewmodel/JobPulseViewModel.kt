package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.JobPulseDatabase
import com.example.data.JobPulseRepository
import com.example.data.SampleData
import com.example.data.model.JobAlertEntity
import com.example.data.model.JobApplicationEntity
import com.example.data.model.JobEntity
import com.example.data.model.NotificationRecordEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JobFilterCriteria(
  val query: String = "",
  val industry: String = "All",
  val location: String = "All",
  val experience: String = "All",
  val jobType: String = "All",
  val onlyRemote: Boolean = false,
  val minSalary: Int = 0
)

class JobPulseViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: JobPulseRepository

  init {
    val database = JobPulseDatabase.getDatabase(application)
    repository = JobPulseRepository(database, application)
  }

  // Active User Profile
  private val _currentUserId = MutableStateFlow<Long>(1L)
  val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

  val allUsers: StateFlow<List<UserProfileEntity>> = repository.allUsers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentUser: StateFlow<UserProfileEntity?> = _currentUserId
    .combine(repository.allUsers) { id, users ->
      users.find { it.id == id } ?: users.firstOrNull() ?: SampleData.defaultSeeker
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.defaultSeeker)

  fun switchUser(userId: Long) {
    _currentUserId.value = userId
  }

  fun updateCurrentUserProfile(profile: UserProfileEntity) {
    viewModelScope.launch {
      repository.saveUserProfile(profile)
      showSnackbar("Profile updated successfully")
    }
  }

  // Search & Filter State
  private val _filterCriteria = MutableStateFlow(JobFilterCriteria())
  val filterCriteria: StateFlow<JobFilterCriteria> = _filterCriteria.asStateFlow()

  val searchQuery: StateFlow<String> = _filterCriteria.map { it.query }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

  val selectedIndustry: StateFlow<String> = _filterCriteria.map { it.industry }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")

  val selectedLocation: StateFlow<String> = _filterCriteria.map { it.location }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")

  val selectedExperience: StateFlow<String> = _filterCriteria.map { it.experience }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")

  val selectedJobType: StateFlow<String> = _filterCriteria.map { it.jobType }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "All")

  val onlyRemote: StateFlow<Boolean> = _filterCriteria.map { it.onlyRemote }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val minSalary: StateFlow<Int> = _filterCriteria.map { it.minSalary }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  fun setSearchQuery(query: String) {
    _filterCriteria.update { it.copy(query = query) }
  }

  fun setIndustry(industry: String) {
    _filterCriteria.update { it.copy(industry = industry) }
  }

  fun setLocation(location: String) {
    _filterCriteria.update { it.copy(location = location) }
  }

  fun setExperience(exp: String) {
    _filterCriteria.update { it.copy(experience = exp) }
  }

  fun setJobType(type: String) {
    _filterCriteria.update { it.copy(jobType = type) }
  }

  fun toggleOnlyRemote() {
    _filterCriteria.update { it.copy(onlyRemote = !it.onlyRemote) }
  }

  fun setMinSalary(amount: Int) {
    _filterCriteria.update { it.copy(minSalary = amount) }
  }

  fun clearAllFilters() {
    _filterCriteria.value = JobFilterCriteria()
  }

  val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Filtered Job Listings
  val filteredJobs: StateFlow<List<JobEntity>> = combine(
    repository.allJobs,
    _filterCriteria
  ) { jobs, filters ->
    jobs.filter { job ->
      if (!job.isActive) return@filter false

      // Query search
      val matchesQuery = filters.query.isBlank() ||
          job.title.contains(filters.query, ignoreCase = true) ||
          job.company.contains(filters.query, ignoreCase = true) ||
          job.industry.contains(filters.query, ignoreCase = true) ||
          job.location.contains(filters.query, ignoreCase = true) ||
          job.requirements.contains(filters.query, ignoreCase = true)

      // Industry filter
      val matchesIndustry = filters.industry == "All" || job.industry.equals(filters.industry, ignoreCase = true)

      // Location filter
      val matchesLocation = filters.location == "All" ||
          job.location.contains(filters.location, ignoreCase = true) ||
          (filters.location.equals("Remote", ignoreCase = true) && job.isRemote)

      // Experience filter
      val matchesExperience = filters.experience == "All" ||
          job.experienceLevel.contains(filters.experience, ignoreCase = true)

      // Job Type filter
      val matchesType = filters.jobType == "All" ||
          job.jobType.equals(filters.jobType, ignoreCase = true)

      // Remote only filter
      val matchesRemote = !filters.onlyRemote || job.isRemote

      // Min Salary filter
      val matchesSalary = filters.minSalary <= 0 || (job.salaryMax >= filters.minSalary || job.salaryMin >= filters.minSalary)

      matchesQuery && matchesIndustry && matchesLocation && matchesExperience && matchesType && matchesRemote && matchesSalary
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Active saved jobs state flow
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val currentSavedJobIds: StateFlow<Set<Long>> = _currentUserId
    .flatMapLatest { userId ->
      repository.getSavedJobsForUser(userId).map { list -> list.map { it.jobId }.toSet() }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  fun toggleSaveJob(jobId: Long, isCurrentlySaved: Boolean) {
    viewModelScope.launch {
      val userId = _currentUserId.value
      repository.toggleSaveJob(jobId, userId, isCurrentlySaved)
      showSnackbar(if (isCurrentlySaved) "Job removed from saved" else "Job saved to your bookmarks")
    }
  }

  // User Applications
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val userApplications: StateFlow<List<JobApplicationEntity>> = _currentUserId
    .flatMapLatest { userId ->
      repository.getApplicationsForUser(userId)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val appliedJobIds: StateFlow<Set<Long>> = userApplications
    .map { list -> list.map { it.jobId }.toSet() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

  fun applyToJob(jobId: Long, resumeNote: String) {
    viewModelScope.launch {
      val user = currentUser.value ?: SampleData.defaultSeeker
      val application = JobApplicationEntity(
        jobId = jobId,
        applicantId = user.id,
        applicantName = user.fullName,
        applicantEmail = user.email,
        applicantHeadline = user.headline,
        resumeNote = resumeNote,
        status = "APPLIED"
      )
      repository.submitApplication(application)
      showSnackbar("Application submitted successfully!")
    }
  }

  // Job Alerts
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val userAlerts: StateFlow<List<JobAlertEntity>> = _currentUserId
    .flatMapLatest { userId ->
      repository.getAlertsForUser(userId)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun saveJobAlert(
    title: String,
    industry: String,
    location: String,
    experienceLevel: String,
    minSalary: Int,
    emailAlerts: Boolean,
    pushAlerts: Boolean,
    existingId: Long = 0L
  ) {
    viewModelScope.launch {
      val userId = _currentUserId.value
      val alert = JobAlertEntity(
        id = existingId,
        userId = userId,
        title = title.ifBlank { "$industry Jobs Alert" },
        industry = industry,
        location = location,
        experienceLevel = experienceLevel,
        minSalary = minSalary,
        emailAlerts = emailAlerts,
        pushAlerts = pushAlerts,
        isActive = true
      )
      repository.saveAlert(alert)
      showSnackbar("Job alert '${alert.title}' saved! Scanning for matches...")
    }
  }

  fun toggleAlertActive(alert: JobAlertEntity) {
    viewModelScope.launch {
      repository.saveAlert(alert.copy(isActive = !alert.isActive))
    }
  }

  fun deleteAlert(alert: JobAlertEntity) {
    viewModelScope.launch {
      repository.deleteAlert(alert)
      showSnackbar("Alert removed")
    }
  }

  fun triggerManualAlertCheck() {
    viewModelScope.launch {
      val userId = _currentUserId.value
      val matches = repository.triggerAlertCheckManual(userId)
      if (matches > 0) {
        showSnackbar("Found $matches matching openings! Alerts sent via Email & Push.")
      } else {
        showSnackbar("No new matching jobs found for your active alerts.")
      }
    }
  }

  // Employer Post Job & Hub
  val employerPostedJobs: StateFlow<List<JobEntity>> = combine(
    repository.allJobs,
    currentUser
  ) { jobs, user ->
    if (user != null && user.role == "EMPLOYER") {
      jobs.filter { it.employerEmail.equals(user.email, ignoreCase = true) || it.company.equals(user.companyName, ignoreCase = true) }
    } else {
      emptyList()
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allApplicantsForEmployer: StateFlow<List<JobApplicationEntity>> = repository.getAllApplications()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun postNewJob(
    title: String,
    company: String,
    industry: String,
    location: String,
    isRemote: Boolean,
    jobType: String,
    experienceLevel: String,
    salaryMin: Int,
    salaryMax: Int,
    description: String,
    responsibilities: String,
    requirements: String,
    benefits: String,
    employerEmail: String
  ) {
    viewModelScope.launch {
      val salaryText = if (salaryMin > 0 && salaryMax > 0) {
        "$$salaryMin - $$salaryMax / yr"
      } else if (salaryMin > 0) {
        "$$salaryMin+ / yr"
      } else {
        "Competitive Salary"
      }

      val newJob = JobEntity(
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
        responsibilities = responsibilities,
        requirements = requirements,
        benefits = benefits,
        employerEmail = employerEmail,
        isActive = true
      )

      val activeEmail = currentUser.value?.email ?: employerEmail
      repository.postJob(newJob, activeEmail)
      showSnackbar("Job opening '$title' published! Matching seekers alerted.")
    }
  }

  fun updateApplicantStatus(applicationId: Long, newStatus: String) {
    viewModelScope.launch {
      repository.updateApplicationStatus(applicationId, newStatus)
      showSnackbar("Applicant status updated to $newStatus")
    }
  }

  fun toggleJobActive(job: JobEntity) {
    viewModelScope.launch {
      repository.updateJob(job.copy(isActive = !job.isActive))
      showSnackbar(if (job.isActive) "Job paused" else "Job re-opened")
    }
  }

  fun deleteJob(job: JobEntity) {
    viewModelScope.launch {
      repository.deleteJob(job)
      showSnackbar("Job removed")
    }
  }

  // Notifications
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val notifications: StateFlow<List<NotificationRecordEntity>> = _currentUserId
    .flatMapLatest { userId ->
      repository.getNotifications(userId)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val unreadNotifCount: StateFlow<Int> = _currentUserId
    .flatMapLatest { userId ->
      repository.getUnreadNotifCount(userId)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  fun markNotificationAsRead(id: Long) {
    viewModelScope.launch {
      repository.markNotificationAsRead(id)
    }
  }

  fun clearAllNotifications() {
    viewModelScope.launch {
      repository.clearNotifications(_currentUserId.value)
      showSnackbar("All notifications cleared")
    }
  }

  // Selected Detail State
  private val _selectedJob = MutableStateFlow<JobEntity?>(null)
  val selectedJob: StateFlow<JobEntity?> = _selectedJob.asStateFlow()

  fun selectJob(job: JobEntity?) {
    _selectedJob.value = job
  }

  // Email Preview Modal
  private val _previewEmail = MutableStateFlow<NotificationRecordEntity?>(null)
  val previewEmail: StateFlow<NotificationRecordEntity?> = _previewEmail.asStateFlow()

  fun showEmailPreview(notification: NotificationRecordEntity?) {
    _previewEmail.value = notification
  }

  // Snackbar
  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  fun showSnackbar(message: String) {
    _snackbarMessage.value = message
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  fun syncWithCloud() {
    viewModelScope.launch {
      try {
        repository.syncJobsWithCloud()
        showSnackbar("Cloud storage sync completed!")
      } catch (e: Exception) {
        showSnackbar("Cloud sync check completed")
      }
    }
  }
}
