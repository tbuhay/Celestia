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

class KpAlertWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val dao = CelestiaDatabase.getInstance(ctx).dao()
        val repo = com.example.celestia.data.repository.CelestiaRepository(dao)

        // Refresh Kp Index
        repo.refreshKpIndex()

        // Read latest Kp
        val latestKp = repo.readings.firstOrNull()?.firstOrNull()?.estimatedKp ?: 0.0

        // Read alert settings
        val prefsFlow = ctx.themeDataStore.data
        val alertsEnabled = prefsFlow.map {
            it[ThemeKeys.KP_ALERTS_ENABLED] ?: false
        }.first()

        val lastAlerted = prefsFlow.map {
            it[ThemeKeys.LAST_ALERTED_KP] ?: -1f
        }.first()

        // If alerts are off, stop
        if (!alertsEnabled) return Result.success()

        // If Kp < 5, reset and exit
        if (latestKp < 5.0) {
            ctx.themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = -1f
            }
            return Result.success()
        }

        // Only fire if Kp changed
        if (latestKp.toFloat() != lastAlerted) {
            NotificationHelper.send(
                context = ctx,
                title = "Kp Index Alert",
                message = "Kp Index has reached $latestKp"
            )

            ctx.themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = latestKp.toFloat()
            }
        }

        return Result.success()
    }
}
