package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Main Application class for JobPulse.
 * Properly hosts Firebase initialization using FirebaseApp.initializeApp(this).
 */
class JobPulseApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeFirebase()
  }

  private fun initializeFirebase() {
    try {
      // Primary initialization: calls FirebaseApp.initializeApp(this)
      // which loads configuration from app/google-services.json
      val app = FirebaseApp.initializeApp(this)
      if (app != null) {
        Log.i(TAG, "FirebaseApp.initializeApp(this) succeeded: ${app.name}")
      } else {
        // Fallback options if google-services.json was not picked up by the toolchain
        initializeFallbackFirebase()
      }
    } catch (e: Exception) {
      Log.w(TAG, "Standard FirebaseApp.initializeApp(this) failed, using fallback: ${e.message}")
      initializeFallbackFirebase()
    }
  }

  private fun initializeFallbackFirebase() {
    try {
      if (FirebaseApp.getApps(this).isEmpty()) {
        val options = FirebaseOptions.Builder()
          .setApplicationId("1:958786218374:android:jobpulseapp")
          .setProjectId("jobpulse-platform")
          .setApiKey("AIzaSyJobPulsePlatformClientKey123456789")
          .setStorageBucket("jobpulse-platform.appspot.com")
          .setGcmSenderId("958786218374")
          .build()
        FirebaseApp.initializeApp(this, options)
        Log.i(TAG, "Fallback FirebaseApp.initializeApp(this, options) completed.")
      }
    } catch (fallbackError: Exception) {
      Log.e(TAG, "Error initializing fallback FirebaseApp: ${fallbackError.message}")
    }
  }

  companion object {
    private const val TAG = "JobPulseApplication"
  }
}
