package com.example.celestia.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.celestia.MainActivity
import com.example.celestia.R

object NotificationHelper {

    private const val CHANNEL_ID_KP = "kp_alert_channel"
    private const val CHANNEL_ID_ISS = "iss_alert_channel"

    private fun createChannel(context: Context, id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                id,
                name,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }

    fun sendKpNotification(context: Context, message: String) {
        createChannel(context, CHANNEL_ID_KP, "Kp Alerts")
        sendInternal(context, CHANNEL_ID_KP, "Kp Index Alert", message)
    }

    fun sendIssProximityNotification(context: Context, distance: Double) {
        createChannel(context, CHANNEL_ID_ISS, "ISS Alerts")
        sendInternal(
            context,
            CHANNEL_ID_ISS,
            "ISS Nearby",
            "The ISS is within ${distance.toInt()} km of your location"
        )
    }

    private fun sendInternal(context: Context, channelId: String, title: String, message: String) {

        val manager = context.getSystemService(NotificationManager::class.java)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
