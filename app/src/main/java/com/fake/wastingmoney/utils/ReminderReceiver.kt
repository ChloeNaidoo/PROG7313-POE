package com.fake.wastingmoney.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log // Added for logging

class ReminderReceiver : BroadcastReceiver() {

    private val TAG = "ReminderReceiver" // Tag for logging

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "ReminderReceiver onReceive triggered!")

        val channelId = "expense_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create the NotificationChannel for Android 8.0 (Oreo) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for expense reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created (or already exists).")
        }

        // Build the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a built-in icon as a placeholder
            .setContentTitle("Expense Reminder")
            .setContentText("Don't forget to record your upcoming expense!")
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set priority for older Android versions
            .setAutoCancel(true) // Dismiss notification when tapped
            .build()

        // Generate a unique notification ID. Using System.currentTimeMillis() is common.
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.i(TAG, "Notification displayed with ID: $notificationId")
    }
}