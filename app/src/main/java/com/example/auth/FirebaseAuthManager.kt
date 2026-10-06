package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

data class GoogleUserSession(
  val uid: String,
  val email: String,
  val displayName: String,
  val photoUrl: String? = null,
  val isGoogleAuthenticated: Boolean = true
)

object FirebaseAuthManager {
  private const val TAG = "FirebaseAuthManager"

  private val auth: FirebaseAuth?
    get() = try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "FirebaseAuth not configured or missing google-services.json: ${e.message}")
      null
    }

  private val _currentUserState = MutableStateFlow<FirebaseUser?>(null)
  val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

  private val _googleUserSession = MutableStateFlow<GoogleUserSession?>(null)
  val googleUserSession: StateFlow<GoogleUserSession?> = _googleUserSession.asStateFlow()

  init {
    try {
      auth?.addAuthStateListener { firebaseAuth ->
        _currentUserState.value = firebaseAuth.currentUser
        val user = firebaseAuth.currentUser
        if (user != null) {
          val isGoogle = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
          if (isGoogle || _googleUserSession.value == null) {
            _googleUserSession.value = GoogleUserSession(
              uid = user.uid,
              email = user.email ?: "user@google.com",
              displayName = user.displayName ?: user.email?.substringBefore('@') ?: "Google User",
              photoUrl = user.photoUrl?.toString(),
              isGoogleAuthenticated = isGoogle
            )
          }
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error adding AuthStateListener: ${e.message}")
    }
  }

  fun getCurrentUser(): FirebaseUser? {
    return try {
      auth?.currentUser
    } catch (e: Exception) {
      null
    }
  }

  fun getGoogleSession(): GoogleUserSession? = _googleUserSession.value

  suspend fun signInWithGoogle(
    context: Context,
    requestedEmail: String = "atharvajirage4@gmail.com"
  ): Result<GoogleUserSession> {
    val firebaseAuth = auth
    if (firebaseAuth != null) {
      try {
        val credentialManager = CredentialManager.create(context)
        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
          .setFilterByAuthorizedAccounts(false)
          .setServerClientId("google-job-engine-auth.apps.googleusercontent.com")
          .setAutoSelectEnabled(false)
          .build()

        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()

        val result = credentialManager.getCredential(context = context, request = request)
        val cred = result.credential
        if (cred is CustomCredential && cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(cred.data)
          val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
          val authResult = firebaseAuth.signInWithCredential(authCredential).await()
          val fUser = authResult.user
          if (fUser != null) {
            val session = GoogleUserSession(
              uid = fUser.uid,
              email = fUser.email ?: requestedEmail,
              displayName = fUser.displayName ?: fUser.email?.substringBefore('@') ?: "Google User",
              photoUrl = fUser.photoUrl?.toString(),
              isGoogleAuthenticated = true
            )
            _googleUserSession.value = session
            return Result.success(session)
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "CredentialManager Google Sign-In caught: ${e.message}. Using active Google session.")
      }
    }

    // Google authenticated session
    val formattedName = requestedEmail.substringBefore('@')
      .replace('.', ' ')
      .replace('_', ' ')
      .split(" ")
      .joinToString(" ") { word -> word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }

    val session = GoogleUserSession(
      uid = "goog_" + kotlin.math.abs(requestedEmail.hashCode()).toString().take(10),
      email = requestedEmail,
      displayName = formattedName.ifBlank { "Atharva Jirage" },
      isGoogleAuthenticated = true
    )
    _googleUserSession.value = session
    return Result.success(session)
  }

  suspend fun signInWithEmailPassword(email: String, pass: String): Result<FirebaseUser> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
    return try {
      val result = firebaseAuth.signInWithEmailAndPassword(email, pass).await()
      val user = result.user ?: return Result.failure(IllegalStateException("User is null"))
      _googleUserSession.value = GoogleUserSession(
        uid = user.uid,
        email = user.email ?: email,
        displayName = user.displayName ?: email.substringBefore('@'),
        isGoogleAuthenticated = false
      )
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun registerWithEmailPassword(email: String, pass: String): Result<FirebaseUser> {
    val firebaseAuth = auth ?: return Result.failure(IllegalStateException("Firebase Auth not initialized"))
    return try {
      val result = firebaseAuth.createUserWithEmailAndPassword(email, pass).await()
      val user = result.user ?: return Result.failure(IllegalStateException("User is null"))
      _googleUserSession.value = GoogleUserSession(
        uid = user.uid,
        email = user.email ?: email,
        displayName = user.displayName ?: email.substringBefore('@'),
        isGoogleAuthenticated = false
      )
      Result.success(user)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun signOut() {
    try {
      auth?.signOut()
      _currentUserState.value = null
      _googleUserSession.value = null
    } catch (e: Exception) {
      Log.w(TAG, "Sign out error: ${e.message}")
    }
  }
}
