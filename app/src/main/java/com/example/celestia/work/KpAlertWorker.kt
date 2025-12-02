package com.example.celestia.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.datastore.preferences.core.edit
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.store.ThemeKeys
import com.example.celestia.data.store.themeDataStore
import com.example.celestia.notifications.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

/**
 * Worker responsible for **background Kp Index alerting**, running even when the
 * Celestia app is not open.
 *
 * This Worker:
 * 1. Fetches the latest Kp Index from NOAA (via repository)
 * 2. Checks user notification preferences in DataStore
 * 3. Determines whether an alert should be fired
 * 4. Sends a system notification via [NotificationHelper]
 * 5. Stores the last alerted Kp value to avoid duplicates
 *
 * The Worker is scheduled using WorkManager (periodic or one-time),
 * allowing Celestia to deliver storm alerts reliably in the background.
 */
class KpAlertWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    /**
     * Performs the background refresh + alert logic.
     *
     * @return [Result.success] always, since failures should not retry repeatedly.
     */
    override suspend fun doWork(): Result {
        val dao = CelestiaDatabase.getInstance(ctx).celestiaDao()
        val repo = com.example.celestia.data.repository.CelestiaRepository(dao, ctx)

        // ---------------------------------------------------------------------
        // 1. Refresh Kp Index from network (NOAA)
        // ---------------------------------------------------------------------
        repo.refreshKpIndex()

        // ---------------------------------------------------------------------
        // 2. Read the latest stored Kp value
        // ---------------------------------------------------------------------
        val latestKp = repo.readings.firstOrNull()?.firstOrNull()?.estimatedKp ?: 0.0

        // ---------------------------------------------------------------------
        // 3. Load Kp alert preferences from DataStore
        // ---------------------------------------------------------------------
        val prefsFlow = ctx.themeDataStore.data

        val alertsEnabled = prefsFlow.map {
            it[ThemeKeys.KP_ALERTS_ENABLED] ?: false
        }.first()

        val lastAlerted = prefsFlow.map {
            it[ThemeKeys.LAST_ALERTED_KP] ?: -1f
        }.first()

        // If user disabled alerts, stop silently
        if (!alertsEnabled) return Result.success()

        // ---------------------------------------------------------------------
        // 4. If Kp dropped below storm threshold, reset tracking & exit
        // Threshold here is Kp < 5.0 (strong storm)
        // ---------------------------------------------------------------------
        if (latestKp < 5.0) {
            ctx.themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = -1f
            }
            return Result.success()
        }

        // ---------------------------------------------------------------------
        // 5. Trigger alert ONLY if Kp changed since the last notification
        // ---------------------------------------------------------------------
        if (latestKp.toFloat() != lastAlerted) {

            NotificationHelper.send(
                context = ctx,
                title = "Kp Index Alert",
                message = "Kp Index has reached $latestKp"
            )

            // Update the last alerted Kp
            ctx.themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = latestKp.toFloat()
            }
        }

        return Result.success()
    }
}
