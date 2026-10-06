package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAlertDialog(
  userEmail: String,
  initialIndustry: String = "Pharmacy",
  onDismiss: () -> Unit,
  onSaveAlert: (
    title: String,
    industry: String,
    location: String,
    experienceLevel: String,
    minSalary: Int,
    emailAlerts: Boolean,
    pushAlerts: Boolean
  ) -> Unit
) {
  var title by remember { mutableStateOf(if (initialIndustry != "All") "$initialIndustry Openings Alert" else "") }
  var industry by remember { mutableStateOf(if (initialIndustry.isNotBlank()) initialIndustry else "Pharmacy") }
  var location by remember { mutableStateOf("Boston, MA") }
  var experienceLevel by remember { mutableStateOf("All") }
  var minSalaryText by remember { mutableStateOf("100000") }
  var emailAlerts by remember { mutableStateOf(true) }
  var pushAlerts by remember { mutableStateOf(true) }

  var industryExpanded by remember { mutableStateOf(false) }
  var expExpanded by remember { mutableStateOf(false) }

  val industryOptions = listOf("All", "Pharmacy", "Engineering", "Healthcare", "Tech & Software", "Biotech")
  val expOptions = listOf("All", "Entry Level", "Mid Level", "Senior", "Lead / Principal")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.NotificationsActive,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(end = 8.dp)
        )
        Text(
          text = "Industry / Dept Alert",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "Alerts are configured for your chosen industry or department (not for a single job). You will receive instant push and email dispatches as soon as ANY new matching opening is posted.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Alert Name *") },
          placeholder = { Text("e.g. Senior Pharmacy or Cloud Roles") },
          modifier = Modifier.fillMaxWidth().testTag("alert_name_input")
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
            modifier = Modifier.menuAnchor().fillMaxWidth().testTag("alert_industry_select")
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
                  if (title.isBlank()) {
                    title = "$opt Job Alerts"
                  }
                }
              )
            }
          }
        }

        OutlinedTextField(
          value = location,
          onValueChange = { location = it },
          label = { Text("Location (or 'Remote' / 'All')") },
          placeholder = { Text("e.g. Boston, MA or Remote") },
          modifier = Modifier.fillMaxWidth().testTag("alert_location_input")
        )

        // Experience Level Dropdown
        ExposedDropdownMenuBox(
          expanded = expExpanded,
          onExpandedChange = { expExpanded = !expExpanded }
        ) {
          OutlinedTextField(
            value = experienceLevel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Experience Level") },
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

        OutlinedTextField(
          value = minSalaryText,
          onValueChange = { minSalaryText = it.filter { ch -> ch.isDigit() } },
          label = { Text("Minimum Annual Salary ($)") },
          placeholder = { Text("e.g. 90000") },
          modifier = Modifier.fillMaxWidth()
        )

        // Channel Toggles Card
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "Notification Delivery Channels",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )

            // Push Notification Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Column {
                  Text("Push Notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                  Text("Real-time alert on your device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = pushAlerts,
                onCheckedChange = { pushAlerts = it },
                modifier = Modifier.testTag("alert_push_switch")
              )
            }

            // Email Alerts Toggle
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Column {
                  Text("Email Alerts", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                  Text("Sent to $userEmail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
              Switch(
                checked = emailAlerts,
                onCheckedChange = { emailAlerts = it },
                modifier = Modifier.testTag("alert_email_switch")
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalTitle = title.ifBlank {
            "$industry Openings Alert"
          }
          onSaveAlert(
            finalTitle,
            industry,
            location,
            experienceLevel,
            minSalaryText.toIntOrNull() ?: 0,
            emailAlerts,
            pushAlerts
          )
        },
        modifier = Modifier.testTag("save_alert_confirm_button")
      ) {
        Text("Save Alert")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
