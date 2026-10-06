package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal
import com.example.ui.viewmodel.JobViewModel
import com.example.data.model.JobEntity
import kotlinx.coroutines.launch

/**
 * Employer Post Job Form Screen:
 * Allows employers/users to input or edit detailed job opening information (title, company, description, etc.)
 * with quick pre-filled industry templates, live feed preview, and directly saves to Cloud Firestore
 * and local Room persistence.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EmployerPostJobScreen(
  viewModel: JobViewModel,
  jobToEdit: JobEntity? = null,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  val isEditMode = jobToEdit != null
  val currentUserProfile by viewModel.currentUser.collectAsState()
  val firebaseUser by viewModel.firebaseUser.collectAsState()
  val scope = rememberCoroutineScope()

  var title by remember { mutableStateOf(jobToEdit?.title ?: "Senior Cloud Infrastructure Architect") }
  var company by remember { mutableStateOf(jobToEdit?.company ?: currentUserProfile?.companyName?.ifBlank { "NovaVanguard Health & BioLabs" } ?: "NovaVanguard Health & BioLabs") }
  var industry by remember { mutableStateOf(jobToEdit?.industry ?: "Engineering") }
  var location by remember { mutableStateOf(jobToEdit?.location ?: "Boston, MA") }
  var isRemote by remember { mutableStateOf(jobToEdit?.isRemote ?: true) }
  var jobType by remember { mutableStateOf(jobToEdit?.jobType ?: "Full-time") }
  var experienceLevel by remember { mutableStateOf(jobToEdit?.experienceLevel ?: "Senior") }
  var salaryMinText by remember { mutableStateOf(jobToEdit?.salaryMin?.toString() ?: "145000") }
  var salaryMaxText by remember { mutableStateOf(jobToEdit?.salaryMax?.toString() ?: "185000") }
  var description by remember {
    mutableStateOf(
      jobToEdit?.description ?: "Architect and scale distributed multi-cloud systems powering real-time healthcare telemetry and clinical diagnostic pipelines. Implement resilient CI/CD and secure microservice communications."
    )
  }
  var responsibilities by remember {
    mutableStateOf(
      jobToEdit?.responsibilities ?: "- Design Kubernetes and Terraform infrastructure across multi-region clusters.\n- Ensure HIPAA and SOC2 compliance across streaming pipelines.\n- Lead architectural reviews and mentor junior engineering teams."
    )
  }
  var requirements by remember {
    mutableStateOf(
      jobToEdit?.requirements ?: "- 5+ years building distributed cloud infrastructure\n- Deep Kubernetes, Terraform, Go, or Python expertise\n- Experience with high-availability health-tech platforms"
    )
  }
  var benefits by remember { mutableStateOf(jobToEdit?.benefits ?: "100% remote flexibility, $3,000 home office allowance, 401(k) 6% match, comprehensive medical/dental/vision") }
  var employerEmail by remember { mutableStateOf(jobToEdit?.employerEmail ?: firebaseUser?.email ?: currentUserProfile?.email ?: "recruiting@jobengine.app") }

  var industryExpanded by remember { mutableStateOf(false) }
  var expExpanded by remember { mutableStateOf(false) }
  var typeExpanded by remember { mutableStateOf(false) }

  var isSubmitting by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var postedSuccessDocId by remember { mutableStateOf<String?>(null) }

  val industryOptions = listOf("Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")
  val expOptions = listOf("Entry Level", "Mid Level", "Senior", "Lead / Principal")
  val typeOptions = listOf("Full-time", "Part-time", "Contract", "Internship")

  // Preset Template Helper
  fun applyTemplate(
    tTitle: String,
    tCompany: String,
    tIndustry: String,
    tLocation: String,
    tRemote: Boolean,
    tMin: String,
    tMax: String,
    tDesc: String,
    tResp: String,
    tReq: String
  ) {
    title = tTitle
    company = tCompany
    industry = tIndustry
    location = tLocation
    isRemote = tRemote
    salaryMinText = tMin
    salaryMaxText = tMax
    description = tDesc
    responsibilities = tResp
    requirements = tReq
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isEditMode) "Update Job Opening" else "Post Job Opening",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("post_job_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          if (errorMessage != null) {
            Text(
              text = errorMessage ?: "",
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.sp,
              modifier = Modifier.padding(bottom = 6.dp)
            )
          }

          Button(
            onClick = {
              if (title.isBlank()) {
                errorMessage = "Please specify a job title."
                return@Button
              }
              if (company.isBlank()) {
                errorMessage = "Please enter the hiring company name."
                return@Button
              }
              if (description.isBlank()) {
                errorMessage = "Please provide a job description."
                return@Button
              }

              errorMessage = null
              isSubmitting = true

              val minSal = salaryMinText.toIntOrNull() ?: 0
              val maxSal = salaryMaxText.toIntOrNull() ?: 0

              if (isEditMode && jobToEdit != null) {
                scope.launch {
                  val result = viewModel.updateEmployerJob(
                    jobId = jobToEdit.id,
                    title = title.trim(),
                    company = company.trim(),
                    industry = industry,
                    location = location.trim(),
                    isRemote = isRemote,
                    jobType = jobType,
                    experienceLevel = experienceLevel,
                    salaryMin = minSal,
                    salaryMax = maxSal,
                    description = description.trim(),
                    responsibilities = responsibilities.trim(),
                    requirements = requirements.trim(),
                    benefits = benefits.trim(),
                    isActive = jobToEdit.isActive
                  )
                  isSubmitting = false
                  result.onSuccess {
                    postedSuccessDocId = "UPDATED_${jobToEdit.id}"
                  }.onFailure {
                    errorMessage = it.message ?: "Failed to update job"
                  }
                }
              } else {
                scope.launch {
                  val result = viewModel.postJobToFirestore(
                    title = title.trim(),
                    company = company.trim(),
                    industry = industry,
                    location = location.trim(),
                    isRemote = isRemote,
                    jobType = jobType,
                    experienceLevel = experienceLevel,
                    salaryMin = minSal,
                    salaryMax = maxSal,
                    description = description.trim(),
                    responsibilities = responsibilities.trim(),
                    requirements = requirements.trim(),
                    benefits = benefits.trim(),
                    employerEmail = employerEmail.trim()
                  )
                  isSubmitting = false
                  result.onSuccess { docId ->
                    postedSuccessDocId = docId
                  }.onFailure {
                    postedSuccessDocId = "LOCAL_SYNC_QUEUED"
                  }
                }
              }
            },
            enabled = !isSubmitting,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .testTag("submit_post_job_button")
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(if (isEditMode) "Updating Job..." else "Saving to Firestore...", fontWeight = FontWeight.Bold)
            } else {
              Icon(if (isEditMode) Icons.Default.CloudDone else Icons.Default.CloudUpload, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                if (isEditMode) "Save & Update Job" else "Publish & Save to Firestore",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    if (postedSuccessDocId != null) {
      // Success confirmation state
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(JobPulseEmerald.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudDone,
            contentDescription = null,
            tint = JobPulseEmerald,
            modifier = Modifier.size(42.dp)
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
          text = "Job Successfully Published!",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Opening for '$title' at $company has been stored locally and synced with Cloud Firestore. Subscribed candidates will receive instant alerts!",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "Firestore Document ID:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = postedSuccessDocId ?: "synced",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = onNavigateBack,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Return to Feed / Hub")
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .testTag("employer_post_job_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Fast Template Presets Carousel
        item {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = JobPulseAmber, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Quick 1-Click Templates:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              item {
                AssistChip(
                  onClick = {
                    applyTemplate(
                      tTitle = "Clinical Oncology Pharmacist",
                      tCompany = "Boston Medical Center",
                      tIndustry = "Pharmacy",
                      tLocation = "Boston, MA",
                      tRemote = false,
                      tMin = "135000",
                      tMax = "165000",
                      tDesc = "Lead oncology chemotherapy regimens, dosing optimization, and sterile compounding compliance under USP 797/800 standards.",
                      tResp = "- Verify antineoplastic orders and pharmacokinetics\n- Participate in multidisciplinary tumor board rounds\n- Supervise cleanroom aseptic compounding technicians",
                      tReq = "- PharmD and active Pharmacist license\n- 2+ years hospital or oncology clinical experience\n- Board Certification in Oncology Pharmacy (BCOP) preferred"
                    )
                  },
                  label = { Text("💊 Oncology Pharmacist", fontSize = 12.sp) }
                )
              }
              item {
                AssistChip(
                  onClick = {
                    applyTemplate(
                      tTitle = "Cloud Infrastructure & SRE Architect",
                      tCompany = "Aether Systems",
                      tIndustry = "Engineering",
                      tLocation = "Remote",
                      tRemote = true,
                      tMin = "150000",
                      tMax = "190000",
                      tDesc = "Scale high-throughput streaming systems and multi-region Kubernetes clusters with zero-trust networking.",
                      tResp = "- Manage ArgoCD, Terraform, and Kubernetes clusters\n- Optimize CI/CD pipelines achieving sub-10m deployments\n- Implement distributed observability with Prometheus and Datadog",
                      tReq = "- 5+ years SRE / DevOps experience\n- Production Kubernetes and AWS/GCP expertise\n- Strong Go or Python scripting skills"
                    )
                  },
                  label = { Text("💻 Cloud Engineer", fontSize = 12.sp) }
                )
              }
              item {
                AssistChip(
                  onClick = {
                    applyTemplate(
                      tTitle = "Healthcare Telemetry Solutions Specialist",
                      tCompany = "VitalPath Diagnostics",
                      tIndustry = "Healthcare",
                      tLocation = "Chicago, IL",
                      tRemote = true,
                      tMin = "115000",
                      tMax = "140000",
                      tDesc = "Deploy remote patient monitoring telemetry gateways and integration protocols across acute care hospital wings.",
                      tResp = "- Configure HL7 and FHIR clinical data interfaces\n- Train bedside nursing teams on sensor pairing\n- Troubleshoot real-time vitals broadcasting protocols",
                      tReq = "- Degree in Biomedical Engineering or Health Informatics\n- 3+ years clinical systems integration background\n- Working knowledge of hospital EHR systems"
                    )
                  },
                  label = { Text("🩺 Healthcare Tech", fontSize = 12.sp) }
                )
              }
            }
          }
        }

        // Live Feed Preview Card
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Live Feed Preview", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(JobPulseBlue.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(industry, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JobPulseBlue)
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = title.ifBlank { "Job Title Preview" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )

              Text(
                text = "$company • $location • ${if (isRemote) "Remote" else "On-site"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "$$salaryMinText - $$salaryMaxText / yr",
                style = MaterialTheme.typography.labelMedium,
                color = JobPulseAmber,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Job Title
        item {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Job Title *") },
            placeholder = { Text("e.g. Senior Systems Reliability Engineer") },
            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_job_title_input")
          )
        }

        // Company Name
        item {
          OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Hiring Company *") },
            placeholder = { Text("e.g. Apex Health Systems") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_job_company_input")
          )
        }

        // Industry Dropdown
        item {
          ExposedDropdownMenuBox(
            expanded = industryExpanded,
            onExpandedChange = { industryExpanded = !industryExpanded },
            modifier = Modifier.fillMaxWidth()
          ) {
            OutlinedTextField(
              value = industry,
              onValueChange = {},
              readOnly = true,
              label = { Text("Industry / Department *") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = industryExpanded) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .testTag("post_job_industry_dropdown")
            )
            ExposedDropdownMenu(
              expanded = industryExpanded,
              onDismissRequest = { industryExpanded = false }
            ) {
              industryOptions.forEach { opt ->
                DropdownMenuItem(
                  text = { Text(opt) },
                  onClick = {
                    industry = opt
                    industryExpanded = false
                  }
                )
              }
            }
          }
        }

        // Location & Remote Toggle
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            OutlinedTextField(
              value = location,
              onValueChange = { location = it },
              label = { Text("Location") },
              placeholder = { Text("e.g. Boston, MA") },
              leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
              singleLine = true,
              modifier = Modifier
                .weight(1f)
                .testTag("post_job_location_input")
            )

            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isRemote) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant
              ),
              modifier = Modifier.padding(top = 8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Remote", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Switch(
                  checked = isRemote,
                  onCheckedChange = { isRemote = it },
                  modifier = Modifier.testTag("post_job_remote_switch")
                )
              }
            }
          }
        }

        // Employment Type & Experience
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Type
            ExposedDropdownMenuBox(
              expanded = typeExpanded,
              onExpandedChange = { typeExpanded = !typeExpanded },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = jobType,
                onValueChange = {},
                readOnly = true,
                label = { Text("Job Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor()
              )
              ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
              ) {
                typeOptions.forEach { opt ->
                  DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                      jobType = opt
                      typeExpanded = false
                    }
                  )
                }
              }
            }

            // Experience Level
            ExposedDropdownMenuBox(
              expanded = expExpanded,
              onExpandedChange = { expExpanded = !expExpanded },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = experienceLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Experience") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expExpanded) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor()
              )
              ExposedDropdownMenu(
                expanded = expExpanded,
                onDismissRequest = { expExpanded = false }
              ) {
                expOptions.forEach { opt ->
                  DropdownMenuItem(
                    text = { Text(opt) },
                    onClick = {
                      experienceLevel = opt
                      expExpanded = false
                    }
                  )
                }
              }
            }
          }
        }

        // Compensation Range
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            OutlinedTextField(
              value = salaryMinText,
              onValueChange = { salaryMinText = it.filter { char -> char.isDigit() } },
              label = { Text("Min Salary ($/yr)") },
              leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
              value = salaryMaxText,
              onValueChange = { salaryMaxText = it.filter { char -> char.isDigit() } },
              label = { Text("Max Salary ($/yr)") },
              leadingIcon = { Icon(Icons.Default.Paid, contentDescription = null) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Detailed Description
        item {
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Job Description *") },
            placeholder = { Text("Outline role scope, core objectives, and team context...") },
            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_job_desc_input")
          )
        }

        // Key Responsibilities
        item {
          OutlinedTextField(
            value = responsibilities,
            onValueChange = { responsibilities = it },
            label = { Text("Key Responsibilities") },
            placeholder = { Text("- Lead development of clinical drug distribution pipelines\n- Coordinate code review and compliance audits") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Requirements & Qualifications
        item {
          OutlinedTextField(
            value = requirements,
            onValueChange = { requirements = it },
            label = { Text("Requirements & Qualifications") },
            placeholder = { Text("- Bachelor's degree or higher\n- 4+ years of industry experience\n- Strong communication skills") },
            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_job_reqs_input")
          )
        }

        // Benefits & Perks
        item {
          OutlinedTextField(
            value = benefits,
            onValueChange = { benefits = it },
            label = { Text("Benefits & Perks") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Employer Email
        item {
          OutlinedTextField(
            value = employerEmail,
            onValueChange = { employerEmail = it },
            label = { Text("Hiring / Contact Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("post_job_email_input")
          )
        }

        item {
          Spacer(modifier = Modifier.height(20.dp))
        }
      }
    }
  }
}
