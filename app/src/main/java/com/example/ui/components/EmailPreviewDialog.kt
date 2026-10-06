package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationRecordEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmailPreviewDialog(
  notification: NotificationRecordEntity,
  onDismiss: () -> Unit,
  onViewJob: (jobId: Long) -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    .format(Date(notification.timestampMillis))

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.MarkEmailRead,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
          )
          Text(
            text = "Dispatched Email Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close preview")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Email Client Header Simulation
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                "From: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                "JobPulse Alerts <alerts@jobpulse.careers>",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                "To: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                notification.recipientEmail,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                "Sent: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                dateStr,
                style = MaterialTheme.typography.bodySmall
              )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
              text = "Subject: 🎯 Alert Match: ${notification.jobTitle} at ${notification.company}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Rendered Email Template
        Card(
          colors = CardDefaults.cardColors(containerColor = Color.White),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Email branded header
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Work,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
              Text(
                "JobPulse",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color(0xFF0F172A)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "Hi Alex,",
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF1E293B),
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "We found a new job opening matching your saved alert \"${notification.alertTitle}\":",
              color = Color(0xFF475569),
              fontSize = 13.sp,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Highlight Box inside email
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                .padding(12.dp)
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = notification.jobTitle,
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color(0xFF0F2B48)
                )
                Text(
                  text = notification.company,
                  fontWeight = FontWeight.Medium,
                  fontSize = 13.sp,
                  color = Color(0xFF2563EB)
                )
                Text(
                  text = notification.message,
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                onViewJob(notification.jobId)
                onDismiss()
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("email_open_job_button")
            ) {
              Text("View Job Opening on JobPulse")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "You are receiving this automated email alert because you opted in to email notifications on JobPulse. Manage your preferences anytime in the Alerts tab.",
              fontSize = 10.sp,
              color = Color(0xFF94A3B8),
              lineHeight = 14.sp
            )
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Done")
      }
    }
  )
}
