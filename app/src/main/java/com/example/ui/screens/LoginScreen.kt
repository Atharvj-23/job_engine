package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.JobPulseAmber
import com.example.ui.theme.JobPulseBlue
import com.example.ui.theme.JobPulseEmerald
import com.example.ui.theme.JobPulsePurple
import com.example.ui.theme.JobPulseTeal
import com.example.ui.viewmodel.JobViewModel
import kotlinx.coroutines.launch

/**
 * Login & Role Selection Screen:
 * Displayed upon first opening the app.
 * Provides distinct portals for:
 * 1. Job Seeker (User): View posts, search, apply, and save bookmarks (Cannot post).
 * 2. Employer / Recruiter: Post new jobs, edit openings, and manage candidate hiring pipeline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
  viewModel: JobViewModel,
  onLoginSuccess: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  // Selected Role: "SEEKER" or "EMPLOYER"
  var selectedRole by remember { mutableStateOf("SEEKER") }

  // Tab: 0 for Sign In, 1 for Create Account
  var authTab by remember { mutableIntStateOf(0) }

  var fullName by remember { mutableStateOf("") }
  var companyName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val isEmployer = selectedRole == "EMPLOYER"
  val isAdmin = selectedRole == "ADMIN"
  val isSeeker = selectedRole == "SEEKER"

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      // 1. App Logo & Brand Header
      Box(
        modifier = Modifier
          .size(68.dp)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              colors = listOf(JobPulseBlue, JobPulsePurple)
            )
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Work,
          contentDescription = "Job Engine Logo",
          tint = Color.White,
          modifier = Modifier.size(34.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Job Engine",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Verified Careers & Live Talent Marketplace",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 2. Select Your Portal (Seeker vs Employer)
      Text(
        text = "SELECT YOUR ACCOUNT TYPE",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Job Seeker Card
        Card(
          onClick = {
            selectedRole = "SEEKER"
            errorMessage = null
          },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSeeker) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
          ),
          border = if (isSeeker) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
          modifier = Modifier
            .weight(1f)
            .testTag("role_selector_seeker")
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isSeeker) JobPulseBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PersonSearch,
                contentDescription = null,
                tint = if (isSeeker) JobPulseBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Job Seeker",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Apply & AI",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }

        // Employer Card
        Card(
          onClick = {
            selectedRole = "EMPLOYER"
            errorMessage = null
          },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isEmployer) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
          ),
          border = if (isEmployer) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
          modifier = Modifier
            .weight(1f)
            .testTag("role_selector_employer")
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isEmployer) JobPulsePurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.BusinessCenter,
                contentDescription = null,
                tint = if (isEmployer) JobPulsePurple else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Employer",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Post & Hire",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }

        // Admin Card
        Card(
          onClick = {
            selectedRole = "ADMIN"
            errorMessage = null
          },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isAdmin) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
          ),
          border = if (isAdmin) ButtonDefaults.outlinedButtonBorder(enabled = true) else null,
          modifier = Modifier
            .weight(1f)
            .testTag("role_selector_admin")
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isAdmin) JobPulseEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = if (isAdmin) JobPulseEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Admin",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Full Access",
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. Role Permission Info Note
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(
            when {
              isAdmin -> JobPulseEmerald.copy(alpha = 0.1f)
              isEmployer -> JobPulsePurple.copy(alpha = 0.1f)
              else -> JobPulseBlue.copy(alpha = 0.1f)
            }
          )
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = when {
              isAdmin -> Icons.Default.AdminPanelSettings
              isEmployer -> Icons.Default.Business
              else -> Icons.Default.Work
            },
            contentDescription = null,
            tint = when {
              isAdmin -> JobPulseEmerald
              isEmployer -> JobPulsePurple
              else -> JobPulseBlue
            },
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = when {
              isAdmin -> "Admin Mode: Full access to manage all openings, view all candidates, and configure platform settings."
              isEmployer -> "Employer Mode: Create postings, set compensation, sync to Firestore, and review candidates."
              else -> "Job Seeker Mode: View verified postings, search & filter, ATS resume analysis, and apply."
            },
            fontSize = 12.sp,
            color = when {
              isAdmin -> JobPulseEmerald
              isEmployer -> JobPulsePurple
              else -> JobPulseBlue
            },
            fontWeight = FontWeight.Medium
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Sign In vs Create Account Tabs
      TabRow(
        selectedTabIndex = authTab,
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = authTab == 0,
          onClick = { authTab = 0; errorMessage = null },
          text = { Text("Sign In", fontWeight = if (authTab == 0) FontWeight.Bold else FontWeight.Normal) }
        )
        Tab(
          selected = authTab == 1,
          onClick = { authTab = 1; errorMessage = null },
          text = { Text("Create Account", fontWeight = if (authTab == 1) FontWeight.Bold else FontWeight.Normal) }
        )
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Error Banner
      if (errorMessage != null) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(10.dp)
        ) {
          Text(
            text = errorMessage ?: "",
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontSize = 12.sp
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // Input Form Fields
      AnimatedVisibility(visible = authTab == 1) {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name *") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_fullname_input")
          )
          Spacer(modifier = Modifier.height(12.dp))
        }
      }

      AnimatedVisibility(visible = authTab == 1 && isEmployer) {
        Column(modifier = Modifier.fillMaxWidth()) {
          OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text("Hiring Company Name *") },
            placeholder = { Text("e.g. NovaVanguard BioLabs") },
            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("login_company_input")
          )
          Spacer(modifier = Modifier.height(12.dp))
        }
      }

      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(if (isEmployer) "Work Email Address *" else "Email Address *") },
        placeholder = { Text(if (isEmployer) "recruiting@company.com" else "seeker@example.com") },
        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_email_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password *") },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
        trailingIcon = {
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
              contentDescription = if (passwordVisible) "Hide password" else "Show password"
            )
          }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_password_input")
      )

      Spacer(modifier = Modifier.height(20.dp))

      // 5. Submit Button
      Button(
        onClick = {
          if (email.isBlank() || !email.contains("@")) {
            errorMessage = "Please enter a valid email address."
            return@Button
          }
          if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return@Button
          }
          if (authTab == 1 && fullName.isBlank()) {
            errorMessage = "Please enter your full name."
            return@Button
          }

          errorMessage = null
          isLoading = true

          scope.launch {
            if (authTab == 1) {
              viewModel.registerWithEmailPassword(email.trim(), password)
            } else {
              viewModel.signInWithEmailPassword(email.trim(), password)
            }
            viewModel.loginAsUser(
              role = selectedRole,
              email = email.trim(),
              fullName = fullName.trim(),
              companyName = companyName.trim()
            )
            isLoading = false
            onLoginSuccess()
          }
        },
        shape = RoundedCornerShape(12.dp),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isEmployer) JobPulsePurple else JobPulseBlue
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("login_submit_button")
      ) {
        if (isLoading) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text(
            text = if (authTab == 0) {
              if (isEmployer) "Sign In as Employer" else "Sign In as Job Seeker"
            } else {
              if (isEmployer) "Create Employer Account" else "Create Seeker Account"
            },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 6. Fast 1-Click Demo Profiles (For Instant Testing)
      Text(
        text = "OR QUICK DEMO LOGIN",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        OutlinedButton(
          onClick = {
            viewModel.loginDemoSeeker()
            onLoginSuccess()
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("demo_login_seeker_btn")
        ) {
          Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(13.dp), tint = JobPulseBlue)
          Spacer(modifier = Modifier.width(3.dp))
          Text("Seeker", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = {
            viewModel.loginDemoEmployer()
            onLoginSuccess()
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("demo_login_employer_btn")
        ) {
          Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(13.dp), tint = JobPulsePurple)
          Spacer(modifier = Modifier.width(3.dp))
          Text("Employer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
          onClick = {
            viewModel.loginDemoAdmin()
            onLoginSuccess()
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
          modifier = Modifier
            .weight(1f)
            .testTag("demo_login_admin_btn")
        ) {
          Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(13.dp), tint = JobPulseEmerald)
          Spacer(modifier = Modifier.width(3.dp))
          Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 7. Google 1-Click Authentication
      OutlinedButton(
        onClick = {
          isLoading = true
          scope.launch {
            val result = viewModel.signInWithGoogle(context, "atharvajirage4@gmail.com")
            isLoading = false
            if (result.isSuccess) {
              viewModel.loginAsUser(
                role = selectedRole,
                email = result.getOrNull()?.email ?: "atharvajirage4@gmail.com",
                fullName = result.getOrNull()?.displayName ?: "Google User",
                companyName = if (isEmployer) "NovaVanguard Health" else ""
              )
              onLoginSuccess()
            }
          }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("login_google_btn")
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_google_logo),
          contentDescription = "Google",
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = if (isEmployer) "Continue with Google as Employer" else "Continue with Google as Seeker",
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 8. Skip / Guest View Button
      TextButton(
        onClick = {
          viewModel.loginDemoSeeker()
          onLoginSuccess()
        },
        modifier = Modifier.testTag("login_skip_btn")
      ) {
        Text(
          text = "Skip & Browse Openings as Guest Seeker →",
          fontSize = 13.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
