package com.example.celestia

import android.app.Application
import com.example.celestia.utils.AppLifecycleTracker
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.celestia.work.CelestiaAlertWorker

/**
 * Custom Application class for Celestia.
 *
 * This class performs global, app-wide initialization such as:
 *
 * - Registering [AppLifecycleTracker] to monitor foreground/background state
 * - Scheduling background workers (Kp Index alert checks via WorkManager)
 *
 * WorkManager allows Celestia to deliver Kp Index alerts even when
 * the app is fully closed, ensuring reliable background notifications.
 */
class CelestiaApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ---------------------------------------------------------------------
        // 1. Track app foreground / background state
        // ---------------------------------------------------------------------
        AppLifecycleTracker.init()

        // ---------------------------------------------------------------------
        // 2. Periodic Worker for Kp Alerts
        //
        // This runs every 15 minutes — WorkManager’s minimum interval.
        // ---------------------------------------------------------------------

        val workRequest =
            PeriodicWorkRequestBuilder<CelestiaAlertWorker>(15, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "kpAlertWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )


        // ---------------------------------------------------------------------
        // 3. One-Time Worker — for testing only
        //
        // Runs once, after a 1-minute delay. Ideal for development
        // when you want to verify background alerts without waiting.
        // ---------------------------------------------------------------------
        /*
        val kpTestWork =
            OneTimeWorkRequestBuilder<KpAlertWorker>()
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueue(kpTestWork)

         */
    }
}
