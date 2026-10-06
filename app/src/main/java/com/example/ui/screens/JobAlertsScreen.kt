package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobAlertEntity
import com.example.data.model.NotificationRecordEntity
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
fun JobAlertsScreen(
  user: UserProfileEntity,
  alerts: List<JobAlertEntity>,
  notifications: List<NotificationRecordEntity>,
  onCreateAlertClick: () -> Unit,
  onToggleActive: (JobAlertEntity) -> Unit,
  onDeleteAlert: (JobAlertEntity) -> Unit,
  onTriggerAlertScan: () -> Unit,
  onPreviewEmail: (NotificationRecordEntity) -> Unit,
  onViewJobById: (Long) -> Unit
) {
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onCreateAlertClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier.testTag("fab_create_alert")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Create Alert")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("job_alerts_list"),
      contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 88.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  "Preferred Job Alerts",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  "Alerts delivered via Email & Push Notifications",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                "Recipient: ${user.email}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Button(
                onClick = onTriggerAlertScan,
                modifier = Modifier.testTag("trigger_alert_scan_button")
              ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan Matches", fontSize = 12.sp)
              }
            }
          }
        }
      }

      // Active Alerts Section
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Your Saved Alerts (${alerts.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          OutlinedButton(
            onClick = onCreateAlertClick,
            modifier = Modifier.testTag("add_alert_button")
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Alert", fontSize = 12.sp)
          }
        }
      }

      if (alerts.isEmpty()) {
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
              Text("No active job alerts yet.", fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                "Create an alert for Engineering or Pharmacy jobs to receive instant push and email dispatches.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      } else {
        items(alerts, key = { "alert_${it.id}" }) { alert ->
          AlertCardItem(
            alert = alert,
            userEmail = user.email,
            onToggleActive = { onToggleActive(alert) },
            onDelete = { onDeleteAlert(alert) }
          )
        }
      }

      // Recent Dispatches / Delivery History Section
      item {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Delivered Alert Notifications (${notifications.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }

      if (notifications.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              "No alert dispatches recorded yet. Tap 'Scan Matches' above to test.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(16.dp)
            )
          }
        }
      } else {
        items(notifications, key = { "notif_${it.id}" }) { record ->
          NotificationDispatchCard(
            record = record,
            onPreviewEmail = { onPreviewEmail(record) },
            onViewJob = { onViewJobById(record.jobId) }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertCardItem(
  alert: JobAlertEntity,
  userEmail: String,
  onToggleActive: () -> Unit,
  onDelete: () -> Unit
) {
  ElevatedCard(
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("alert_card_${alert.id}"),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = alert.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Target: ${alert.industry} • ${alert.location.ifBlank { "Any location" }}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = alert.isActive,
            onCheckedChange = { onToggleActive() },
            modifier = Modifier.testTag("alert_toggle_${alert.id}")
          )
          IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete alert", tint = MaterialTheme.colorScheme.error)
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Tags
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        if (alert.minSalary > 0) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(JobPulseAmber.copy(alpha = 0.15f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text("Min $${alert.minSalary / 1000}k+", fontSize = 11.sp, color = JobPulseAmber, fontWeight = FontWeight.Bold)
          }
        }

        if (alert.experienceLevel != "All") {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(alert.experienceLevel, fontSize = 11.sp)
          }
        }

        // Channels enabled
        if (alert.pushAlerts) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(JobPulseBlue.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Notifications, contentDescription = null, tint = JobPulseBlue, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Push Active", fontSize = 11.sp, color = JobPulseBlue, fontWeight = FontWeight.SemiBold)
          }
        }

        if (alert.emailAlerts) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(JobPulseTeal.copy(alpha = 0.12f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(Icons.Default.Email, contentDescription = null, tint = JobPulseTeal, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Email ($userEmail)", fontSize = 11.sp, color = JobPulseTeal, fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
fun NotificationDispatchCard(
  record: NotificationRecordEntity,
  onPreviewEmail: () -> Unit,
  onViewJob: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    .format(Date(record.timestampMillis))

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(JobPulseEmerald.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = JobPulseEmerald,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Alert: ${record.alertTitle}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "${record.jobTitle} at ${record.company}",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = record.message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedButton(
          onClick = onPreviewEmail,
          modifier = Modifier.testTag("preview_email_btn_${record.id}")
        ) {
          Icon(Icons.Default.MarkEmailRead, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Preview Email", fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
          onClick = onViewJob,
          modifier = Modifier.testTag("notif_view_job_btn_${record.id}")
        ) {
          Text("View Job", fontSize = 11.sp)
        }
      }
    }
  }
}
