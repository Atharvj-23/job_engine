package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobEntity
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JobDetailScreen(
  job: JobEntity,
  isSaved: Boolean,
  isApplied: Boolean,
  onBack: () -> Unit,
  onToggleSave: () -> Unit,
  onApply: () -> Unit
) {
  val industryColor = when (job.industry.lowercase()) {
    "pharmacy" -> JobPulseEmerald
    "engineering" -> JobPulseBlue
    "healthcare" -> JobPulseTeal
    "tech & software" -> JobPulsePurple
    else -> JobPulseAmber
  }

  val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    .format(Date(job.postedDateMillis))

  val requirementsList = job.requirements
    .split(",")
    .map { it.trim() }
    .filter { it.isNotEmpty() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Job Details", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
          }
        },
        actions = {
          IconButton(onClick = onToggleSave, modifier = Modifier.testTag("detail_save_button")) {
            Icon(
              imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = if (isSaved) "Remove bookmark" else "Bookmark job",
              tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              "Salary Range",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = job.salaryText.ifBlank { "Competitive" },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = JobPulseAmber
            )
          }

          if (isApplied) {
            Button(
              onClick = {},
              enabled = false,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("detail_already_applied_button")
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
              Text("Applied")
            }
          } else {
            Button(
              onClick = onApply,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.testTag("detail_apply_button")
            ) {
              Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
              Text("Apply Now")
            }
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Job Header Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(industryColor.copy(alpha = 0.15f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                job.industry,
                color = industryColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }

            if (job.isRemote) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFE0F2FE))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text("Remote Opportunity", color = Color(0xFF0369A1), fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = job.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
          )

          Spacer(modifier = Modifier.height(6.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(job.company, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(12.dp))

          // Meta items grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(job.location, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
              }
            }
            Column {
              Text("Experience", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(job.experienceLevel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Column {
              Text("Job Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(job.jobType, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
          }
        }
      }

      // Job Description Section
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("About the Role", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = job.description,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
          )
        }
      }

      // Responsibilities Section
      if (job.responsibilities.isNotBlank()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Key Responsibilities", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = job.responsibilities,
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 22.sp
            )
          }
        }
      }

      // Requirements & Skills Section
      if (requirementsList.isNotEmpty()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Required Skills & Qualifications", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              requirementsList.forEach { req ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                  Text(req, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
              }
            }
          }
        }
      }

      // Benefits & Perks
      if (job.benefits.isNotBlank()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Benefits & Perks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = job.benefits,
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 20.sp
            )
          }
        }
      }

      // Posting Meta Details Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Posted: $dateStr", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          if (job.employerEmail.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Contact: ${job.employerEmail}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Applicants: ${job.applicantCount} submitted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}
