package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CloudSyncDao
import com.example.data.dao.JobAlertDao
import com.example.data.dao.JobApplicationDao
import com.example.data.dao.JobDao
import com.example.data.dao.JobListingDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.SavedJobDao
import com.example.data.dao.UserDao
import com.example.data.model.CloudSyncQueueEntity
import com.example.data.model.JobAlertEntity
import com.example.data.model.JobApplicationEntity
import com.example.data.model.JobEntity
import com.example.data.model.JobListingEntity
import com.example.data.model.NotificationRecordEntity
import com.example.data.model.SavedJobEntity
import com.example.data.model.UserProfileEntity

@Database(
  entities = [
    JobEntity::class,
    JobListingEntity::class,
    UserProfileEntity::class,
    JobAlertEntity::class,
    JobApplicationEntity::class,
    SavedJobEntity::class,
    NotificationRecordEntity::class,
    CloudSyncQueueEntity::class
  ],
  version = 5,
  exportSchema = false
)
abstract class JobPulseDatabase : RoomDatabase() {
  abstract fun jobDao(): JobDao
  abstract fun jobListingDao(): JobListingDao
  abstract fun userDao(): UserDao
  abstract fun jobAlertDao(): JobAlertDao
  abstract fun jobApplicationDao(): JobApplicationDao
  abstract fun savedJobDao(): SavedJobDao
  abstract fun notificationDao(): NotificationDao
  abstract fun cloudSyncDao(): CloudSyncDao

  companion object {
    @Volatile
    private var INSTANCE: JobPulseDatabase? = null

    fun getDatabase(context: Context): JobPulseDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          JobPulseDatabase::class.java,
          "jobpulse_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
