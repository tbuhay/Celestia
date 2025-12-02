package com.example.celestia.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.celestia.MainActivity
import com.example.celestia.R

/**
 * Utility object responsible for building and displaying system notifications
 * for the Celestia app.
 *
 * Currently used for:
 * - Kp Index threshold alerts
 * - Potential future notification types (ISS overhead passes, lunar events, etc.)
 *
 * This helper handles:
 * - Creating the appropriate notification channel (Android 8+)
 * - Building a notification with title, message, and tap action
 * - Routing the user back into the app (MainActivity) when tapped
 */
object NotificationHelper {

    /** Notification channel ID used for all Kp alert notifications. */
    private const val CHANNEL_ID = "kp_alert_channel"

    /**
     * Sends a high-priority system notification.
     *
     * @param context Application context used to access system services.
     * @param title Title text displayed in the notification.
     * @param message Body text displayed in the notification.
     *
     * The notification:
     * - Uses high importance so it appears immediately.
     * - Opens [MainActivity] when tapped.
     * - Auto-dismisses once opened.
     */
    fun send(context: Context, title: String, message: String) {
        val manager = context.getSystemService(NotificationManager::class.java)

        // Create notification channel (required for Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Kp Alerts",
            NotificationManager.IMPORTANCE_HIGH
        )
        manager.createNotificationChannel(channel)

        // Intent to reopen the app at the Home screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "home")
        }

        // PendingIntent wrapping the above intent
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Construct system notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)   // Opens app when tapped
            .setAutoCancel(true)               // Remove once tapped
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Push notification using timestamp as unique ID
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}