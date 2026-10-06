package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UserProfileEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateJobDialog(
  currentUser: UserProfileEntity,
  onDismiss: () -> Unit,
  onPostJob: (
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
  ) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var company by remember { mutableStateOf(currentUser.companyName.ifBlank { "NovaVanguard Health" }) }
  var industry by remember { mutableStateOf("Engineering") }
  var location by remember { mutableStateOf("Boston, MA") }
  var isRemote by remember { mutableStateOf(false) }
  var jobType by remember { mutableStateOf("Full-time") }
  var experienceLevel by remember { mutableStateOf("Mid Level") }
  var salaryMinText by remember { mutableStateOf("110000") }
  var salaryMaxText by remember { mutableStateOf("145000") }
  var description by remember { mutableStateOf("") }
  var responsibilities by remember { mutableStateOf("") }
  var requirements by remember { mutableStateOf("") }
  var benefits by remember { mutableStateOf("Health insurance, 401(k) match, PTO, Remote flexibility") }
  var employerEmail by remember { mutableStateOf(currentUser.email) }

  var industryExpanded by remember { mutableStateOf(false) }
  var expExpanded by remember { mutableStateOf(false) }

  val industryOptions = listOf("Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")
  val expOptions = listOf("Entry Level", "Mid Level", "Senior", "Lead / Principal")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Post New Job Opening",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Job Title *") },
          placeholder = { Text("e.g. Senior Chemical / Pharmacy Engineer") },
          leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
          modifier = Modifier.fillMaxWidth().testTag("job_title_input")
        )

        OutlinedTextField(
          value = company,
          onValueChange = { company = it },
          label = { Text("Company Name *") },
          leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
          modifier = Modifier.fillMaxWidth().testTag("job_company_input")
        )

        // Industry Dropdown
        ExposedDropdownMenuBox(
          expanded = industryExpanded,
          onExpandedChange = { industryExpanded = !industryExpanded }
        ) {
          OutlinedTextField(
            value = industry,
            onValueChange = {},
            readOnly = true,
            label = { Text("Industry *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = industryExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth().testTag("job_industry_dropdown")
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

        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text("Location (City, State or Country) *") },
          modifier = Modifier.fillMaxWidth().testTag("job_location_input")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Fully Remote Position", style = MaterialTheme.typography.bodyMedium)
          Switch(
            checked = isRemote,
            onCheckedChange = { isRemote = it },
            modifier = Modifier.testTag("job_remote_switch")
          )
        }

        // Experience dropdown
        ExposedDropdownMenuBox(
          expanded = expExpanded,
          onExpandedChange = { expExpanded = !expExpanded }
        ) {
          OutlinedTextField(
            value = experienceLevel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Experience Level *") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
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

        // Salary range
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = salaryMinText,
            onValueChange = { salaryMinText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Min Salary ($)") },
            modifier = Modifier.weight(1f).testTag("job_salary_min_input")
          )
          OutlinedTextField(
            value = salaryMaxText,
            onValueChange = { salaryMaxText = it.filter { ch -> ch.isDigit() } },
            label = { Text("Max Salary ($)") },
            modifier = Modifier.weight(1f).testTag("job_salary_max_input")
          )
        }

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Role Description *") },
          placeholder = { Text("Describe the team, mission, and scope...") },
          modifier = Modifier.fillMaxWidth().height(110.dp).testTag("job_desc_input"),
          maxLines = 5
        )

        OutlinedTextField(
          value = responsibilities,
          onValueChange = { responsibilities = it },
          label = { Text("Key Responsibilities") },
          placeholder = { Text("• Lead formulation design\n• Manage clinical audits...") },
          modifier = Modifier.fillMaxWidth().height(100.dp),
          maxLines = 4
        )

        OutlinedTextField(
          value = requirements,
          onValueChange = { requirements = it },
          label = { Text("Requirements & Skills (comma-separated)") },
          placeholder = { Text("e.g. PharmD, Python, GMP, FDA 510(k), AutoCAD") },
          modifier = Modifier.fillMaxWidth().testTag("job_reqs_input")
        )

        OutlinedTextField(
          value = benefits,
          onValueChange = { benefits = it },
          label = { Text("Benefits & Perks") },
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = employerEmail,
          onValueChange = { employerEmail = it },
          label = { Text("Employer Contact Email *") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && company.isNotBlank()) {
            onPostJob(
              title.trim(),
              company.trim(),
              industry,
              location.trim(),
              isRemote,
              jobType,
              experienceLevel,
              salaryMinText.toIntOrNull() ?: 0,
              salaryMaxText.toIntOrNull() ?: 0,
              description.ifBlank { "Exciting career opportunity in $industry with $company." },
              responsibilities,
              requirements,
              benefits,
              employerEmail.trim()
            )
          }
        },
        enabled = title.isNotBlank() && company.isNotBlank(),
        modifier = Modifier.testTag("publish_job_button")
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text("Publish Job Opening")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
