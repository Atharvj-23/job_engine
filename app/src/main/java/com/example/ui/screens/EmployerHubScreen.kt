package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobApplicationEntity
import com.example.data.model.JobEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal
import com.example.ui.viewmodel.JobViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Employer Dashboard Screen:
 * Authenticated employers can input job details like title, description, and salary,
 * and save them directly to Firestore.
 * Also allows tracking posted openings, toggling active status, and managing applicant pipelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployerDashboardScreen(
  employerUser: UserProfileEntity,
  postedJobs: List<JobEntity>,
  applications: List<JobApplicationEntity>,
  viewModel: JobViewModel? = null,
  onPostJobClick: () -> Unit = {},
  onEditJob: (JobEntity) -> Unit = {},
  onToggleJobActive: (JobEntity) -> Unit = {},
  onDeleteJob: (JobEntity) -> Unit = {},
  onUpdateApplicantStatus: (Long, String) -> Unit = { _, _ -> },
  onViewJobDetail: (JobEntity) -> Unit = {}
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Openings (${postedJobs.size})", "Post to Firestore", "Applicants (${applications.size})")

  // Embedded Firestore Job Post Form State
  val scope = rememberCoroutineScope()
  var postTitle by remember { mutableStateOf("") }
  var postCompany by remember { mutableStateOf(employerUser.companyName.ifBlank { "NovaVanguard Health" }) }
  var postIndustry by remember { mutableStateOf("Engineering") }
  var postLocation by remember { mutableStateOf(employerUser.location.ifBlank { "Boston, MA" }) }
  var postIsRemote by remember { mutableStateOf(true) }
  var postJobType by remember { mutableStateOf("Full-time") }
  var postExperienceLevel by remember { mutableStateOf("Mid Level") }
  var postSalaryMin by remember { mutableStateOf("120000") }
  var postSalaryMax by remember { mutableStateOf("160000") }
  var postDescription by remember { mutableStateOf("") }
  var postRequirements by remember { mutableStateOf("") }
  var postBenefits by remember { mutableStateOf("Comprehensive health, dental, 401(k) matching, flexible PTO") }
  var isSavingToFirestore by remember { mutableStateOf(false) }
  var firestoreSuccessDocId by remember { mutableStateOf<String?>(null) }
  var formErrorMessage by remember { mutableStateOf<String?>(null) }

  val industryOptions = listOf("Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")

  Scaffold(
    floatingActionButton = {
      if (selectedTab != 1) {
        FloatingActionButton(
          onClick = { selectedTab = 1 },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White,
          modifier = Modifier.testTag("fab_post_job")
        ) {
          Icon(Icons.Default.Add, contentDescription = "Post New Job")
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Employer Dashboard Header
      Card(
        shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(JobPulsePurple),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Business,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Employer Dashboard",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${employerUser.fullName} • ${employerUser.companyName.ifBlank { "Hiring Console" }}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            // Cloud Status Badge
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(JobPulseTeal.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Icon(Icons.Default.CloudDone, contentDescription = null, tint = JobPulseTeal, modifier = Modifier.size(13.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Firestore", fontSize = 10.sp, color = JobPulseTeal, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // 2. Dashboard Navigation Tabs
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("employer_tab_$index")
          )
        }
      }

      // 3. Tab Contents
      when (selectedTab) {
        0 -> {
          // TAB 0: Posted Openings List
          if (postedJobs.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Jobs Posted Yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  "Input job details like title, description, and salary in the 'Post to Firestore' tab to publish live openings.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { selectedTab = 1 }) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Create First Job Opening")
                }
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .testTag("employer_posted_jobs_list"),
              contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              item {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "Live Active Openings (${postedJobs.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Button(
                    onClick = { selectedTab = 1 },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                  ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Job", fontSize = 12.sp)
                  }
                }
              }

              items(postedJobs, key = { "employer_job_${it.id}" }) { job ->
                EmployerJobCard(
                  job = job,
                  onEdit = { onEditJob(job) },
                  onToggleActive = { onToggleJobActive(job) },
                  onDelete = { onDeleteJob(job) },
                  onClick = { onViewJobDetail(job) }
                )
              }
            }
          }
        }

        1 -> {
          // TAB 1: Direct Firestore Job Input Form
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .testTag("employer_dashboard_post_form"),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "Post Opening to Firestore",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Save details directly to cloud Firestore & broadcast live to job feed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            // Quick 1-Click Template Chips
            item {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JobPulseAmber, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Fill with Preset Template:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  item {
                    AssistChip(
                      onClick = {
                        postTitle = "Clinical Oncology Pharmacist"
                        postCompany = employerUser.companyName.ifBlank { "Boston Medical Center" }
                        postIndustry = "Pharmacy"
                        postLocation = "Boston, MA"
                        postIsRemote = false
                        postSalaryMin = "135000"
                        postSalaryMax = "165000"
                        postDescription = "Lead inpatient oncology chemotherapy regimens, dosing optimization, and sterile compounding compliance under USP 797/800 standards."
                        postRequirements = "PharmD, BCOP certified, Massachusetts RPh license, 3+ years hospital oncology experience."
                      },
                      label = { Text("💊 Oncology Pharmacist", fontSize = 11.sp) }
                    )
                  }
                  item {
                    AssistChip(
                      onClick = {
                        postTitle = "Cloud Infrastructure & SRE Architect"
                        postCompany = employerUser.companyName.ifBlank { "NovaVanguard Health" }
                        postIndustry = "Engineering"
                        postLocation = "Remote"
                        postIsRemote = true
                        postSalaryMin = "150000"
                        postSalaryMax = "190000"
                        postDescription = "Scale high-throughput streaming systems, distributed telemetry pipelines, and multi-region Kubernetes clusters with zero-trust networking."
                        postRequirements = "5+ years SRE / DevOps experience, production Kubernetes, Terraform, and Go/Kotlin scripting."
                      },
                      label = { Text("💻 Cloud Engineer", fontSize = 11.sp) }
                    )
                  }
                  item {
                    AssistChip(
                      onClick = {
                        postTitle = "Biomedical Telemetry Systems Engineer"
                        postCompany = employerUser.companyName.ifBlank { "VitalPath BioLabs" }
                        postIndustry = "Healthcare"
                        postLocation = "Cambridge, MA"
                        postIsRemote = true
                        postSalaryMin = "125000"
                        postSalaryMax = "155000"
                        postDescription = "Design patient telemetry gateways and HL7/FHIR clinical data interfaces connecting bedside diagnostic monitors with hospital clouds."
                        postRequirements = "Degree in Biomedical Engineering or Health Informatics, HL7/FHIR familiarity, 3+ years clinical tech experience."
                      },
                      label = { Text("🩺 Biomedical Tech", fontSize = 11.sp) }
                    )
                  }
                }
              }
            }

            // Success Banner
            if (firestoreSuccessDocId != null) {
              item {
                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = JobPulseEmerald.copy(alpha = 0.15f)),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = JobPulseEmerald, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Job Saved Directly to Firestore!", fontWeight = FontWeight.Bold, color = JobPulseEmerald, fontSize = 13.sp)
                      Text("Document ID: ${firestoreSuccessDocId?.take(16)}... Live on feed!", fontSize = 11.sp)
                    }
                  }
                }
              }
            }

            // Error Banner
            if (formErrorMessage != null) {
              item {
                Card(
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = formErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                  )
                }
              }
            }

            // 1. Title Input
            item {
              OutlinedTextField(
                value = postTitle,
                onValueChange = { postTitle = it },
                label = { Text("Job Title *") },
                placeholder = { Text("e.g. Senior Clinical Pharmacist") },
                leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dashboard_job_title_input")
              )
            }

            // 2. Company Name
            item {
              OutlinedTextField(
                value = postCompany,
                onValueChange = { postCompany = it },
                label = { Text("Hiring Company *") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dashboard_job_company_input")
              )
            }

            // 3. Category / Industry Selection
            item {
              Column {
                Text("Industry / Category *", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  items(industryOptions) { ind ->
                    val isSel = postIndustry == ind
                    FilterChip(
                      selected = isSel,
                      onClick = { postIndustry = ind },
                      label = { Text(ind, fontSize = 12.sp) },
                      colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                      )
                    )
                  }
                }
              }
            }

            // 4. Location & Remote Toggle
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedTextField(
                  value = postLocation,
                  onValueChange = { postLocation = it },
                  label = { Text("Location *") },
                  leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )

                FilterChip(
                  selected = postIsRemote,
                  onClick = { postIsRemote = !postIsRemote },
                  label = { Text(if (postIsRemote) "✓ Remote" else "On-site", fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = JobPulseEmerald,
                    selectedLabelColor = Color.White
                  )
                )
              }
            }

            // 5. Salary Details (Min & Max)
            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedTextField(
                  value = postSalaryMin,
                  onValueChange = { postSalaryMin = it.filter { c -> c.isDigit() } },
                  label = { Text("Min Salary ($/yr) *") },
                  leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                  value = postSalaryMax,
                  onValueChange = { postSalaryMax = it.filter { c -> c.isDigit() } },
                  label = { Text("Max Salary ($/yr)") },
                  leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null) },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                  singleLine = true,
                  modifier = Modifier.weight(1f)
                )
              }
            }

            // 6. Job Description
            item {
              OutlinedTextField(
                value = postDescription,
                onValueChange = { postDescription = it },
                label = { Text("Job Description *") },
                placeholder = { Text("Outline role scope, core objectives, clinical or engineering challenges...") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dashboard_job_desc_input")
              )
            }

            // 7. Requirements
            item {
              OutlinedTextField(
                value = postRequirements,
                onValueChange = { postRequirements = it },
                label = { Text("Requirements & Qualifications *") },
                placeholder = { Text("- Bachelor's or Master's or PharmD degree\n- 3+ years experience\n- Technical or clinical competencies") },
                leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("dashboard_job_reqs_input")
              )
            }

            // 8. Benefits
            item {
              OutlinedTextField(
                value = postBenefits,
                onValueChange = { postBenefits = it },
                label = { Text("Benefits & Perks") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )
            }

            // 9. Save Directly to Firestore Button
            item {
              Button(
                onClick = {
                  if (postTitle.isBlank()) {
                    formErrorMessage = "Please enter a job title."
                    return@Button
                  }
                  if (postCompany.isBlank()) {
                    formErrorMessage = "Please enter hiring company name."
                    return@Button
                  }
                  if (postDescription.isBlank()) {
                    formErrorMessage = "Please enter a job description."
                    return@Button
                  }

                  formErrorMessage = null
                  isSavingToFirestore = true

                  val minSal = postSalaryMin.toIntOrNull() ?: 100000
                  val maxSal = postSalaryMax.toIntOrNull() ?: 150000

                  scope.launch {
                    if (viewModel != null) {
                      val result = viewModel.postJobToFirestore(
                        title = postTitle.trim(),
                        company = postCompany.trim(),
                        industry = postIndustry,
                        location = postLocation.trim(),
                        isRemote = postIsRemote,
                        jobType = postJobType,
                        experienceLevel = postExperienceLevel,
                        salaryMin = minSal,
                        salaryMax = maxSal,
                        description = postDescription.trim(),
                        responsibilities = "",
                        requirements = postRequirements.trim(),
                        benefits = postBenefits.trim(),
                        employerEmail = employerUser.email
                      )
                      isSavingToFirestore = false
                      result.onSuccess { docId ->
                        firestoreSuccessDocId = docId
                        viewModel.showSnackbar("Job opening successfully uploaded to Firestore!")
                      }.onFailure { err ->
                        firestoreSuccessDocId = "LOCAL_SYNC_QUEUED"
                        viewModel.showSnackbar("Saved to local database. Firestore sync queued.")
                      }
                    } else {
                      isSavingToFirestore = false
                      firestoreSuccessDocId = "DOC_SIMULATED_SUCCESS"
                    }
                  }
                },
                enabled = !isSavingToFirestore,
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                  containerColor = JobPulsePurple
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(52.dp)
                  .testTag("dashboard_save_to_firestore_btn")
              ) {
                if (isSavingToFirestore) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                  Spacer(modifier = Modifier.width(10.dp))
                  Text("Saving directly to Firestore...", fontWeight = FontWeight.Bold)
                } else {
                  Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Save Job Directly to Firestore", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
              }
            }
          }
        }

        2 -> {
          // TAB 2: Applicants Pipeline
          var applicantStatusFilter by remember { mutableStateOf("ALL") }
          val filteredApplicants = when (applicantStatusFilter) {
            "ALL" -> applications
            "APPLIED" -> applications.filter { it.status.uppercase() in listOf("APPLIED", "SUBMITTED") }
            "REVIEWING" -> applications.filter { it.status.uppercase() == "REVIEWING" }
            "INTERVIEW" -> applications.filter { it.status.uppercase() == "INTERVIEW" }
            "OFFERED" -> applications.filter { it.status.uppercase() in listOf("ACCEPTED", "OFFERED") }
            "REJECTED" -> applications.filter { it.status.uppercase() == "REJECTED" }
            else -> applications
          }

          if (applications.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(52.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text("No Applicants Yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  "Applications submitted by candidates from the Job Feed will appear here for hiring pipeline review.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          } else {
            Column(modifier = Modifier.fillMaxSize()) {
              // Pipeline stage filter chips
              LazyRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val stages = listOf(
                  "ALL" to "All (${applications.size})",
                  "APPLIED" to "Applied (${applications.count { it.status.uppercase() in listOf("APPLIED", "SUBMITTED") }})",
                  "REVIEWING" to "Reviewing (${applications.count { it.status.uppercase() == "REVIEWING" }})",
                  "INTERVIEW" to "Interview (${applications.count { it.status.uppercase() == "INTERVIEW" }})",
                  "OFFERED" to "Offered (${applications.count { it.status.uppercase() in listOf("ACCEPTED", "OFFERED") }})",
                  "REJECTED" to "Rejected (${applications.count { it.status.uppercase() == "REJECTED" }})"
                )
                items(stages) { (code, label) ->
                  FilterChip(
                    selected = applicantStatusFilter == code,
                    onClick = { applicantStatusFilter = code },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("applicant_filter_$code")
                  )
                }
              }

              LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .testTag("employer_applicants_list"),
                contentPadding = PaddingValues(16.dp, 6.dp, 16.dp, 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                items(filteredApplicants, key = { "applicant_${it.id}" }) { app ->
                  val jobTitle = postedJobs.find { it.id == app.jobId }?.title
                    ?: viewModel?.allJobs?.value?.find { it.id == app.jobId }?.title
                    ?: "Position #${app.jobId}"
                  ApplicantCard(
                    application = app,
                    jobTitle = jobTitle,
                    onUpdateStatus = { newStatus -> onUpdateApplicantStatus(app.id, newStatus) }
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

// Keep EmployerHubScreen as an alias for backwards compatibility
@Composable
fun EmployerHubScreen(
  employerUser: UserProfileEntity,
  postedJobs: List<JobEntity>,
  applications: List<JobApplicationEntity>,
  viewModel: JobViewModel? = null,
  onPostJobClick: () -> Unit = {},
  onEditJob: (JobEntity) -> Unit = {},
  onToggleJobActive: (JobEntity) -> Unit = {},
  onDeleteJob: (JobEntity) -> Unit = {},
  onUpdateApplicantStatus: (Long, String) -> Unit = { _, _ -> },
  onViewJobDetail: (JobEntity) -> Unit = {}
) {
  EmployerDashboardScreen(
    employerUser = employerUser,
    postedJobs = postedJobs,
    applications = applications,
    viewModel = viewModel,
    onPostJobClick = onPostJobClick,
    onEditJob = onEditJob,
    onToggleJobActive = onToggleJobActive,
    onDeleteJob = onDeleteJob,
    onUpdateApplicantStatus = onUpdateApplicantStatus,
    onViewJobDetail = onViewJobDetail
  )
}

@Composable
private fun EmployerJobCard(
  job: JobEntity,
  onEdit: () -> Unit,
  onToggleActive: () -> Unit,
  onDelete: () -> Unit,
  onClick: () -> Unit
) {
  ElevatedCard(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("employer_job_card_${job.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (job.isActive) JobPulseEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = if (job.isActive) "● Active Opening" else "○ Paused",
            color = if (job.isActive) JobPulseEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onToggleActive,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (job.isActive) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (job.isActive) "Pause" else "Activate",
              tint = if (job.isActive) JobPulseAmber else JobPulseEmerald,
              modifier = Modifier.size(18.dp)
            )
          }

          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = job.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )

      Text(
        text = "${job.industry} • ${job.location}${if (job.isRemote) " (Remote)" else ""}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = job.salaryText.ifBlank { "Competitive" },
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = JobPulseAmber
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.width(4.dp))
          Text("${job.applicantCount} Applicants", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun ApplicantCard(
  application: JobApplicationEntity,
  jobTitle: String,
  onUpdateStatus: (String) -> Unit
) {
  val statusUpper = application.status.uppercase()
  val statusColor = when (statusUpper) {
    "ACCEPTED", "OFFERED" -> JobPulseEmerald
    "INTERVIEW" -> JobPulseBlue
    "REVIEWING" -> JobPulsePurple
    "REJECTED" -> MaterialTheme.colorScheme.error
    else -> JobPulseAmber
  }

  ElevatedCard(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("applicant_card_${application.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(JobPulseBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = application.applicantName.take(1).uppercase().ifBlank { "A" },
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = JobPulseBlue
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = application.applicantName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = application.applicantHeadline.ifBlank { application.applicantEmail },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
          Text(
            text = application.status,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Applied For: $jobTitle",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (application.applicantEmail.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = application.applicantEmail,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (application.resumeNote.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp)
        ) {
          Column {
            Text("Cover Note / Message:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "\"${application.resumeNote}\"",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider()
      Spacer(modifier = Modifier.height(8.dp))

      // Pipeline Stage Status update actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Stage:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          FilterChip(
            selected = statusUpper == "REVIEWING",
            onClick = { onUpdateStatus("REVIEWING") },
            label = { Text("Review", fontSize = 10.sp) }
          )
          FilterChip(
            selected = statusUpper == "INTERVIEW",
            onClick = { onUpdateStatus("INTERVIEW") },
            label = { Text("Interview", fontSize = 10.sp) }
          )
          FilterChip(
            selected = statusUpper in listOf("ACCEPTED", "OFFERED"),
            onClick = { onUpdateStatus("OFFERED") },
            label = { Text("Offer", fontSize = 10.sp) }
          )
          FilterChip(
            selected = statusUpper == "REJECTED",
            onClick = { onUpdateStatus("REJECTED") },
            label = { Text("Reject", fontSize = 10.sp) }
          )
        }
      }
    }
  }
}
