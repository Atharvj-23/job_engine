package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseNavy

/**
 * Friendly Empty State UI component for the JobFeed.
 * Displays a colorful friendly illustration, warm and inviting copy, and contextual
 * action buttons (reset filter, post job, subscribe to alerts).
 */
@Composable
fun JobFeedEmptyState(
  searchQuery: String = "",
  selectedIndustry: String = "All",
  onResetFilters: () -> Unit,
  onPostJobClick: (() -> Unit)? = null,
  onCreateAlertClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val isSearching = searchQuery.isNotBlank() || selectedIndustry != "All"

  ElevatedCard(
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .padding(16.dp)
      .testTag("job_feed_empty_state")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // 1. Friendly Illustration with subtle decorative border / glow
      Box(
        modifier = Modifier
          .size(190.dp)
          .clip(RoundedCornerShape(24.dp))
          .background(
            Brush.radialGradient(
              colors = listOf(
                JobPulseBlue.copy(alpha = 0.12f),
                JobPulseAmber.copy(alpha = 0.08f),
                Color.Transparent
              )
            )
          )
          .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_empty_job_feed_1790847796205),
          contentDescription = "Friendly search illustration with magnifying glass and desk",
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .testTag("empty_state_illustration")
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // 2. Friendly Headline
      Text(
        text = if (isSearching) {
          if (searchQuery.isNotBlank()) "No listings matching \"$searchQuery\""
          else "No openings in $selectedIndustry"
        } else {
          "Ready for New Opportunities!"
        },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("empty_state_title")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Helpful Explanatory Text
      Text(
        text = if (isSearching) {
          "We couldn't find any job listings matching your current search or industry filters. Try clearing your search or subscribing to an alert for future openings."
        } else {
          "There are currently no job listings recorded in the database. Be the first to publish an opening, or set up an industry alert so you're notified the second new roles appear!"
        },
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 4. Primary & Secondary CTA Buttons
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        if (isSearching) {
          Button(
            onClick = onResetFilters,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("empty_state_reset_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.FilterAltOff, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Search & View All", fontWeight = FontWeight.Bold)
          }
        } else if (onPostJobClick != null) {
          Button(
            onClick = onPostJobClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("empty_state_post_job_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Post the First Opening", fontWeight = FontWeight.Bold)
          }
        }

        if (onCreateAlertClick != null) {
          OutlinedButton(
            onClick = onCreateAlertClick,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("empty_state_alert_btn")
          ) {
            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (selectedIndustry != "All") "Notify Me When $selectedIndustry Posts" else "Set Up Job Alerts",
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
