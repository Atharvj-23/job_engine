package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.JobListingEntity
import com.example.ui.components.JobFeedEmptyState
import com.example.ui.components.JobSearchBar
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal
import com.example.ui.viewmodel.JobViewModel

/**
 * JobFeedScreen:
 * Displays the list of JobListing entities from the Room database using LazyColumn,
 * leveraging the existing JobViewModel to observe data reactively via StateFlow.
 * Includes Material 3 PullToRefreshBox to allow users to update their listings manually.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun JobFeedScreen(
  viewModel: JobViewModel,
  onJobListingClick: (JobListingEntity) -> Unit,
  onSaveIndustryAlert: (String) -> Unit,
  onPostJobClick: (() -> Unit)? = null
) {
  val jobListings by viewModel.filteredJobListings.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedIndustry by viewModel.selectedIndustry.collectAsState()
  val selectedLocation by viewModel.selectedLocation.collectAsState()
  val onlyRemote by viewModel.onlyRemote.collectAsState()
  val fcmAlertsEnabled by viewModel.fcmNotificationsEnabled.collectAsState()
  val savedJobIds by viewModel.currentSavedJobIds.collectAsState()
  val isRefreshing by viewModel.isRefreshing.collectAsState()
  val context = LocalContext.current

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.setFcmNotifications(context, true)
    } else {
      viewModel.showSnackbar("Notification permission required for Firebase job alerts")
    }
  }

  PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.refreshJobListings() },
    modifier = Modifier
      .fillMaxSize()
      .testTag("job_feed_pull_to_refresh")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("job_feed_screen_lazy_column"),
      contentPadding = PaddingValues(bottom = 88.dp)
    ) {
    // 1. SearchBar Component with Multi-Criteria Filtering (category, location, keywords) and FCM Toggle
    item {
      JobSearchBar(
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        selectedCategory = selectedIndustry,
        onCategorySelected = { viewModel.onIndustrySelected(it) },
        selectedLocation = selectedLocation,
        onLocationSelected = { viewModel.setLocation(it) },
        onlyRemote = onlyRemote,
        onToggleRemoteOnly = { viewModel.toggleOnlyRemote() },
        fcmAlertsEnabled = fcmAlertsEnabled,
        onToggleFcmAlerts = {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
          ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
          } else {
            viewModel.toggleFcmNotifications(context)
          }
        },
        onResetFilters = { viewModel.clearAllFilters() }
      )
    }

    // 2. Feed Header with Count & Department Alert CTA
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.DynamicFeed,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Job Feed (${jobListings.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(4.dp))
          IconButton(
            onClick = { viewModel.refreshJobListings() },
            modifier = Modifier
              .size(32.dp)
              .testTag("feed_manual_refresh_btn")
          ) {
            Icon(
              Icons.Default.Refresh,
              contentDescription = "Refresh Feed",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        if (selectedIndustry != "All") {
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.primaryContainer)
              .clickable { onSaveIndustryAlert(selectedIndustry) }
              .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.NotificationsActive,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "Alert for $selectedIndustry",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      }
    }

    // 3. Feed Items
    if (jobListings.isEmpty()) {
      item {
        JobFeedEmptyState(
          searchQuery = searchQuery,
          selectedIndustry = selectedIndustry,
          onResetFilters = { viewModel.clearAllFilters() },
          onPostJobClick = onPostJobClick,
          onCreateAlertClick = { onSaveIndustryAlert(selectedIndustry) }
        )
      }
    } else {
      items(jobListings, key = { "feed_listing_${it.id}" }) { listing ->
        val isSaved = savedJobIds.contains(listing.id)
        val industryColor = when (listing.industry.lowercase()) {
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
            .clickable { onJobListingClick(listing) }
            .testTag("feed_item_${listing.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Top row: Industry Pill & Save Bookmark
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(industryColor.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = listing.industry,
                  color = industryColor,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(JobPulseTeal.copy(alpha = 0.12f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Icon(
                    Icons.Default.CloudDone,
                    contentDescription = "Fetched from Firestore",
                    tint = JobPulseTeal,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text("Firestore", color = JobPulseTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                if (listing.isRemote) {
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
                  onClick = { viewModel.toggleSaveJob(listing.id, isSaved) },
                  modifier = Modifier
                    .size(36.dp)
                    .testTag("feed_save_btn_${listing.id}")
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

            Text(
              text = listing.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = listing.company,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tags
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(3.dp))
                Text(listing.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(listing.experienceLevel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(listing.jobType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom: Salary & View Detail action
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
                  text = listing.salaryText.ifBlank { "Competitive" },
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              OutlinedButton(
                onClick = { onJobListingClick(listing) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("feed_view_btn_${listing.id}")
              ) {
                Text("View Details", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}
}
