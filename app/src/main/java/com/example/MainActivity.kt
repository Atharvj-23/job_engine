package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.SampleData
import com.example.data.model.JobEntity
import com.example.data.model.JobListingEntity
import com.example.service.NotificationHelper
import com.example.ui.components.ApplyJobDialog
import com.example.ui.components.CreateAlertDialog
import com.example.ui.components.CreateJobDialog
import com.example.ui.components.EmailPreviewDialog
import com.example.ui.components.JobFilterBottomSheet
import com.example.ui.screens.CareerChatbotScreen
import com.example.ui.screens.EmployerHubScreen
import com.example.ui.screens.EmployerPostJobScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JobAlertsScreen
import com.example.ui.screens.JobDetailScreen
import com.example.ui.screens.JobFeedScreen
import com.example.ui.screens.JobListingDetailView
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JobViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    NotificationHelper.initChannel(this)

    val targetJobId = intent?.getLongExtra("TARGET_JOB_ID", -1L) ?: -1L

    setContent {
      MyApplicationTheme {
        JobPulseApp(initialTargetJobId = targetJobId)
      }
    }
  }
}

@Composable
fun JobPulseApp(
  initialTargetJobId: Long = -1L,
  viewModel: JobViewModel = viewModel()
) {
  val context = LocalContext.current
  val snackbarHostState = remember { SnackbarHostState() }

  // Observed State from Room / ViewModel
  val allJobs by viewModel.allJobs.collectAsStateWithLifecycle()
  val filteredJobs by viewModel.filteredJobs.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedIndustry by viewModel.selectedIndustry.collectAsStateWithLifecycle()
  val selectedLocation by viewModel.selectedLocation.collectAsStateWithLifecycle()
  val selectedExperience by viewModel.selectedExperience.collectAsStateWithLifecycle()
  val selectedJobType by viewModel.selectedJobType.collectAsStateWithLifecycle()
  val onlyRemote by viewModel.onlyRemote.collectAsStateWithLifecycle()
  val minSalary by viewModel.minSalary.collectAsStateWithLifecycle()

  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()
  val savedJobIds by viewModel.currentSavedJobIds.collectAsStateWithLifecycle()
  val appliedJobIds by viewModel.appliedJobIds.collectAsStateWithLifecycle()
  val userAlerts by viewModel.userAlerts.collectAsStateWithLifecycle()
  val userApplications by viewModel.userApplications.collectAsStateWithLifecycle()
  val employerPostedJobs by viewModel.employerPostedJobs.collectAsStateWithLifecycle()
  val allApplicantsForEmployer by viewModel.allApplicantsForEmployer.collectAsStateWithLifecycle()
  val notifications by viewModel.notifications.collectAsStateWithLifecycle()
  val unreadNotifCount by viewModel.unreadNotifCount.collectAsStateWithLifecycle()

  val selectedJob by viewModel.selectedJob.collectAsStateWithLifecycle()
  val selectedJobListing by viewModel.selectedJobListing.collectAsStateWithLifecycle()
  val previewEmail by viewModel.previewEmail.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

  // Navigation Screen State
  var currentTab by remember { mutableStateOf("feed") }
  var isDetailActive by remember { mutableStateOf(false) }
  var showEmployerPostJobScreen by remember { mutableStateOf(false) }
  var editingJob by remember { mutableStateOf<JobEntity?>(null) }

  // Dialog States
  var showFilterSheet by remember { mutableStateOf(false) }
  var applyingJob by remember { mutableStateOf<JobEntity?>(null) }
  var showCreateJobDialog by remember { mutableStateOf(false) }
  var showCreateAlertDialog by remember { mutableStateOf(false) }
  var alertTargetIndustry by remember { mutableStateOf("All") }

  // Notification Permission Launcher (Android 13+)
  val notifPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.showSnackbar("Notifications enabled for job alerts!")
    }
  }

  LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  // Handle Intent Extras (notification click deep-link)
  LaunchedEffect(initialTargetJobId, allJobs) {
    if (initialTargetJobId > 0 && allJobs.isNotEmpty()) {
      val foundJob = allJobs.find { it.id == initialTargetJobId }
      if (foundJob != null) {
        viewModel.selectJob(foundJob)
        isDetailActive = true
      }
    }
  }

  // Handle snackbar messages
  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  // Back button handling
  if (showEmployerPostJobScreen || editingJob != null) {
    BackHandler {
      editingJob = null
      showEmployerPostJobScreen = false
    }
  }

  if (selectedJobListing != null) {
    BackHandler {
      viewModel.selectJobListing(null)
    }
  }

  if (isDetailActive) {
    BackHandler {
      isDetailActive = false
      viewModel.selectJob(null)
    }
  }

  val activeUser = currentUser ?: SampleData.defaultSeeker
  val isEmployer = activeUser.role == "EMPLOYER" || activeUser.role == "ADMIN"
  val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()

  // First opening: Show Login & Role Selection Screen
  if (!isUserLoggedIn) {
    LoginScreen(
      viewModel = viewModel,
      onLoginSuccess = {
        currentTab = "feed"
      }
    )
    return
  }

  val isAnyOverlayActive = isDetailActive || selectedJobListing != null || showEmployerPostJobScreen

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (!isAnyOverlayActive) {
        NavigationBar(
          modifier = Modifier.testTag("main_bottom_nav")
        ) {
          NavigationBarItem(
            selected = currentTab == "feed",
            onClick = { currentTab = "feed" },
            icon = {
              Icon(
                if (currentTab == "feed") Icons.Default.DynamicFeed else Icons.Outlined.DynamicFeed,
                contentDescription = "Feed"
              )
            },
            label = { Text("Feed", fontWeight = if (currentTab == "feed") FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_feed")
          )

          NavigationBarItem(
            selected = currentTab == "jobs",
            onClick = { currentTab = "jobs" },
            icon = {
              Icon(
                if (currentTab == "jobs") Icons.Default.Work else Icons.Outlined.WorkOutline,
                contentDescription = "Explore"
              )
            },
            label = { Text("Explore", fontWeight = if (currentTab == "jobs") FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_jobs")
          )

          NavigationBarItem(
            selected = currentTab == "alerts",
            onClick = { currentTab = "alerts" },
            icon = {
              BadgedBox(
                badge = {
                  if (unreadNotifCount > 0) {
                    Badge { Text(unreadNotifCount.toString()) }
                  }
                }
              ) {
                Icon(
                  if (currentTab == "alerts") Icons.Default.NotificationsActive else Icons.Outlined.Notifications,
                  contentDescription = "Alerts"
                )
              }
            },
            label = { Text("Alerts", fontWeight = if (currentTab == "alerts") FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_alerts")
          )

          // AI Career Chatbot Tab
          NavigationBarItem(
            selected = currentTab == "chat",
            onClick = { currentTab = "chat" },
            icon = {
              Icon(
                if (currentTab == "chat") Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                contentDescription = "AI Coach"
              )
            },
            label = { Text("AI Coach", fontWeight = if (currentTab == "chat") FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_chat")
          )

          // Only show 'Post Job' tab if user is an Employer / Admin
          if (isEmployer) {
            NavigationBarItem(
              selected = currentTab == "employer",
              onClick = { currentTab = "employer" },
              icon = {
                Icon(
                  if (currentTab == "employer") Icons.Default.BusinessCenter else Icons.Outlined.BusinessCenter,
                  contentDescription = "Post Job"
                )
              },
              label = { Text("Post Job", fontWeight = if (currentTab == "employer") FontWeight.Bold else FontWeight.Normal) },
              modifier = Modifier.testTag("nav_item_employer")
            )
          }

          NavigationBarItem(
            selected = currentTab == "profile",
            onClick = { currentTab = "profile" },
            icon = {
              Icon(
                if (currentTab == "profile") Icons.Default.AccountCircle else Icons.Outlined.AccountCircle,
                contentDescription = "Profile"
              )
            },
            label = { Text("Profile", fontWeight = if (currentTab == "profile") FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("nav_item_profile")
          )
        }
      }
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      if (showEmployerPostJobScreen || editingJob != null) {
        EmployerPostJobScreen(
          viewModel = viewModel,
          jobToEdit = editingJob,
          onNavigateBack = {
            editingJob = null
            showEmployerPostJobScreen = false
          }
        )
      } else if (selectedJobListing != null) {
        val listing = selectedJobListing!!
        JobListingDetailView(
          jobListing = listing,
          isSaved = savedJobIds.contains(listing.id),
          isApplied = appliedJobIds.contains(listing.id),
          hasAlertForIndustry = userAlerts.any { it.industry.equals(listing.industry, ignoreCase = true) },
          onBack = { viewModel.selectJobListing(null) },
          onToggleSave = {
            viewModel.toggleSaveJob(listing.id, savedJobIds.contains(listing.id))
          },
          onSaveAlert = { industry ->
            viewModel.saveIndustryJobAlert(
              industryOrDepartment = industry,
              title = "$industry Career Alerts"
            )
          },
          onApply = {
            applyingJob = listing.toJobEntity()
          }
        )
      } else if (isDetailActive && selectedJob != null) {
        JobDetailScreen(
          job = selectedJob!!,
          isSaved = savedJobIds.contains(selectedJob!!.id),
          isApplied = appliedJobIds.contains(selectedJob!!.id),
          onBack = {
            isDetailActive = false
            viewModel.selectJob(null)
          },
          onToggleSave = {
            viewModel.toggleSaveJob(selectedJob!!.id, savedJobIds.contains(selectedJob!!.id))
          },
          onApply = {
            applyingJob = selectedJob
          }
        )
      } else {
        when (currentTab) {
          "feed" -> {
            JobFeedScreen(
              viewModel = viewModel,
              onJobListingClick = { listing ->
                viewModel.selectJobListing(listing)
              },
              onSaveIndustryAlert = { industry ->
                viewModel.saveIndustryJobAlert(
                  industryOrDepartment = industry,
                  title = "$industry Department Alerts"
                )
              },
              onPostJobClick = if (isEmployer) { { showEmployerPostJobScreen = true } } else null
            )
          }

          "jobs" -> {
            HomeScreen(
              jobs = filteredJobs,
              searchQuery = searchQuery,
              selectedIndustry = selectedIndustry,
              selectedLocation = selectedLocation,
              selectedExperience = selectedExperience,
              onlyRemote = onlyRemote,
              minSalary = minSalary,
              savedJobIds = savedJobIds,
              appliedJobIds = appliedJobIds,
              onSearchChange = { viewModel.onSearchQueryChange(it) },
              onIndustrySelect = { viewModel.onIndustrySelected(it) },
              onOpenFilterSheet = { showFilterSheet = true },
              onClearFilters = { viewModel.clearAllFilters() },
              onJobClick = { job ->
                viewModel.selectJob(job)
                isDetailActive = true
              },
              onToggleSaveJob = { job ->
                viewModel.toggleSaveJob(job.id, savedJobIds.contains(job.id))
              },
              onQuickApply = { job ->
                applyingJob = job
              },
              onNavigateToAlerts = { currentTab = "alerts" },
              onSubscribeIndustryAlert = { ind ->
                alertTargetIndustry = ind
                showCreateAlertDialog = true
              }
            )
          }

          "alerts" -> {
            JobAlertsScreen(
              user = activeUser,
              alerts = userAlerts,
              notifications = notifications,
              onCreateAlertClick = {
                alertTargetIndustry = "All"
                showCreateAlertDialog = true
              },
              onToggleActive = { alert -> viewModel.toggleAlertActive(alert) },
              onDeleteAlert = { alert -> viewModel.deleteAlert(alert) },
              onTriggerAlertScan = { viewModel.triggerManualAlertCheck() },
              onPreviewEmail = { record -> viewModel.showEmailPreview(record) },
              onViewJobById = { jobId ->
                val job = allJobs.find { it.id == jobId }
                if (job != null) {
                  viewModel.selectJob(job)
                  isDetailActive = true
                }
              }
            )
          }

          "employer" -> {
            if (isEmployer) {
              EmployerHubScreen(
                employerUser = activeUser,
                postedJobs = employerPostedJobs.ifEmpty { allJobs.filter { it.employerEmail == activeUser.email } },
                applications = allApplicantsForEmployer,
                viewModel = viewModel,
                onPostJobClick = {
                  editingJob = null
                  showEmployerPostJobScreen = true
                },
                onEditJob = { job ->
                  editingJob = job
                  showEmployerPostJobScreen = true
                },
                onToggleJobActive = { job -> viewModel.toggleJobActive(job) },
                onDeleteJob = { job -> viewModel.deleteJob(job) },
                onUpdateApplicantStatus = { id, status -> viewModel.updateApplicantStatus(id, status) },
                onViewJobDetail = { job ->
                  viewModel.selectJob(job)
                  isDetailActive = true
                }
              )
            } else {
              currentTab = "feed"
            }
          }

          "chat" -> {
            CareerChatbotScreen(
              onNavigateBack = { currentTab = "feed" }
            )
          }

          "profile" -> {
            UserProfileScreen(
              viewModel = viewModel,
              onCreateAlertClick = {
                alertTargetIndustry = "All"
                showCreateAlertDialog = true
              },
              onJobClick = { job ->
                viewModel.selectJob(job)
                isDetailActive = true
              }
            )
          }
        }
      }
    }
  }

  // Filter Bottom Sheet
  if (showFilterSheet) {
    JobFilterBottomSheet(
      selectedIndustry = selectedIndustry,
      selectedLocation = selectedLocation,
      selectedExperience = selectedExperience,
      selectedJobType = selectedJobType,
      onlyRemote = onlyRemote,
      minSalary = minSalary,
      onApplyFilters = { ind, loc, exp, type, rem, sal ->
        viewModel.onIndustrySelected(ind)
        viewModel.setLocation(loc)
        viewModel.setExperience(exp)
        viewModel.setJobType(type)
        if (onlyRemote != rem) viewModel.toggleOnlyRemote()
        viewModel.setMinSalary(sal)
        showFilterSheet = false
      },
      onDismiss = { showFilterSheet = false }
    )
  }

  // Quick Apply Modal Dialog
  applyingJob?.let { job ->
    ApplyJobDialog(
      job = job,
      user = activeUser,
      onDismiss = { applyingJob = null },
      onSubmit = { resumeNote ->
        viewModel.applyToJob(job.id, resumeNote)
        applyingJob = null
      }
    )
  }

  // Create Job Dialog (Quick Dialog option)
  if (showCreateJobDialog) {
    CreateJobDialog(
      currentUser = activeUser,
      onDismiss = { showCreateJobDialog = false },
      onPostJob = { title, comp, ind, loc, isRem, type, exp, minS, maxS, desc, resp, req, ben, email ->
        viewModel.postNewJob(title, comp, ind, loc, isRem, type, exp, minS, maxS, desc, resp, req, ben, email)
        showCreateJobDialog = false
      }
    )
  }

  // Create Industry Job Alert Modal Dialog
  if (showCreateAlertDialog) {
    CreateAlertDialog(
      userEmail = activeUser.email,
      initialIndustry = alertTargetIndustry,
      onDismiss = { showCreateAlertDialog = false },
      onSaveAlert = { title, ind, loc, exp, minS, emailA, pushA ->
        viewModel.saveIndustryJobAlert(
          industryOrDepartment = ind,
          title = title,
          location = loc,
          experienceLevel = exp,
          minSalary = minS,
          emailAlerts = emailA,
          pushAlerts = pushA
        )
        showCreateAlertDialog = false
      }
    )
  }

  // Notification Email Preview Modal
  previewEmail?.let { notifRecord ->
    EmailPreviewDialog(
      notification = notifRecord,
      onDismiss = { viewModel.showEmailPreview(null) },
      onViewJob = { jobId ->
        val job = allJobs.find { it.id == jobId }
        if (job != null) {
          viewModel.selectJob(job)
          isDetailActive = true
        }
      }
    )
  }
}
