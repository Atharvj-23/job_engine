package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.FirebaseAuthManager
import com.example.auth.GoogleUserSession
import com.example.service.FirebaseJobMessagingManager
import com.example.data.JobPulseDatabase
import com.example.data.JobPulseRepository
import com.example.data.SampleData
import com.example.data.cloud.FirestoreJobSyncService
import com.example.data.model.JobAlertEntity
import com.example.data.model.JobApplicationEntity
import com.example.data.model.JobEntity
import com.example.data.model.JobListing
import com.example.data.model.JobListingEntity
import com.example.data.model.NotificationRecordEntity
import com.example.data.model.UserProfileEntity
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * JobViewModel:
 * Uses the Room database to fetch job listings and exposes them as StateFlows for the UI layer.
 * Allows real-time filtering of job listings by title or company name via a search query.
 * Manages job alerts configured for user-selected job industries/departments (not for a specific job).
 */
class JobViewModel(application: Application) : AndroidViewModel(application) {
  private val database = JobPulseDatabase.getDatabase(application)
  private val jobDao = database.jobDao()
  private val jobListingDao = database.jobListingDao()
  private val alertDao = database.jobAlertDao()
  private val repository = JobPulseRepository(database, application)
  private val authPrefs = application.getSharedPreferences("job_engine_auth_prefs", Context.MODE_PRIVATE)

  // Authentication State: Tracks whether user has completed login
  private val _isUserLoggedIn = MutableStateFlow(authPrefs.getBoolean("is_logged_in", false))
  val isUserLoggedIn: StateFlow<Boolean> = _isUserLoggedIn.asStateFlow()

  // Real-time search query for title and company filtering
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  // Selected Industry / Department filter
  private val _selectedIndustry = MutableStateFlow("All")
  val selectedIndustry: StateFlow<String> = _selectedIndustry.asStateFlow()

  // Additional filter states
  private val _selectedLocation = MutableStateFlow("All")
  val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

  private val _selectedExperience = MutableStateFlow("All")
  val selectedExperience: StateFlow<String> = _selectedExperience.asStateFlow()

  private val _selectedJobType = MutableStateFlow("All")
  val selectedJobType: StateFlow<String> = _selectedJobType.asStateFlow()

  private val _onlyRemote = MutableStateFlow(false)
  val onlyRemote: StateFlow<Boolean> = _onlyRemote.asStateFlow()

  private val _minSalary = MutableStateFlow(0)
  val minSalary: StateFlow<Int> = _minSalary.asStateFlow()

  // Active User Profile
  private val _currentUserId = MutableStateFlow<Long>(authPrefs.getLong("active_user_id", 1L))
  val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

  fun loginAsUser(
    role: String,
    email: String,
    fullName: String = "",
    companyName: String = ""
  ) {
    viewModelScope.launch {
      val existing = allUsers.value.find { it.email.equals(email, ignoreCase = true) }
      val targetId: Long = if (existing != null) {
        val updated = existing.copy(
          role = role,
          fullName = fullName.ifBlank { existing.fullName },
          companyName = if (role == "EMPLOYER") companyName.ifBlank { existing.companyName.ifBlank { "NovaVanguard Health" } } else ""
        )
        repository.saveUserProfile(updated)
        existing.id
      } else {
        val newProfile = UserProfileEntity(
          email = email,
          fullName = fullName.ifBlank { if (role == "EMPLOYER") "Hiring Manager" else "Job Seeker" },
          role = role,
          headline = if (role == "EMPLOYER") "Talent Acquisition & Hiring Director" else "Healthcare & Technology Specialist",
          industry = "Engineering",
          location = "Boston, MA",
          companyName = if (role == "EMPLOYER") companyName.ifBlank { "NovaVanguard Health" } else "",
          skills = if (role == "EMPLOYER") "Recruiting, Hiring, Engineering" else "Kotlin, Java, Healthcare"
        )
        repository.saveUserProfile(newProfile)
      }
      _currentUserId.value = targetId
      authPrefs.edit().putBoolean("is_logged_in", true).putLong("active_user_id", targetId).apply()
      _isUserLoggedIn.value = true
      showSnackbar("Signed in as ${if (role == "EMPLOYER") "Employer" else "Job Seeker"}")
    }
  }

