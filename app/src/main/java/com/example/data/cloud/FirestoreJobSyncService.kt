package com.example.data.cloud

import android.util.Log
import com.example.data.model.JobEntity
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object FirestoreJobSyncService {
  private const val TAG = "FirestoreJobSync"
  private const val COLLECTION_JOBS = "jobs"

  private val firestore: FirebaseFirestore?
    get() = try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      Log.w(TAG, "FirebaseFirestore instance not ready or unconfigured: ${e.message}")
      null
    }

  fun toFirestoreMap(job: JobEntity): Map<String, Any?> {
    return mapOf(
      "title" to job.title,
      "company" to job.company,
      "industry" to job.industry,
      "location" to job.location,
      "isRemote" to job.isRemote,
      "jobType" to job.jobType,
      "experienceLevel" to job.experienceLevel,
      "salaryMin" to job.salaryMin,
      "salaryMax" to job.salaryMax,
      "salaryText" to job.salaryText,
      "description" to job.description,
      "responsibilities" to job.responsibilities,
      "requirements" to job.requirements,
      "benefits" to job.benefits,
      "postedDateMillis" to job.postedDateMillis,
      "employerEmail" to job.employerEmail,
      "isActive" to job.isActive,
      "applicantCount" to job.applicantCount,
      "updatedAtMillis" to System.currentTimeMillis()
    )
  }

  fun fromFirestoreDocument(doc: DocumentSnapshot): JobEntity? {
    return try {
      val data = doc.data ?: return null
      JobEntity(
        cloudId = doc.id,
        title = data["title"] as? String ?: "",
        company = data["company"] as? String ?: "",
        industry = data["industry"] as? String ?: "Engineering",
        location = data["location"] as? String ?: "",
        isRemote = data["isRemote"] as? Boolean ?: false,
        jobType = data["jobType"] as? String ?: "Full-time",
        experienceLevel = data["experienceLevel"] as? String ?: "Mid Level",
        salaryMin = (data["salaryMin"] as? Number)?.toInt() ?: 0,
        salaryMax = (data["salaryMax"] as? Number)?.toInt() ?: 0,
        salaryText = data["salaryText"] as? String ?: "",
        description = data["description"] as? String ?: "",
        responsibilities = data["responsibilities"] as? String ?: "",
        requirements = data["requirements"] as? String ?: "",
        benefits = data["benefits"] as? String ?: "",
        postedDateMillis = (data["postedDateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
        employerEmail = data["employerEmail"] as? String ?: "",
        isActive = data["isActive"] as? Boolean ?: true,
        applicantCount = (data["applicantCount"] as? Number)?.toInt() ?: 0,
        syncStatus = JobEntity.SYNC_STATUS_SYNCED,
        lastSyncedAtMillis = System.currentTimeMillis()
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing Firestore document: ${doc.id}", e)
      null
    }
  }

  suspend fun uploadJobToCloud(job: JobEntity): Result<String> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      val map = toFirestoreMap(job)
      val collection = db.collection(COLLECTION_JOBS)
      val docRef = if (job.cloudId != null && job.cloudId.isNotBlank()) {
        collection.document(job.cloudId).apply { set(map).await() }
      } else {
        collection.add(map).await()
      }
      Result.success(docRef.id)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to upload job to Firestore: ${job.title}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchCloudJobs(): Result<List<JobEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firestore not initialized"))
    return try {
      val snapshot = db.collection(COLLECTION_JOBS)
        .whereEqualTo("isActive", true)
        .orderBy("postedDateMillis", Query.Direction.DESCENDING)
        .get()
        .await()

      val jobs = snapshot.documents.mapNotNull { fromFirestoreDocument(it) }
      Result.success(jobs)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to fetch jobs from Firestore", e)
      Result.failure(e)
    }
  }

  fun listenToCloudJobs(onJobsReceived: (List<JobEntity>) -> Unit): ListenerRegistration? {
    val db = firestore ?: return null
    return try {
      db.collection(COLLECTION_JOBS)
        .whereEqualTo("isActive", true)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Listen failed for cloud jobs: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null) {
            val jobs = snapshot.documents.mapNotNull { fromFirestoreDocument(it) }
            onJobsReceived(jobs)
          }
        }
    } catch (e: Exception) {
      Log.w(TAG, "Failed to attach Firestore listener: ${e.message}")
      null
    }
  }
}
