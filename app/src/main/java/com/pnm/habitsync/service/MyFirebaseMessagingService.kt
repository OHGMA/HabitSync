package com.pnm.habitsync.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This function catches the push notification when the app is in the FOREGROUND
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        remoteMessage.notification?.let {
            val title = it.title ?: "HabitSync"
            val body = it.body ?: "You have a new alert!"

            // Show the Android pop-up
            showNotification(title, body)
        }
    }

    // Every phone gets a unique FCM Token. You would normally save this to your database.
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM Token: $token") // Check your Logcat for this token!
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "habitsync_channel"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ requires a Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "HabitSync Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Default Android icon for now
            .setAutoCancel(true)
            .build()

        // Show the lock screen pop-up!
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}