  fun loginDemoSeeker() {
    viewModelScope.launch {
      _currentUserId.value = 1L
      authPrefs.edit().putBoolean("is_logged_in", true).putLong("active_user_id", 1L).apply()
      _isUserLoggedIn.value = true
      showSnackbar("Logged in as Job Seeker (Alex Morgan)")
    }
  }

  fun loginDemoEmployer() {
    viewModelScope.launch {
      _currentUserId.value = 2L
      authPrefs.edit().putBoolean("is_logged_in", true).putLong("active_user_id", 2L).apply()
      _isUserLoggedIn.value = true
      showSnackbar("Logged in as Employer (Dr. Sarah Chen, NovaVanguard)")
    }
  }

  fun loginDemoAdmin() {
    viewModelScope.launch {
      _currentUserId.value = 3L
      authPrefs.edit().putBoolean("is_logged_in", true).putLong("active_user_id", 3L).apply()
      _isUserLoggedIn.value = true
      showSnackbar("Logged in as Administrator (Full System Control)")
    }
  }

  fun logout() {
    viewModelScope.launch {
      FirebaseAuthManager.signOut()
      authPrefs.edit().putBoolean("is_logged_in", false).apply()
      _isUserLoggedIn.value = false
      showSnackbar("Signed out successfully")
    }
  }

  val allUsers: StateFlow<List<UserProfileEntity>> = repository.allUsers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val currentUser: StateFlow<UserProfileEntity?> = _currentUserId
    .combine(repository.allUsers) { id, users ->
      users.find { it.id == id } ?: users.firstOrNull() ?: SampleData.defaultSeeker
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.defaultSeeker)

