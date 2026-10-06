package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobFilterBottomSheet(
  selectedIndustry: String,
  selectedLocation: String,
  selectedExperience: String,
  selectedJobType: String,
  onlyRemote: Boolean,
  minSalary: Int,
  onApplyFilters: (industry: String, location: String, experience: String, jobType: String, remote: Boolean, salary: Int) -> Unit,
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var tempIndustry by remember { mutableStateOf(selectedIndustry) }
  var tempLocation by remember { mutableStateOf(selectedLocation) }
  var tempExperience by remember { mutableStateOf(selectedExperience) }
  var tempJobType by remember { mutableStateOf(selectedJobType) }
  var tempOnlyRemote by remember { mutableStateOf(onlyRemote) }
  var tempMinSalary by remember { mutableFloatStateOf(minSalary.toFloat()) }

  val industries = listOf("All", "Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")
  val locations = listOf("All", "Boston", "San Francisco", "Austin", "Chicago", "San Diego", "Remote")
  val experiences = listOf("All", "Entry Level", "Mid Level", "Senior", "Lead / Principal")
  val jobTypes = listOf("All", "Full-time", "Part-time", "Contract", "Internship")

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    modifier = Modifier.testTag("filter_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(bottom = 32.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Filter Job Openings",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        OutlinedButton(
          onClick = {
            tempIndustry = "All"
            tempLocation = "All"
            tempExperience = "All"
            tempJobType = "All"
            tempOnlyRemote = false
            tempMinSalary = 0f
          },
          modifier = Modifier.testTag("reset_filters_button")
        ) {
          Icon(Icons.Default.Clear, contentDescription = "Reset filters", modifier = Modifier.padding(end = 4.dp))
          Text("Reset")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Industry
      Text("Industry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        industries.forEach { ind ->
          FilterChip(
            selected = tempIndustry.equals(ind, ignoreCase = true),
            onClick = { tempIndustry = ind },
            label = { Text(ind) },
            leadingIcon = if (tempIndustry.equals(ind, ignoreCase = true)) {
              { Icon(Icons.Default.Done, contentDescription = null) }
            } else null,
            modifier = Modifier.testTag("filter_chip_industry_$ind")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Location
      Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        locations.forEach { loc ->
          FilterChip(
            selected = tempLocation.equals(loc, ignoreCase = true),
            onClick = { tempLocation = loc },
            label = { Text(loc) },
            leadingIcon = if (tempLocation.equals(loc, ignoreCase = true)) {
              { Icon(Icons.Default.Done, contentDescription = null) }
            } else null,
            modifier = Modifier.testTag("filter_chip_location_$loc")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Experience Level
      Text("Experience Level", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        experiences.forEach { exp ->
          FilterChip(
            selected = tempExperience.equals(exp, ignoreCase = true),
            onClick = { tempExperience = exp },
            label = { Text(exp) },
            leadingIcon = if (tempExperience.equals(exp, ignoreCase = true)) {
              { Icon(Icons.Default.Done, contentDescription = null) }
            } else null,
            modifier = Modifier.testTag("filter_chip_exp_$exp")
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Job Type
      Text("Job Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        jobTypes.forEach { type ->
          FilterChip(
            selected = tempJobType.equals(type, ignoreCase = true),
            onClick = { tempJobType = type },
            label = { Text(type) },
            leadingIcon = if (tempJobType.equals(type, ignoreCase = true)) {
              { Icon(Icons.Default.Done, contentDescription = null) }
            } else null
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Remote Only Toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Remote Roles Only", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
          Text("Show jobs offering full remote work", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
          checked = tempOnlyRemote,
          onCheckedChange = { tempOnlyRemote = it },
          modifier = Modifier.testTag("remote_only_switch")
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Minimum Salary Slider
      Text(
        text = "Minimum Annual Salary: ${if (tempMinSalary.toInt() == 0) "Any" else "$" + tempMinSalary.toInt() / 1000 + "k+/yr"}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
      Slider(
        value = tempMinSalary,
        onValueChange = { tempMinSalary = it },
        valueRange = 0f..200000f,
        steps = 19,
        modifier = Modifier.testTag("salary_slider")
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Apply Button
      Button(
        onClick = {
          onApplyFilters(
            tempIndustry,
            tempLocation,
            tempExperience,
            tempJobType,
            tempOnlyRemote,
            tempMinSalary.toInt()
          )
          onDismiss()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("apply_filters_button")
      ) {
        Text("Apply Filters", style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}
