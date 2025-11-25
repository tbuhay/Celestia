package com.example.celestia

import android.app.Application
import com.example.celestia.utils.AppLifecycleTracker
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.example.celestia.work.KpAlertWorker

class CelestiaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppLifecycleTracker.init()

//        val workRequest =
//            PeriodicWorkRequestBuilder<KpAlertWorker>(15, TimeUnit.MINUTES)
//                .build()
//
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            "kpAlertWork",
//            ExistingPeriodicWorkPolicy.UPDATE,
//            workRequest
//        )

        // --- OPTIONAL: Testing worker (one-time, 1 minute delay) ---
        // Uncomment this to test alerts quickly

        val kpTestWork =
            OneTimeWorkRequestBuilder<KpAlertWorker>()
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()

        WorkManager.getInstance(this).enqueue(kpTestWork)

    }
}