package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.FirebaseAuthManager
import com.example.auth.GoogleUserSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel that manages the user's authentication state using Firebase Auth
 * and exposes the current user's login status to the UI.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

  private val authPrefs = application.getSharedPreferences("job_engine_auth_prefs", Context.MODE_PRIVATE)

  // Current Firebase User state directly from Firebase Auth
  val currentUser: StateFlow<FirebaseUser?> = FirebaseAuthManager.currentUserState

  // Google User Session state
  val googleSession: StateFlow<GoogleUserSession?> = FirebaseAuthManager.googleUserSession

  // Current login status exposed to the UI
  val isLoggedIn: StateFlow<Boolean> = currentUser
    .map { it != null || authPrefs.getBoolean("is_logged_in", false) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authPrefs.getBoolean("is_logged_in", false))

  // Detailed human-readable login status string for UI status displays
  val loginStatusText: StateFlow<String> = currentUser
    .map { user ->
      when {
        user != null -> "Signed in as ${user.email ?: user.displayName ?: "Firebase User"}"
        authPrefs.getBoolean("is_logged_in", false) -> "Signed in (Local Session)"
        else -> "Not signed in"
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Checking status...")

  // Status message for Snackbars or UI toasts
  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  /**
   * Signs in using Google via Credential Manager.
   */
  fun signInWithGoogle(context: Context, defaultEmail: String = "atharvajirage4@gmail.com") {
    viewModelScope.launch {
      _isLoading.value = true
      val result = FirebaseAuthManager.signInWithGoogle(context, defaultEmail)
      _isLoading.value = false
      result.onSuccess { session ->
        authPrefs.edit().putBoolean("is_logged_in", true).apply()
        _statusMessage.value = "Successfully signed in: ${session.email}"
      }.onFailure { err ->
        _statusMessage.value = "Sign-in error: ${err.message}"
      }
    }
  }

  /**
   * Signs out of Firebase Auth and clears local preferences.
   */
  fun signOut() {
    viewModelScope.launch {
      FirebaseAuthManager.signOut()
      authPrefs.edit().putBoolean("is_logged_in", false).apply()
      _statusMessage.value = "Signed out successfully"
    }
  }

  /**
   * Refreshes the current user from Firebase Auth.
   */
  fun reloadUser() {
    viewModelScope.launch {
      try {
        FirebaseAuth.getInstance().currentUser?.reload()
      } catch (e: Exception) {
        _statusMessage.value = "Reload error: ${e.message}"
      }
    }
  }
}
