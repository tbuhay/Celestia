package com.example.celestia.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.celestia.MainActivity
import com.example.celestia.R

object NotificationHelper {

    private const val CHANNEL_ID = "kp_alert_channel"

    fun send(context: Context, title: String, message: String) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Create channel
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Kp Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        // Create intent to open HomeScreen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "home")
        }

        // Wrap it in PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Must exist
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)  // ← IMPORTANT
            .setAutoCancel(true)              // ← Dismiss when tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
