package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobApplicationEntity
import com.example.data.model.JobEntity
import com.example.data.model.UserProfileEntity
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulseTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  currentUser: UserProfileEntity,
  allUsers: List<UserProfileEntity>,
  applications: List<JobApplicationEntity>,
  savedJobs: List<JobEntity>,
  allJobs: List<JobEntity>,
  onSwitchUser: (Long) -> Unit,
  onUpdateProfile: (UserProfileEntity) -> Unit,
  onJobClick: (JobEntity) -> Unit
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var showEditProfileDialog by remember { mutableStateOf(false) }
  var showAccountSwitcherDialog by remember { mutableStateOf(false) }

  val tabs = listOf("Overview", "Applications (${applications.size})", "Saved (${savedJobs.size})")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("profile_screen_scroll"),
    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Profile Header Card
    item {
      ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                  .size(56.dp)
                  .clip(CircleShape)
                  .background(if (currentUser.role == "EMPLOYER") JobPulseBlue else JobPulseEmerald),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = currentUser.fullName.take(2).uppercase(),
                  color = Color.White,
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 20.sp
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column {
                Text(
                  text = currentUser.fullName,
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = currentUser.headline.ifBlank { if (currentUser.role == "EMPLOYER") "Employer / Recruiter" else "Job Seeker" },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            IconButton(
              onClick = { showEditProfileDialog = true },
              modifier = Modifier.testTag("edit_profile_btn")
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit profile", tint = MaterialTheme.colorScheme.primary)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Role and switch button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (currentUser.role == "EMPLOYER") JobPulseBlue.copy(alpha = 0.15f) else JobPulseEmerald.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                text = if (currentUser.role == "EMPLOYER") "Role: Employer & Recruiter" else "Role: Job Seeker / Candidate",
                color = if (currentUser.role == "EMPLOYER") JobPulseBlue else JobPulseEmerald,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }

            OutlinedButton(
              onClick = { showAccountSwitcherDialog = true },
              modifier = Modifier.testTag("switch_account_btn")
            ) {
              Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Switch User", fontSize = 11.sp)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(10.dp))

          // Contact Details
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(6.dp))
              Text(currentUser.email, style = MaterialTheme.typography.bodySmall)
            }
            if (currentUser.location.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text(currentUser.location, style = MaterialTheme.typography.bodySmall)
              }
            }
            if (currentUser.phone.isNotBlank()) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text(currentUser.phone, style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      }
    }

    // Tabs
    item {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
            modifier = Modifier.testTag("profile_tab_$index")
          )
        }
      }
    }

    // Tab content
    if (selectedTab == 0) {
      // Overview
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Professional Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = currentUser.bio.ifBlank { "No summary provided yet. Click the edit button to add your background." },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      if (currentUser.skills.isNotBlank()) {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text("Expertise & Skills", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(10.dp))
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                currentUser.skills.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { skill ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                      .padding(horizontal = 10.dp, vertical = 6.dp)
                  ) {
                    Text(skill, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                  }
                }
              }
            }
          }
        }
      }

      if (currentUser.role == "EMPLOYER") {
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text("Company Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(8.dp))
              Text("Company: ${currentUser.companyName}", fontWeight = FontWeight.SemiBold)
              Text("Website: ${currentUser.companyWebsite}", color = MaterialTheme.colorScheme.primary)
              if (currentUser.companyBio.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(currentUser.companyBio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    } else if (selectedTab == 1) {
      // Applications
      if (applications.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("No applications submitted yet.", fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "Find interesting jobs in Engineering or Pharmacy and tap 'Apply Now'.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(applications, key = { "profile_app_${it.id}" }) { app ->
          val targetJob = allJobs.find { it.id == app.jobId }
          val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            .format(Date(app.appliedAtMillis))

          val statusColor = when (app.status) {
            "ACCEPTED" -> JobPulseEmerald
            "INTERVIEW" -> JobPulseBlue
            "REVIEWING" -> JobPulseAmber
            "DECLINED" -> MaterialTheme.colorScheme.error
            else -> JobPulseTeal
          }

          ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { targetJob?.let { onJobClick(it) } },
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = targetJob?.title ?: "Position #${app.jobId}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(app.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = "${targetJob?.company ?: "Company"} • Applied on $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      // Saved Jobs Tab
      if (savedJobs.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text("No saved bookmarks yet.", fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "Tap the bookmark icon on any job card to save it here for later review.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(savedJobs, key = { "profile_saved_${it.id}" }) { job ->
          Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onJobClick(job) },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text("${job.company} • ${job.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(4.dp))
              Text(job.salaryText, fontWeight = FontWeight.Bold, color = JobPulseAmber, fontSize = 12.sp)
            }
          }
        }
      }
    }
  }

  // Switch Account Dialog
  if (showAccountSwitcherDialog) {
    AlertDialog(
      onDismissRequest = { showAccountSwitcherDialog = false },
      title = { Text("Switch Active Account", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          allUsers.forEach { user ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  onSwitchUser(user.id)
                  showAccountSwitcherDialog = false
                },
              colors = CardDefaults.cardColors(
                containerColor = if (user.id == currentUser.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
              )
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (user.role == "EMPLOYER") JobPulseBlue else JobPulseEmerald),
                  contentAlignment = Alignment.Center
                ) {
                  Text(user.fullName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  Text("${user.role} • ${user.email}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (user.id == currentUser.id) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAccountSwitcherDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // Edit Profile Dialog
  if (showEditProfileDialog) {
    EditProfileDialog(
      user = currentUser,
      onDismiss = { showEditProfileDialog = false },
      onSave = { updated ->
        onUpdateProfile(updated)
        showEditProfileDialog = false
      }
    )
  }
}

@Composable
fun EditProfileDialog(
  user: UserProfileEntity,
  onDismiss: () -> Unit,
  onSave: (UserProfileEntity) -> Unit
) {
  var fullName by remember { mutableStateOf(user.fullName) }
  var email by remember { mutableStateOf(user.email) }
  var headline by remember { mutableStateOf(user.headline) }
  var location by remember { mutableStateOf(user.location) }
  var phone by remember { mutableStateOf(user.phone) }
  var bio by remember { mutableStateOf(user.bio) }
  var skills by remember { mutableStateOf(user.skills) }
  var companyName by remember { mutableStateOf(user.companyName) }
  var companyWebsite by remember { mutableStateOf(user.companyWebsite) }
  var role by remember { mutableStateOf(user.role) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = role == "SEEKER",
              onClick = { role = "SEEKER" },
              label = { Text("Job Seeker") }
            )
            FilterChip(
              selected = role == "EMPLOYER",
              onClick = { role = "EMPLOYER" },
              label = { Text("Employer / Recruiter") }
            )
          }
        }
        item {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth().testTag("edit_fullname_input")
          )
        }
        item {
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth().testTag("edit_email_input")
          )
        }
        item {
          OutlinedTextField(
            value = headline,
            onValueChange = { headline = it },
            label = { Text("Headline / Current Role") },
            placeholder = { Text("e.g. Clinical Pharmacist or Lead Roboticist") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = skills,
            onValueChange = { skills = it },
            label = { Text("Skills & Qualifications (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
        item {
          OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Professional Bio") },
            modifier = Modifier.fillMaxWidth().height(90.dp),
            maxLines = 4
          )
        }
        if (role == "EMPLOYER") {
          item {
            OutlinedTextField(
              value = companyName,
              onValueChange = { companyName = it },
              label = { Text("Company Name") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          item {
            OutlinedTextField(
              value = companyWebsite,
              onValueChange = { companyWebsite = it },
              label = { Text("Company Website") },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            user.copy(
              fullName = fullName,
              email = email,
              headline = headline,
              location = location,
              phone = phone,
              bio = bio,
              skills = skills,
              companyName = companyName,
              companyWebsite = companyWebsite,
              role = role
            )
          )
        },
        modifier = Modifier.testTag("save_profile_button")
      ) {
        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text("Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