  // Raw Job Listings from Room Database
  val allJobs: StateFlow<List<JobEntity>> = jobDao.getAllJobs()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allJobListings: StateFlow<List<JobListing>> = jobListingDao.getAllJobListings()
    .map { list -> list.map { it.toJobListing() } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allJobListingEntities: StateFlow<List<JobListingEntity>> = jobListingDao.getAllJobListings()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Pull-to-refresh state
  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  fun refreshJobListings() {
    viewModelScope.launch {
      _isRefreshing.value = true
      try {
        kotlinx.coroutines.delay(800)
        val count = repository.refreshJobListings()
        showSnackbar("Job Feed updated ($count listings available)")
      } catch (e: Exception) {
        showSnackbar("Job Feed updated")
      } finally {
        _isRefreshing.value = false
      }
    }
  }

  // Real-time filtered JobListingEntities based on keywords, category, location, and remote-only
  val filteredJobListings: StateFlow<List<JobListingEntity>> = combine(
    allJobListingEntities,
    _searchQuery,
    _selectedIndustry,
    _selectedLocation,
    _onlyRemote
  ) { args: Array<Any> ->
    @Suppress("UNCHECKED_CAST")
    val listings = args[0] as List<JobListingEntity>
    val query = (args[1] as String).trim()
    val industry = args[2] as String
    val location = args[3] as String
    val onlyRemote = args[4] as Boolean

    listings.filter { listing ->
      // 1. Keywords filtering across title, company, requirements, and description
      val matchesKeywords = query.isEmpty() ||
          listing.title.contains(query, ignoreCase = true) ||
          listing.company.contains(query, ignoreCase = true) ||
          listing.requirements.contains(query, ignoreCase = true) ||
          listing.description.contains(query, ignoreCase = true)

      // 2. Category / Industry filtering
      val matchesCategory = industry == "All" || listing.industry.equals(industry, ignoreCase = true)

      // 3. Location filtering
      val matchesLocation = location == "All" ||
          listing.location.contains(location, ignoreCase = true) ||
          (location.equals("Remote", ignoreCase = true) && listing.isRemote)

      // 4. Remote only filter
      val matchesRemote = !onlyRemote || listing.isRemote

      matchesKeywords && matchesCategory && matchesLocation && matchesRemote
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Firebase Cloud Messaging Notification Toggle & Topics
  val fcmNotificationsEnabled: StateFlow<Boolean> = FirebaseJobMessagingManager.fcmEnabled
  val fcmSubscribedTopics: StateFlow<Set<String>> = FirebaseJobMessagingManager.subscribedTopics
  val fcmToken: StateFlow<String?> = FirebaseJobMessagingManager.fcmToken

  fun toggleFcmNotifications(context: Context) {
    val newStatus = !fcmNotificationsEnabled.value
    val criteria = listOf(_selectedIndustry.value, _selectedLocation.value).filter { it != "All" }.ifEmpty {
      listOf("Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")
    }
    FirebaseJobMessagingManager.setFcmEnabled(context, newStatus, criteria)
    if (newStatus) {
      showSnackbar("Firebase job push alerts enabled for criteria")
    } else {
      showSnackbar("Firebase job push alerts disabled")
    }
  }

  fun setFcmNotifications(context: Context, enabled: Boolean) {
    val criteria = listOf(_selectedIndustry.value, _selectedLocation.value).filter { it != "All" }.ifEmpty {
      listOf("Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")
    }
    FirebaseJobMessagingManager.setFcmEnabled(context, enabled, criteria)
    showSnackbar(if (enabled) "Firebase alerts enabled for criteria" else "Firebase alerts paused")
  }

  // Selected JobListing for the detailed view component
  private val _selectedJobListing = MutableStateFlow<JobListingEntity?>(null)
  val selectedJobListing: StateFlow<JobListingEntity?> = _selectedJobListing.asStateFlow()

  fun selectJobListing(listing: JobListingEntity?) {
    _selectedJobListing.value = listing
  }

  // Firebase & Google Auth integration
  val firebaseUser: StateFlow<FirebaseUser?> = FirebaseAuthManager.currentUserState
  val googleSession: StateFlow<GoogleUserSession?> = FirebaseAuthManager.googleUserSession

  fun getFirebaseUser(): FirebaseUser? = FirebaseAuthManager.getCurrentUser()

  fun signOutFirebaseAuth() {
    FirebaseAuthManager.signOut()
    showSnackbar("Signed out successfully")
  }

  suspend fun signInWithGoogle(
    context: Context,
    email: String = "atharvajirage4@gmail.com"
  ): Result<GoogleUserSession> {
    val res = FirebaseAuthManager.signInWithGoogle(context, email)
    res.onSuccess { session ->
      showSnackbar("Signed in with Google: ${session.email}")
      currentUser.value?.let { currentProf ->
        updateCurrentUserProfile(
          currentProf.copy(
            email = session.email,
            fullName = session.displayName
          )
        )
      }
    }.onFailure { err ->
      showSnackbar("Google sign-in error: ${err.message}")
    }
    return res
  }

  suspend fun signInWithEmailPassword(email: String, pass: String): Result<FirebaseUser> {
    val res = FirebaseAuthManager.signInWithEmailPassword(email, pass)
    res.onSuccess { user ->
      showSnackbar("Signed in as ${user.email}")
    }.onFailure { err ->
      showSnackbar("Sign-in error: ${err.message}")
    }
    return res
  }

  suspend fun registerWithEmailPassword(email: String, pass: String): Result<FirebaseUser> {
    val res = FirebaseAuthManager.registerWithEmailPassword(email, pass)
    res.onSuccess { user ->
      showSnackbar("Account created for ${user.email}")
    }.onFailure { err ->
      showSnackbar("Registration error: ${err.message}")
    }
    return res
  }

  // Real-time Filtered Job Listings by Title or Company Name & Industry
  val filteredJobs: StateFlow<List<JobEntity>> = combine(
    jobDao.getActiveJobs(),
    _searchQuery,
    _selectedIndustry,
    _selectedLocation,
    _selectedExperience,
    _selectedJobType,
    _onlyRemote,
    _minSalary
  ) { args: Array<Any> ->
    @Suppress("UNCHECKED_CAST")
    val jobs = args[0] as List<JobEntity>
    val query = (args[1] as String).trim()
    val industry = args[2] as String
    val location = args[3] as String
    val experience = args[4] as String
    val jobType = args[5] as String
    val remoteOnly = args[6] as Boolean
    val minSal = args[7] as Int

    jobs.filter { job ->
      // Filter by title or company name in real-time
      val matchesTitleOrCompany = query.isEmpty() ||
          job.title.contains(query, ignoreCase = true) ||
          job.company.contains(query, ignoreCase = true) ||
          job.requirements.contains(query, ignoreCase = true)

      // Filter by user selected industry or department
      val matchesIndustry = industry == "All" || job.industry.equals(industry, ignoreCase = true)

      // Location filter
      val matchesLocation = location == "All" ||
          job.location.contains(location, ignoreCase = true) ||
          (location.equals("Remote", ignoreCase = true) && job.isRemote)

      // Experience level filter
      val matchesExperience = experience == "All" ||
          job.experienceLevel.contains(experience, ignoreCase = true)

      // Job type filter
      val matchesType = jobType == "All" || job.jobType.equals(jobType, ignoreCase = true)

      // Remote filter
      val matchesRemote = !remoteOnly || job.isRemote

      // Min Salary filter
      val matchesSalary = minSal <= 0 || (job.salaryMax >= minSal || job.salaryMin >= minSal)

      matchesTitleOrCompany && matchesIndustry && matchesLocation && matchesExperience && matchesType && matchesRemote && matchesSalary
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Real-time search updater
  fun onSearchQueryChange(query: String) {
    _searchQuery.value = query
  }

  fun setSearchQuery(query: String) = onSearchQueryChange(query)

  // Selected Industry/Department updater
  fun onIndustrySelected(industry: String) {
    _selectedIndustry.value = industry
  }

  fun setIndustry(industry: String) = onIndustrySelected(industry)

  fun setLocation(loc: String) {
    _selectedLocation.value = loc
  }

  fun setExperience(exp: String) {
    _selectedExperience.value = exp
  }

  fun setJobType(type: String) {
    _selectedJobType.value = type
  }

  fun toggleOnlyRemote() {
    _onlyRemote.value = !_onlyRemote.value
  }

  fun setMinSalary(amount: Int) {
    _minSalary.value = amount
  }

  fun clearAllFilters() {
    _searchQuery.value = ""
    _selectedIndustry.value = "All"
    _selectedLocation.value = "All"
    _selectedExperience.value = "All"
    _selectedJobType.value = "All"
    _onlyRemote.value = false
    _minSalary.value = 0
  }

  // Bookmarked Saved Jobs
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
      showSnackbar(if (isCurrentlySaved) "Job removed from bookmarks" else "Job saved to bookmarks")
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

  // ==========================================
  // JOB ALERTS FOR USER SELECTED INDUSTRY/DEPT
  // (Not tied to any specific job, but to the industry / department)
  // ==========================================
  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val userAlerts: StateFlow<List<JobAlertEntity>> = _currentUserId
    .flatMapLatest { userId ->
      alertDao.getAlertsForUser(userId)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  /**
   * Creates or updates a job alert for a chosen industry / department.
   * The alert triggers whenever any new job opening in that industry/department is posted.
   */
  fun saveIndustryJobAlert(
    industryOrDepartment: String,
    title: String = "",
    location: String = "All",
    experienceLevel: String = "All",
    minSalary: Int = 0,
    emailAlerts: Boolean = true,
    pushAlerts: Boolean = true
  ) {
    viewModelScope.launch {
      val userId = _currentUserId.value
      val alertName = title.ifBlank {
        if (industryOrDepartment == "All") "All Career Openings Alert"
        else "$industryOrDepartment Department Alerts"
      }
      val alert = JobAlertEntity(
        userId = userId,
        title = alertName,
        industry = industryOrDepartment, // Target industry/department
        location = location,
        experienceLevel = experienceLevel,
        minSalary = minSalary,
        emailAlerts = emailAlerts,
        pushAlerts = pushAlerts,
        isActive = true
      )
      repository.saveAlert(alert)
      showSnackbar("Subscribed to alerts for $industryOrDepartment department! Scanning matching openings...")
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
      showSnackbar("Industry alert removed")
    }
  }

  fun triggerManualAlertCheck() {
    viewModelScope.launch {
      val userId = _currentUserId.value
      val matches = repository.triggerAlertCheckManual(userId)
      if (matches > 0) {
        showSnackbar("Found $matches openings matching your industry alerts! Delivered via Email & Push.")
      } else {
        showSnackbar("No new openings found for your selected industry alerts.")
      }
    }
  }

  // Employer Posted Jobs & Applicant Management
  val employerPostedJobs: StateFlow<List<JobEntity>> = combine(
    jobDao.getAllJobs(),
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
      showSnackbar("Opening '$title' posted in $industry department!")
    }
  }

  suspend fun postJobToFirestore(
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
  ): Result<String> {
    val salaryText = if (salaryMin > 0 && salaryMax > 0) {
      "$$salaryMin - $$salaryMax / yr"
    } else if (salaryMin > 0) {
      "$$salaryMin+ / yr"
    } else {
      "Competitive Salary"
    }

    val jobEntity = JobEntity(
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
      isActive = true,
      postedDateMillis = System.currentTimeMillis()
    )

    // Save to Room Job table
    val localId = repository.postJob(jobEntity, employerEmail)

    // Also save to Room job_listings table for JobListing entities
    val listingEntity = JobListingEntity(
      id = localId,
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
      postedDateMillis = System.currentTimeMillis()
    )
    jobListingDao.insertJobListing(listingEntity)

    // Upload directly to Firestore collection
    val cloudUploadResult = FirestoreJobSyncService.uploadJobToCloud(jobEntity.copy(id = localId))
    cloudUploadResult.onSuccess { docId ->
      jobDao.updateCloudSyncStatus(localId, docId, JobEntity.SYNC_STATUS_SYNCED)
      showSnackbar("Opening '$title' saved to Firestore (Doc ID: ${docId.take(8)}...)")
    }.onFailure { e ->
      showSnackbar("Saved to local database. Firestore sync queued: ${e.message ?: "Offline"}")
    }

    return cloudUploadResult
  }

  suspend fun updateEmployerJob(
    jobId: Long,
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
    isActive: Boolean
  ): Result<Unit> {
    return try {
      val salaryText = if (salaryMin > 0 && salaryMax > 0) {
        "$$salaryMin - $$salaryMax / yr"
      } else if (salaryMin > 0) {
        "$$salaryMin+ / yr"
      } else {
        "Competitive Salary"
      }
      val existing = allJobs.value.find { it.id == jobId }
      if (existing != null) {
        val updated = existing.copy(
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
          isActive = isActive,
          updatedAtMillis = System.currentTimeMillis()
        )
        jobDao.updateJob(updated)
      }
      val existingListing = allJobListingEntities.value.find { it.id == jobId }
      if (existingListing != null) {
        val updatedListing = existingListing.copy(
          title = title,
          company = company,
          industry = industry,
          location = location,
          isRemote = isRemote,
          jobType = jobType,
          experienceLevel = experienceLevel,
          salaryText = salaryText,
          description = description,
          requirements = requirements
        )
        jobListingDao.updateJobListing(updatedListing)
      }
      showSnackbar("Opening '$title' updated successfully!")
      Result.success(Unit)
    } catch (e: Exception) {
      showSnackbar("Failed to update job: ${e.message}")
      Result.failure(e)
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

  // Delivered Notification Dispatches
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

  // User Profile
  fun switchUser(userId: Long) {
    _currentUserId.value = userId
  }

  fun updateCurrentUserProfile(profile: UserProfileEntity) {
    viewModelScope.launch {
      repository.saveUserProfile(profile)
      showSnackbar("Profile updated successfully")
    }
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

  // AI Resume & ATS Score Analysis
  private val _isAnalyzingResume = MutableStateFlow(false)
  val isAnalyzingResume: StateFlow<Boolean> = _isAnalyzingResume.asStateFlow()

  private val _atsAnalysisResult = MutableStateFlow<com.example.ai.AtsAnalysisResult?>(null)
  val atsAnalysisResult: StateFlow<com.example.ai.AtsAnalysisResult?> = _atsAnalysisResult.asStateFlow()

  fun runAtsResumeAnalysis(
    resumeText: String,
    targetIndustry: String,
    targetRole: String = ""
  ) {
    viewModelScope.launch {
      _isAnalyzingResume.value = true
      try {
        val skills = currentUser.value?.skills ?: ""
        val result = com.example.ai.AiResumeAtsService.analyzeResume(
          resumeText = resumeText,
          targetIndustry = targetIndustry,
          targetRole = targetRole,
          currentSkills = skills
        )
        _atsAnalysisResult.value = result

        // Persist to user profile
        currentUser.value?.let { currentProf ->
          val updated = currentProf.copy(
            fullResumeText = resumeText,
            atsScore = result.overallScore,
            atsAnalysisJson = com.example.ai.AiResumeAtsService.toJson(result)
          )
          repository.saveUserProfile(updated)
        }
        showSnackbar("AI Resume Analysis Complete! ATS Score: ${result.overallScore}/100")
      } catch (e: Exception) {
        showSnackbar("Error analyzing resume: ${e.message}")
      } finally {
        _isAnalyzingResume.value = false
      }
    }
  }

  init {
    FirebaseJobMessagingManager.init(application)
    viewModelScope.launch {
      currentUser.collect { user ->
        if (user != null && user.atsAnalysisJson.isNotBlank() && _atsAnalysisResult.value == null) {
          _atsAnalysisResult.value = com.example.ai.AiResumeAtsService.parseAtsJson(user.atsAnalysisJson)
        }
      }
    }
  }
}
