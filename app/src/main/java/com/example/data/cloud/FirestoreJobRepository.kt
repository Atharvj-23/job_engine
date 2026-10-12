package com.example.data.cloud

import android.util.Log
import com.example.data.model.JobListing
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Basic Firestore repository to facilitate reading and writing job postings.
 */
class FirestoreJobRepository {

  private val firestore: FirebaseFirestore?
    get() = try {
      val app = FirebaseApp.getInstance()
      val dbId = "ai-studio-android-jobengin-c66c8069-7c44-48b8-ad92-73596dc8cd97"
      try {
        FirebaseFirestore.getInstance(app, dbId)
      } catch (e: Exception) {
        FirebaseFirestore.getInstance(app)
      }
    } catch (e: Exception) {
      Log.w(TAG, "FirebaseFirestore instance not ready: ${e.message}")
      null
    }

  /**
   * Real-time Flow of all job listings in Firestore.
   */
  fun getJobListings(): Flow<List<JobListing>> = callbackFlow {
    val db = firestore
    if (db == null) {
      trySend(emptyList())
      close()
      return@callbackFlow
    }

    val subscription = db.collection(COLLECTION_JOBS)
      .orderBy("postedDateMillis", Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Error listening for job postings: ${error.message}")
          return@addSnapshotListener
        }

        val listings = snapshot?.documents?.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          JobListing.fromFirestoreMap(doc.id, data)
        } ?: emptyList()

        trySend(listings)
      }

    awaitClose { subscription.remove() }
  }

  /**
   * Writes a new job posting to Firestore.
   * Returns the generated Document ID.
   */
  suspend fun postJobListing(listing: JobListing): Result<String> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
      val docRef = db.collection(COLLECTION_JOBS).add(listing.toFirestoreMap()).await()
      Log.i(TAG, "Job posting created in Firestore with ID: ${docRef.id}")
      Result.success(docRef.id)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to post job listing to Firestore: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Updates an existing job posting in Firestore.
   */
  suspend fun updateJobListing(documentId: String, listing: JobListing): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
      db.collection(COLLECTION_JOBS).document(documentId).set(listing.toFirestoreMap()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to update job listing $documentId: ${e.message}", e)
      Result.failure(e)
    }
  }

  /**
   * Deletes a job posting from Firestore.
   */
  suspend fun deleteJobListing(documentId: String): Result<Unit> {
    return try {
      val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
      db.collection(COLLECTION_JOBS).document(documentId).delete().await()
      Log.i(TAG, "Job posting $documentId deleted from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to delete job listing $documentId: ${e.message}", e)
      Result.failure(e)
    }
  }

  companion object {
    private const val TAG = "FirestoreJobRepo"
    private const val COLLECTION_JOBS = "jobs"
  }
}
