package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.model.JobEntity

object NotificationHelper {
  private const val CHANNEL_ID = "jobpulse_alerts_channel"
  private const val CHANNEL_NAME = "JobPulse Job Alerts"
  private const val CHANNEL_DESC = "Notifications for matching job openings in Engineering, Pharmacy, and other fields."

  fun initChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_HIGH
      val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
        description = CHANNEL_DESC
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 250, 150, 250)
      }
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  fun sendJobAlertNotification(
    context: Context,
    alertTitle: String,
    job: JobEntity
  ) {
    // Check permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          android.Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    initChannel(context)

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("TARGET_JOB_ID", job.id)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      job.id.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle("🎯 Alert Match: ${job.title}")
      .setContentText("${job.company} • ${job.location} • ${job.salaryText}")
      .setStyle(
        NotificationCompat.BigTextStyle().bigText(
          "Matching alert: '$alertTitle'\n" +
              "Role: ${job.title} at ${job.company}\n" +
              "Industry: ${job.industry} | Location: ${job.location}\n" +
              "Salary: ${job.salaryText}\n\n" +
              job.description.take(150) + "..."
        )
      )
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notifId = (System.currentTimeMillis() % 100000).toInt() + job.id.toInt()
    notificationManager.notify(notifId, notification)
  }
}
