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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.JobEntity
import com.example.data.model.UserProfileEntity

@Composable
fun ApplyJobDialog(
  job: JobEntity,
  user: UserProfileEntity,
  onDismiss: () -> Unit,
  onSubmit: (resumeNote: String) -> Unit
) {
  var resumeNote by remember {
    mutableStateOf(
      "Hi ${job.company} Team, I am excited to apply for the ${job.title} position. With my background in ${user.industry} and expertise in ${user.skills.take(40)}, I am confident in adding immediate value to your team."
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text(
          text = "Apply for Position",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${job.title} • ${job.company}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Applicant Profile Summary Box
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "Applying As",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.padding(horizontal = 4.dp))
              Text(user.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.padding(horizontal = 4.dp))
              Text(user.email, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.padding(horizontal = 4.dp))
              Text(user.headline, style = MaterialTheme.typography.bodySmall)
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "Cover Note & Highlights",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = resumeNote,
          onValueChange = { resumeNote = it },
          placeholder = { Text("Brief introduction, key achievements, or link to portfolio/resume...") },
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .testTag("apply_note_input"),
          maxLines = 6
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Your profile information and skills will be forwarded to ${job.employerEmail.ifBlank { "the hiring team" }}.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSubmit(resumeNote) },
        modifier = Modifier.testTag("confirm_apply_button")
      ) {
        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
        Text("Submit Application")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
