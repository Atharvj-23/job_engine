package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobListingEntity
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
fun JobListingDetailView(
  jobListing: JobListingEntity,
  isSaved: Boolean,
  isApplied: Boolean,
  hasAlertForIndustry: Boolean,
  onBack: () -> Unit,
  onToggleSave: () -> Unit,
  onSaveAlert: (industry: String) -> Unit,
  onApply: () -> Unit
) {
  BackHandler { onBack() }

  val context = LocalContext.current
  var alertSavedLocally by remember { mutableStateOf(hasAlertForIndustry) }

  val industryColor = when (jobListing.industry.lowercase()) {
    "pharmacy" -> JobPulseEmerald
    "engineering" -> JobPulseBlue
    "healthcare" -> JobPulseTeal
    "tech & software" -> JobPulsePurple
    else -> JobPulseAmber
  }

  val dateFormatted = remember(jobListing.postedDateMillis) {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    sdf.format(Date(jobListing.postedDateMillis))
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = jobListing.company,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("detail_back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to listings"
            )
          }
        },
        actions = {
          IconButton(
            onClick = {
              val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_SUBJECT, "Opening for ${jobListing.title} at ${jobListing.company}")
                putExtra(Intent.EXTRA_TEXT, "Check out this opening for ${jobListing.title} at ${jobListing.company} (${jobListing.industry}) on Job Engine:\n${jobListing.salaryText}")
                type = "text/plain"
              }
              val shareIntent = Intent.createChooser(sendIntent, "Share Job Opening")
              context.startActivity(shareIntent)
            },
            modifier = Modifier.testTag("detail_share_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Job Opening"
            )
          }

          IconButton(
            onClick = onToggleSave,
            modifier = Modifier.testTag("detail_bookmark_button")
          ) {
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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Save Alert button for this industry
          OutlinedButton(
            onClick = {
              alertSavedLocally = true
              onSaveAlert(jobListing.industry)
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("save_alert_industry_button"),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = if (alertSavedLocally) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
          ) {
            Icon(
              imageVector = if (alertSavedLocally) Icons.Default.Check else Icons.Default.NotificationsActive,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (alertSavedLocally) "Alert Active" else "Save Alert",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            )
          }

          // Apply button
          Button(
            onClick = onApply,
            enabled = !isApplied,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .weight(1.2f)
              .testTag("detail_apply_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isApplied) JobPulseEmerald else MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(
              imageVector = if (isApplied) Icons.Default.CheckCircle else Icons.Default.Send,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isApplied) "Applied" else "Apply Now",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("job_listing_detail_scroll"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header Card
      item {
        ElevatedCard(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Industry Badge
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(industryColor.copy(alpha = 0.15f))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(
                  text = jobListing.industry,
                  color = industryColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }

              if (jobListing.isRemote) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0F2FE))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = "Remote Eligible",
                    color = Color(0xFF0369A1),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = jobListing.title,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = jobListing.company,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pills: Location, Experience, Type, Date
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Location
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  Icons.Default.LocationOn,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(jobListing.location, fontSize = 12.sp)
              }

              // Experience
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Icon(
                  Icons.Default.Work,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(jobListing.experienceLevel, fontSize = 12.sp)
              }

              // Job Type
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text(jobListing.jobType, fontSize = 12.sp)
              }

              // Posted Date
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Text("Posted $dateFormatted", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }

      // 2. Compensation Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = JobPulseAmber.copy(alpha = 0.12f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Estimated Compensation",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = jobListing.salaryText.ifBlank {
                  if (jobListing.salaryMin > 0 && jobListing.salaryMax > 0) {
                    "$${jobListing.salaryMin} - $${jobListing.salaryMax} / yr"
                  } else {
                    "Competitive Compensation"
                  }
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(JobPulseAmber.copy(alpha = 0.25f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Paid,
                contentDescription = null,
                tint = JobPulseAmber,
                modifier = Modifier.size(26.dp)
              )
            }
          }
        }
      }

      // 3. Save Alert for this Industry CTA Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Alert for ${jobListing.industry}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
              Text(
                text = "Get notified immediately whenever new ${jobListing.industry} openings appear.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            FilledTonalButton(
              onClick = {
                alertSavedLocally = true
                onSaveAlert(jobListing.industry)
              },
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = if (alertSavedLocally) "Subscribed" else "Subscribe",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // 4. Job Description Section
      item {
        ElevatedCard(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Job Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = jobListing.description.ifBlank {
                "As a ${jobListing.title} at ${jobListing.company}, you will collaborate closely with multidisciplinary teams in the ${jobListing.industry} sector. You will solve complex problems, maintain high standards of quality, and drive key operational initiatives forward."
              },
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 22.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      // 5. Requirements & Qualifications Section
      item {
        ElevatedCard(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Requirements & Qualifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val reqs = jobListing.requirements.ifBlank {
              "Bachelor's or Master's degree in relevant discipline\n" +
                  "${jobListing.experienceLevel} background with practical industry experience\n" +
                  "Strong analytical problem-solving and communication abilities\n" +
                  "Familiarity with regulatory compliance and modern tools in ${jobListing.industry}"
            }.split("\n").filter { it.isNotBlank() }

            reqs.forEach { req ->
              Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
              ) {
                Box(
                  modifier = Modifier
                    .padding(top = 6.dp, end = 10.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                  text = req.trim().removePrefix("-").trim(),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // 6. Employer Contact Info
      if (jobListing.employerEmail.isNotBlank()) {
        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.Email,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Hiring Contact",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = jobListing.employerEmail,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
