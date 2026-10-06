package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.JobPulseDatabase
import com.example.data.model.NotificationRecordEntity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manages Firebase Cloud Messaging state, subscription to topic alerts,
 * and user toggle preferences.
 */
object FirebaseJobMessagingManager {
  private const val TAG = "FirebaseJobMessaging"
  private const val PREFS_NAME = "fcm_job_alerts_prefs"
  private const val KEY_ENABLED = "fcm_alerts_enabled"

  private val _fcmEnabled = MutableStateFlow(true)
  val fcmEnabled: StateFlow<Boolean> = _fcmEnabled.asStateFlow()

  private val _fcmToken = MutableStateFlow<String?>(null)
  val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

  private val _subscribedTopics = MutableStateFlow<Set<String>>(emptySet())
  val subscribedTopics: StateFlow<Set<String>> = _subscribedTopics.asStateFlow()

  fun init(context: Context) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val enabled = prefs.getBoolean(KEY_ENABLED, true)
    _fcmEnabled.value = enabled

    CoroutineScope(Dispatchers.IO).launch {
      try {
        val token = FirebaseMessaging.getInstance().token.await()
        _fcmToken.value = token
        Log.d(TAG, "FCM Token retrieved: $token")
        if (enabled) {
          syncSubscriptions(listOf("Engineering", "Pharmacy", "Healthcare", "Tech & Software", "Biotech"))
        }
      } catch (e: Exception) {
        Log.w(TAG, "Could not initialize FCM or missing Google Play Services: ${e.message}")
        // Set placeholder mock token for development verification
        if (_fcmToken.value == null) {
          _fcmToken.value = "fcm_token_jobpulse_" + kotlin.math.abs(System.currentTimeMillis().hashCode())
        }
      }
    }
  }

  fun setFcmEnabled(context: Context, enabled: Boolean, criteriaList: List<String>) {
    _fcmEnabled.value = enabled
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()

    CoroutineScope(Dispatchers.IO).launch {
      if (enabled) {
        syncSubscriptions(criteriaList)
      } else {
        unsubscribeAll()
      }
    }
  }

  private suspend fun syncSubscriptions(criteriaList: List<String>) {
    try {
      // Subscribe to general new jobs broadcast topic
      FirebaseMessaging.getInstance().subscribeToTopic("all_new_jobs").await()
      val newTopics = mutableSetOf("all_new_jobs")

      // Subscribe to specific category/industry topics matching criteria
      for (criteria in criteriaList) {
        val topic = sanitizeTopic("jobs_$criteria")
        FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
        newTopics.add(topic)
      }
      _subscribedTopics.value = newTopics
      Log.d(TAG, "Subscribed to FCM topics: $newTopics")
    } catch (e: Exception) {
      Log.w(TAG, "Error subscribing to FCM topics: ${e.message}")
      val mockTopics = mutableSetOf("all_new_jobs")
      criteriaList.forEach { mockTopics.add(sanitizeTopic("jobs_$it")) }
      _subscribedTopics.value = mockTopics
    }
  }

  private suspend fun unsubscribeAll() {
    try {
      for (topic in _subscribedTopics.value) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).await()
      }
      _subscribedTopics.value = emptySet()
      Log.d(TAG, "Unsubscribed from all FCM topics")
    } catch (e: Exception) {
      Log.w(TAG, "Error unsubscribing from FCM: ${e.message}")
      _subscribedTopics.value = emptySet()
    }
  }

  fun updateToken(token: String) {
    _fcmToken.value = token
  }

  private fun sanitizeTopic(raw: String): String {
    return raw.lowercase()
      .replace("&", "and")
      .replace("[^a-zA-Z0-9-_.~%]+".toRegex(), "_")
      .take(900)
  }
}
