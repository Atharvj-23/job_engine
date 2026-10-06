package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald

/**
 * SearchBar component with comprehensive filtering logic:
 * - Real-time keyword search (title, company, description, requirements)
 * - Category / Industry filtering
 * - Location filtering (including Remote-only toggle)
 * - Firebase Messaging notification toggle to alert users when matching criteria are posted
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JobSearchBar(
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedCategory: String,
  onCategorySelected: (String) -> Unit,
  selectedLocation: String,
  onLocationSelected: (String) -> Unit,
  onlyRemote: Boolean,
  onToggleRemoteOnly: () -> Unit,
  fcmAlertsEnabled: Boolean,
  onToggleFcmAlerts: () -> Unit,
  onResetFilters: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpandedFilters by remember { mutableStateOf(false) }

  val categories = remember {
    listOf("All", "Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech")
  }

  val locations = remember {
    listOf("All", "Remote", "Boston, MA", "San Francisco, CA", "New York, NY", "Austin, TX", "Seattle, WA")
  }

  val activeFilterCount = (if (selectedCategory != "All") 1 else 0) +
      (if (selectedLocation != "All") 1 else 0) +
      (if (onlyRemote) 1 else 0) +
      (if (searchQuery.isNotBlank()) 1 else 0)

  Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp,
    modifier = modifier
      .fillMaxWidth()
      .testTag("job_search_bar_root")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // 1. Keyword Search Input Row with Filter & Clear Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search by keywords, title, company...", fontSize = 13.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { onSearchQueryChange("") },
                modifier = Modifier.testTag("job_search_clear_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear search",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
          ),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("job_search_text_field")
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Filter expand toggle button
        IconButton(
          onClick = { isExpandedFilters = !isExpandedFilters },
          modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
              if (isExpandedFilters || activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .testTag("toggle_filters_btn")
        ) {
          BadgedBox(
            badge = {
              if (activeFilterCount > 0) {
                Badge(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                  Text(activeFilterCount.toString(), fontSize = 10.sp)
                }
              }
            }
          ) {
            Icon(
              imageVector = if (isExpandedFilters) Icons.Default.Tune else Icons.Default.FilterAlt,
              contentDescription = "Filters",
              tint = if (isExpandedFilters || activeFilterCount > 0) MaterialTheme.colorScheme.onPrimaryContainer
              else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 2. Primary Category Horizontal Chip Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (selectedCategory != "All") {
          Text("Reset", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onCategorySelected("All") })
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(categories) { category ->
          val isSelected = selectedCategory.equals(category, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { onCategorySelected(category) },
            label = { Text(category, fontSize = 12.sp) },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("category_chip_${category.replace(" ", "_")}")
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Location Horizontal Chip Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Location:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (selectedLocation != "All" || onlyRemote) {
          Text("Reset", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { onLocationSelected("All") })
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(locations) { loc ->
          val isSelected = selectedLocation.equals(loc, ignoreCase = true)
          FilterChip(
            selected = isSelected,
            onClick = { onLocationSelected(loc) },
            label = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (loc == "Remote") Icons.Default.Public else Icons.Default.LocationOn,
                  contentDescription = null,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(loc, fontSize = 12.sp)
              }
            },
            shape = RoundedCornerShape(10.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = JobPulseBlue,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("location_chip_${loc.replace(" ", "_").replace(",", "")}")
          )
        }
      }

      // 3. Expandable Advanced Filters & Firebase Messaging Toggle
      AnimatedVisibility(
        visible = isExpandedFilters,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

          // A. Location Selection Filter
          Column {
            Text(
              text = "Location Filter",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(locations) { loc ->
                val isSelected = selectedLocation.equals(loc, ignoreCase = true)
                FilterChip(
                  selected = isSelected,
                  onClick = { onLocationSelected(loc) },
                  label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = if (loc == "Remote") Icons.Default.Public else Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(loc, fontSize = 12.sp)
                    }
                  },
                  shape = RoundedCornerShape(8.dp),
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = JobPulseBlue,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("location_chip_${loc.replace(" ", "_").replace(",", "")}")
                )
              }
            }
          }

          // B. Remote Only Toggle Chip & Quick Reset
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            FilterChip(
              selected = onlyRemote,
              onClick = onToggleRemoteOnly,
              label = {
                Text(
                  if (onlyRemote) "✓ Remote Only" else "Remote Only",
                  fontSize = 12.sp
                )
              },
              leadingIcon = {
                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp))
              },
              shape = RoundedCornerShape(8.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = JobPulseEmerald,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.testTag("remote_only_chip")
            )

            if (activeFilterCount > 0) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                  .clickable { onResetFilters() }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("reset_all_filters_btn")
              ) {
                Icon(
                  Icons.Default.FilterAltOff,
                  contentDescription = null,
                  modifier = Modifier.size(14.dp),
                  tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  "Reset All ($activeFilterCount)",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error
                )
              }
            }
          }

          // C. Firebase Messaging Notification Toggle Card
          ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.elevatedCardColors(
              containerColor = if (fcmAlertsEnabled) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("fcm_alert_toggle_card")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                      if (fcmAlertsEnabled) JobPulseEmerald.copy(alpha = 0.2f)
                      else MaterialTheme.colorScheme.surfaceVariant
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (fcmAlertsEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = null,
                    tint = if (fcmAlertsEnabled) JobPulseEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "Firebase Alerts for Criteria",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF9100).copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                      Text(
                        "FCM",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                      )
                    }
                  }
                  Text(
                    text = if (fcmAlertsEnabled) {
                      "Subscribed: ${if (selectedCategory != "All") selectedCategory else "All criteria"}"
                    } else {
                      "Turn on to receive FCM pushes when matching jobs are posted"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Switch(
                checked = fcmAlertsEnabled,
                onCheckedChange = { onToggleFcmAlerts() },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = JobPulseEmerald,
                  checkedTrackColor = JobPulseEmerald.copy(alpha = 0.3f)
                ),
                modifier = Modifier.testTag("fcm_notification_toggle")
              )
            }
          }
        }
      }
    }
  }
}
