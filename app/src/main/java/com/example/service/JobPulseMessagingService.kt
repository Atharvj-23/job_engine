package com.example.service

import android.util.Log
import com.example.data.JobPulseDatabase
import com.example.data.model.NotificationRecordEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Service to process incoming Firebase Cloud Messages for newly posted jobs.
 */
class JobPulseMessagingService : FirebaseMessagingService() {

  companion object {
    private const val TAG = "JobPulseMessagingService"
  }

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New FCM Registration Token: $token")
    FirebaseJobMessagingManager.updateToken(token)
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

    // Check if user has FCM notifications toggled on
    if (!FirebaseJobMessagingManager.fcmEnabled.value) {
      Log.d(TAG, "FCM alerts disabled by user toggle, skipping delivery")
      return
    }

    val title = remoteMessage.notification?.title
      ?: remoteMessage.data["title"]
      ?: "New Job Opening Alert"

    val body = remoteMessage.notification?.body
      ?: remoteMessage.data["body"]
      ?: remoteMessage.data["message"]
      ?: "A new job matching your criteria has been posted."

    val jobId = remoteMessage.data["job_id"]?.toLongOrNull() ?: 1L
    val jobTitle = remoteMessage.data["job_title"] ?: title
    val company = remoteMessage.data["company"] ?: "Featured Employer"
    val industry = remoteMessage.data["industry"] ?: "General"

    // Persist to local Notification Database
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val database = JobPulseDatabase.getDatabase(applicationContext)
        val record = NotificationRecordEntity(
          userId = 1L,
          jobId = jobId,
          jobTitle = jobTitle,
          company = company,
          alertTitle = "FCM Push: $industry",
          channelType = "PUSH",
          recipientEmail = "active_user@jobengine.app",
          message = body,
          timestampMillis = System.currentTimeMillis(),
          isRead = false
        )
        database.notificationDao().insertNotification(record)
      } catch (e: Exception) {
        Log.e(TAG, "Failed to persist FCM notification record: ${e.message}")
      }
    }
  }
}
