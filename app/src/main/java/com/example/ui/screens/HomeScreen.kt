package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.JobEntity
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulseNavy
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
  jobs: List<JobEntity>,
  searchQuery: String,
  selectedIndustry: String,
  selectedLocation: String,
  selectedExperience: String,
  onlyRemote: Boolean,
  minSalary: Int,
  savedJobIds: Set<Long>,
  appliedJobIds: Set<Long>,
  onSearchChange: (String) -> Unit,
  onIndustrySelect: (String) -> Unit,
  onOpenFilterSheet: () -> Unit,
  onClearFilters: () -> Unit,
  onJobClick: (JobEntity) -> Unit,
  onToggleSaveJob: (JobEntity) -> Unit,
  onQuickApply: (JobEntity) -> Unit,
  onNavigateToAlerts: () -> Unit,
  onSubscribeIndustryAlert: (String) -> Unit = {}
) {
  val industries = listOf("All", "Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")

  val hasActiveFilters = selectedIndustry != "All" ||
      selectedLocation != "All" ||
      selectedExperience != "All" ||
      onlyRemote ||
      minSalary > 0

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("home_screen_job_list"),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // 1. TOP SEARCH BAR (Material 3 TextField for Real-Time Title or Company Filtering)
    item {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            TextField(
              value = searchQuery,
              onValueChange = onSearchChange,
              label = { Text("Search by title or company") },
              placeholder = { Text("e.g. Systems Engineer, Clinical Pharmacist...") },
              leadingIcon = {
                Icon(
                  Icons.Default.Search,
                  contentDescription = "Search",
                  tint = MaterialTheme.colorScheme.primary
                )
              },
              trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                  IconButton(
                    onClick = { onSearchChange("") },
                    modifier = Modifier.testTag("clear_search_btn")
                  ) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear search query")
                  }
                }
              } else null,
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
              ),
              modifier = Modifier
                .weight(1f)
                .testTag("search_jobs_input")
            )

            // Filter sheet trigger button
            OutlinedButton(
              onClick = onOpenFilterSheet,
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (hasActiveFilters) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .height(56.dp)
                .testTag("filter_button")
            ) {
              Icon(
                Icons.Default.FilterList,
                contentDescription = "Filter openings",
                tint = if (hasActiveFilters) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
              )
              if (hasActiveFilters) {
                Spacer(modifier = Modifier.width(4.dp))
                Text("Active", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // 2. Industry / Department Carousel
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Industry / Department:",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Dedicated Industry Alert Subscription button
            if (selectedIndustry != "All") {
              Row(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                  .clickable { onSubscribeIndustryAlert(selectedIndustry) }
                  .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.NotificationsActive,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "Alert for $selectedIndustry",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(industries) { ind ->
              val isSelected = selectedIndustry.equals(ind, ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = { onIndustrySelect(ind) },
                label = { Text(ind, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("industry_chip_$ind")
              )
            }
          }
        }
      }
    }

    // 3. Hero Visual Banner
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(160.dp)
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .clip(RoundedCornerShape(16.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.hero_job_banner_1790357173535),
          contentDescription = "Engineering and Pharmacy Careers Banner",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
        // Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.25f),
                  JobPulseNavy.copy(alpha = 0.85f)
                )
              )
            )
        )
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
          verticalArrangement = Arrangement.Bottom
        ) {
          Text(
            text = "Engineering & Pharmacy Careers",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "Real-time search across titles & companies with automated industry alerts",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f)
          )
        }
      }
    }

    // 4. Results Header & Active Filters Bar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (searchQuery.isNotBlank()) {
            "${jobs.size} Results for \"$searchQuery\""
          } else {
            "${jobs.size} Openings Available"
          },
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (hasActiveFilters || searchQuery.isNotBlank()) {
          Text(
            text = "Reset All",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .clickable { onClearFilters() }
              .padding(4.dp)
              .testTag("clear_filters_text")
          )
        }
      }
    }

    // 5. Job Cards List
    if (jobs.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(16.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              Icons.Default.Work,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              "No Job Openings Found",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              if (searchQuery.isNotBlank()) {
                "No jobs match title or company \"$searchQuery\". Try different keywords or set an alert for this department."
              } else {
                "No jobs match your selected filters. Subscribe to alerts for this department to be notified when one appears."
              },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(onClick = onClearFilters) {
                Text("Reset Filters")
              }
              OutlinedButton(onClick = onNavigateToAlerts) {
                Text("Manage Alerts")
              }
            }
          }
        }
      }
    } else {
      items(jobs, key = { "home_job_${it.id}" }) { job ->
        JobListItemCard(
          job = job,
          isSaved = savedJobIds.contains(job.id),
          isApplied = appliedJobIds.contains(job.id),
          onClick = { onJobClick(job) },
          onToggleSave = { onToggleSaveJob(job) },
          onApply = { onQuickApply(job) }
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobListItemCard(
  job: JobEntity,
  isSaved: Boolean,
  isApplied: Boolean,
  onClick: () -> Unit,
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

  ElevatedCard(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clickable(onClick = onClick)
      .testTag("job_card_${job.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top row: Industry Pill & Save Bookmark
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Industry / Department Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(industryColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = job.industry,
            color = industryColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (job.isRemote) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFE0F2FE))
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("Remote", color = Color(0xFF0369A1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(6.dp))
          }

          IconButton(
            onClick = onToggleSave,
            modifier = Modifier
              .size(36.dp)
              .testTag("save_job_btn_${job.id}")
          ) {
            Icon(
              imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = if (isSaved) "Remove bookmark" else "Save job",
              tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Job Title
      Text(
        text = job.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(4.dp))

      // Company Name
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Business,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = job.company,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Location, Experience, Salary tags
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        // Location tag
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.width(3.dp))
          Text(job.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Experience tag
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(job.experienceLevel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Job Type
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(job.jobType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row: Salary & Quick Apply
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Paid,
            contentDescription = null,
            tint = JobPulseAmber,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = job.salaryText.ifBlank { "Competitive" },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        if (isApplied) {
          AssistChip(
            onClick = onClick,
            label = { Text("Applied", color = JobPulseEmerald, fontWeight = FontWeight.Bold) },
            leadingIcon = {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = JobPulseEmerald, modifier = Modifier.size(16.dp))
            },
            colors = AssistChipDefaults.assistChipColors(containerColor = JobPulseEmerald.copy(alpha = 0.1f)),
            border = null
          )
        } else {
          Button(
            onClick = onApply,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.testTag("quick_apply_btn_${job.id}")
          ) {
            Text("Quick Apply", fontSize = 12.sp)
          }
        }
      }
    }
  }
}
