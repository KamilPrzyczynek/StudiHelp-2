package com.example.studihelp.ui.Notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.example.studihelp.R

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "tasks_channel"
    private val CHANNEL_NAME = "Tasks Channel"
    private val NOTIFICATION_ID = 101

    fun createNotification(title: String, description: String) {
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }
}
