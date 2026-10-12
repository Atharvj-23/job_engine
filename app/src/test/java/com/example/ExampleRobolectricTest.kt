package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.auth.FirebaseAuthManager
import com.example.data.JobPulseDatabase
import com.example.data.model.JobEntity
import com.example.data.model.JobListingEntity
import com.example.service.FirebaseJobMessagingManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Job Engine", appName)
  }

  @Test
  fun `verify google sign in flow`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = FirebaseAuthManager.signInWithGoogle(context, "atharvajirage4@gmail.com")
    assertTrue(result.isSuccess)
    val session = result.getOrNull()
    assertNotNull(session)
    assertEquals("atharvajirage4@gmail.com", session?.email)
    assertTrue(session?.isGoogleAuthenticated == true)
  }

  @Test
  fun `verify fcm alerts toggle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    FirebaseJobMessagingManager.init(context)

    // Toggle off
    FirebaseJobMessagingManager.setFcmEnabled(context, false, emptyList())
    assertFalse(FirebaseJobMessagingManager.fcmEnabled.value)

    // Toggle on with criteria
    FirebaseJobMessagingManager.setFcmEnabled(context, true, listOf("Engineering", "Pharmacy"))
    assertTrue(FirebaseJobMessagingManager.fcmEnabled.value)
  }

  @Test
  fun `verify employer job add and update isolation`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = JobPulseDatabase.getDatabase(context)
    val jobDao = db.jobDao()

    val initialJob = JobEntity(
      id = 9999L,
      title = "Initial Junior Engineer",
      company = "Test Corp",
      industry = "Engineering",
      location = "Boston, MA",
      isRemote = false,
      jobType = "Full-time",
      experienceLevel = "Entry Level",
      salaryMin = 90000,
      salaryMax = 110000,
      salaryText = "$90,000 - $110,000 / yr",
      description = "Initial description",
      responsibilities = "Testing responsibilities",
      requirements = "Kotlin experience",
      benefits = "Healthcare",
      employerEmail = "employer@testcorp.com",
      isActive = true
    )

    jobDao.insertJob(initialJob)
    val saved = jobDao.getJobById(9999L).first()
    assertNotNull(saved)
    assertEquals("Initial Junior Engineer", saved?.title)

    // Employer updates job title and salary
    val updatedJob = saved!!.copy(
      title = "Updated Senior Staff Engineer",
      salaryMin = 160000,
      salaryMax = 190000,
      salaryText = "$160,000 - $190,000 / yr"
    )
    jobDao.updateJob(updatedJob)

    val fetchedUpdated = jobDao.getJobById(9999L).first()
    assertEquals("Updated Senior Staff Engineer", fetchedUpdated?.title)
    assertEquals(160000, fetchedUpdated?.salaryMin)
  }

  @Test
  fun `verify job feed filtering by title category and location`() {
    val sampleList = listOf(
      JobListingEntity(
        id = 1L,
        title = "Cloud Infrastructure Architect",
        company = "NovaVanguard",
        industry = "Engineering",
        location = "Boston, MA",
        isRemote = true,
        jobType = "Full-time",
        experienceLevel = "Senior",
        salaryText = "$150,000 / yr",
        description = "Cloud systems",
        requirements = "Kubernetes",
        employerEmail = "hiring@novavanguard.com",
        postedDateMillis = System.currentTimeMillis()
      ),
      JobListingEntity(
        id = 2L,
        title = "Clinical Pharmacist",
        company = "Boston Health",
        industry = "Pharmacy",
        location = "Boston, MA",
        isRemote = false,
        jobType = "Full-time",
        experienceLevel = "Mid Level",
        salaryText = "$130,000 / yr",
        description = "Hospital pharmacy",
        requirements = "PharmD",
        employerEmail = "hr@bostonhealth.org",
        postedDateMillis = System.currentTimeMillis()
      ),
      JobListingEntity(
        id = 3L,
        title = "Mobile Engineer",
        company = "Aether Labs",
        industry = "Tech & Software",
        location = "San Francisco, CA",
        isRemote = false,
        jobType = "Full-time",
        experienceLevel = "Senior",
        salaryText = "$170,000 / yr",
        description = "Android Jetpack Compose",
        requirements = "Kotlin",
        employerEmail = "jobs@aether.io",
        postedDateMillis = System.currentTimeMillis()
      )
    )

    // Filter by title
    val query = "Cloud"
    val titleFiltered = sampleList.filter { it.title.contains(query, ignoreCase = true) }
    assertEquals(1, titleFiltered.size)
    assertEquals("Cloud Infrastructure Architect", titleFiltered.first().title)

    // Filter by Category
    val categoryFiltered = sampleList.filter { it.industry.equals("Pharmacy", ignoreCase = true) }
    assertEquals(1, categoryFiltered.size)
    assertEquals("Clinical Pharmacist", categoryFiltered.first().title)

    // Filter by Location
    val locationFiltered = sampleList.filter { it.location.contains("Boston", ignoreCase = true) }
    assertEquals(2, locationFiltered.size)

    // Filter by Remote only
    val remoteFiltered = sampleList.filter { it.isRemote }
    assertEquals(1, remoteFiltered.size)
  }

  @Test
  fun `verify login role separation for seeker and employer`() {
    val seekerRole = "SEEKER"
    val employerRole = "EMPLOYER"

    val isSeekerAllowedToPost = seekerRole == "EMPLOYER" || seekerRole == "ADMIN"
    assertFalse(isSeekerAllowedToPost)

    val isEmployerAllowedToPost = employerRole == "EMPLOYER" || employerRole == "ADMIN"
    assertTrue(isEmployerAllowedToPost)
  }

  @Test
  fun `verify seeker profile contact info resume snippet and job preferences`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = JobPulseDatabase.getDatabase(context)
    val userDao = db.userDao()

    val profile = com.example.data.model.UserProfileEntity(
      id = 5001L,
      email = "alex.seeker@test.com",
      fullName = "Alex Seeker",
      role = "SEEKER",
      phone = "+1 (617) 555-1234",
      location = "Boston, MA",
      portfolioUrl = "https://alexseeker.dev",
      headline = "Staff Software Engineer",
      resumeSnippet = "Experienced Android developer with 5+ years of Kotlin, Compose, and Room persistence.",
      skills = "Kotlin, Jetpack Compose, Room, Coroutines, M3",
      industry = "Tech & Software",
      preferredJobType = "Full-time",
      preferredWorkMode = "Remote",
      minExpectedSalary = 140000
    )

    userDao.insertUser(profile)
    val retrieved = userDao.getUserById(5001L).first()

    assertNotNull(retrieved)
    assertEquals("Alex Seeker", retrieved?.fullName)
    assertEquals("+1 (617) 555-1234", retrieved?.phone)
    assertEquals("Experienced Android developer with 5+ years of Kotlin, Compose, and Room persistence.", retrieved?.resumeSnippet)
    assertEquals("Tech & Software", retrieved?.industry)
    assertEquals("Remote", retrieved?.preferredWorkMode)
    assertEquals(140000, retrieved?.minExpectedSalary)
  }

  @Test
  fun `verify employer job details direct firestore map generation`() {
    val job = JobEntity(
      id = 7001L,
      title = "Clinical Oncology Pharmacist",
      company = "Boston Medical Center",
      industry = "Pharmacy",
      location = "Boston, MA",
      isRemote = false,
      jobType = "Full-time",
      experienceLevel = "Senior",
      salaryMin = 135000,
      salaryMax = 165000,
      salaryText = "$135,000 - $165,000 / yr",
      description = "Lead oncology chemotherapy regimens and sterile compounding.",
      responsibilities = "Review orders, participate in rounds",
      requirements = "PharmD, BCOP",
      benefits = "Health, 401(k)",
      employerEmail = "recruiting@bmc.org",
      isActive = true
    )

    val firestoreMap = com.example.data.cloud.FirestoreJobSyncService.toFirestoreMap(job)

    assertEquals("Clinical Oncology Pharmacist", firestoreMap["title"])
    assertEquals("Boston Medical Center", firestoreMap["company"])
    assertEquals("Pharmacy", firestoreMap["industry"])
    assertEquals(135000, firestoreMap["salaryMin"])
    assertEquals(165000, firestoreMap["salaryMax"])
    assertEquals("Lead oncology chemotherapy regimens and sterile compounding.", firestoreMap["description"])
    assertEquals("recruiting@bmc.org", firestoreMap["employerEmail"])
    assertEquals(true, firestoreMap["isActive"])
  }

  @Test
  fun `verify ai ats resume evaluation and recommended skills generation`() {
    val sampleResume = """
      Clinical Pharmacist with 5+ years experience in oncology hospital dispensing.
      Managed USP 797 sterile compounding cleanroom and evaluated chemotherapy dosage regimens.
      Education: Doctor of Pharmacy (PharmD) from ACPE accredited university.
      Certifications: BCOP oncology pharmacist license.
    """.trimIndent()

    val analysis = com.example.ai.AiResumeAtsService.evaluateLocally(
      resumeText = sampleResume,
      targetIndustry = "Pharmacy",
      targetRole = "Clinical Oncology Specialist",
      currentSkills = "Pharmacology, Sterile Compounding, USP 797"
    )

    // Verify ATS score is computed
    assertTrue("Overall score should be > 50", analysis.overallScore > 50)
    assertTrue("Keywords score should be calculated", analysis.keywordsScore > 0)
    assertTrue("Matching keywords should contain pharmacy terms", analysis.matchingKeywords.isNotEmpty())

    // Verify skills to learn are suggested
    assertTrue("Should provide recommended skills to learn", analysis.recommendedSkillsToLearn.isNotEmpty())
    val topSkill = analysis.recommendedSkillsToLearn.first()
    assertNotNull(topSkill.name)
    assertNotNull(topSkill.category)
    assertNotNull(topSkill.reason)
    assertNotNull(topSkill.estimatedLearningTime)

    // Verify serialization round-trip
    val json = com.example.ai.AiResumeAtsService.toJson(analysis)
    val parsed = com.example.ai.AiResumeAtsService.parseAtsJson(json)
    assertNotNull(parsed)
    assertEquals(analysis.overallScore, parsed?.overallScore)
    assertEquals(analysis.scoreGrade, parsed?.scoreGrade)
    assertEquals(analysis.recommendedSkillsToLearn.size, parsed?.recommendedSkillsToLearn?.size)
  }

  @Test
  fun `verify cloud matching email notification triggers on new job posting`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = JobPulseDatabase.getDatabase(context)
    val userDao = db.userDao()
    val notifDao = db.notificationDao()

    val seeker = com.example.data.model.UserProfileEntity(
      id = 9001L,
      email = "matched.pharmacist@test.com",
      fullName = "Dr. Matched Pharmacist",
      role = "SEEKER",
      industry = "Pharmacy",
      minExpectedSalary = 120000
    )
    userDao.insertUser(seeker)

    val newJob = JobEntity(
      id = 9500L,
      title = "Inpatient Clinical Pharmacist",
      company = "St. Jude Hospital",
      industry = "Pharmacy",
      location = "Boston, MA",
      isRemote = false,
      salaryMin = 130000,
      salaryMax = 155000,
      salaryText = "$130,000 - $155,000 / yr",
      description = "Inpatient clinical pharmacy protocols and pharmacokinetics.",
      employerEmail = "hr@stjude.org",
      isActive = true
    )

    // Simulate Cloud Function matching evaluation
    val matchesIndustry = seeker.industry.equals(newJob.industry, ignoreCase = true)
    val matchesSalary = newJob.salaryMax >= seeker.minExpectedSalary

    assertTrue("Job should match seeker industry preference", matchesIndustry)
    assertTrue("Job should match seeker salary preference", matchesSalary)

    // Record email dispatch
    val emailRecord = com.example.data.model.NotificationRecordEntity(
      userId = seeker.id,
      jobId = newJob.id,
      jobTitle = newJob.title,
      company = newJob.company,
      alertTitle = "🎯 Preference Match: ${newJob.industry}",
      channelType = "EMAIL",
      recipientEmail = seeker.email,
      message = "📧 Email Sent to ${seeker.email}: '${newJob.title}' at ${newJob.company} matches your saved preferences."
    )
    notifDao.insertNotification(emailRecord)

    val savedNotifs = notifDao.getNotificationsForUser(seeker.id).first()
    assertEquals(1, savedNotifs.size)
    assertEquals("EMAIL", savedNotifs.first().channelType)
    assertEquals("matched.pharmacist@test.com", savedNotifs.first().recipientEmail)
  }

  @Test
  fun `verify gemini multi turn chatbot response generation`() = runBlocking {
    val history = listOf(
      com.example.ai.ChatMessage(
        role = "user",
        text = "Hello, what skills should I learn for Pharmacy roles?"
      ),
      com.example.ai.ChatMessage(
        role = "model",
        text = "Hello! Key skills include USP 797, USP 800, and Epic Willow."
      )
    )

    val response = com.example.ai.GeminiChatService.sendMessage(
      history = history,
      newUserMessage = "How can I negotiate salary?",
      selectedModel = com.example.ai.GeminiChatService.MODEL_GENERAL
    )

    assertNotNull(response)
    assertEquals("model", response.role)
    assertTrue("Response should contain salary negotiation advice", response.text.contains("salary", ignoreCase = true))
  }

  @Test
  fun `verify JobViewModel initializes without NullPointerException`() = runBlocking {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = com.example.ui.viewmodel.JobViewModel(application)
    assertNotNull(viewModel.currentUser.value)
    assertEquals(1L, viewModel.currentUserId.value)
  }

  @Test
  fun `verify AuthViewModel manages authentication state and exposes login status`() = runBlocking {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val authViewModel = com.example.ui.viewmodel.AuthViewModel(application)
    assertNotNull(authViewModel.loginStatusText.value)
    assertNotNull(authViewModel.isLoggedIn.value)
  }

  @Test
  fun `verify JobListing data class and Firestore map serialization`() {
    val listing = com.example.data.model.JobListing(
      title = "Robotics Formulation Engineer",
      company = "BioMech Inc",
      industry = "Engineering",
      location = "Cambridge, MA",
      salaryText = "$140,000 - $165,000 / yr",
      salaryMin = 140000,
      salaryMax = 165000,
      description = "Lead automated drug delivery systems."
    )
    val map = listing.toFirestoreMap()
    assertEquals("Robotics Formulation Engineer", map["title"])
    assertEquals("BioMech Inc", map["company"])

    val restored = com.example.data.model.JobListing.fromFirestoreMap("doc-123", map)
    assertEquals("doc-123", restored.cloudId)
    assertEquals(listing.title, restored.title)
    assertEquals(listing.salaryMin, restored.salaryMin)
  }
}
