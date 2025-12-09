package com.example.celestia.work

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.celestia.data.db.CelestiaDatabase
import com.example.celestia.data.repository.CelestiaRepository
import com.example.celestia.data.store.ThemeKeys
import com.example.celestia.data.store.themeDataStore
import com.example.celestia.notifications.NotificationHelper
import com.example.celestia.utils.LocationUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.math.*

class CelestiaAlertWorker(
    private val ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Log.d("CelestiaWorker", "Worker started successfully")

        val dao = CelestiaDatabase.getInstance(ctx).celestiaDao()
        val repo = CelestiaRepository(dao, ctx)

        checkKpAlert(repo)
        checkIssProximityAlert(repo)

        return Result.success()
    }

    // ----------------------------------------------------------
    //   K P   A L E R T   L O G I C
    // ----------------------------------------------------------
    private suspend fun checkKpAlert(repo: CelestiaRepository) {

        repo.refreshKpIndex()

        val latestKp = repo.readings.firstOrNull()?.firstOrNull()?.estimatedKp ?: 0.0

        val prefs = ctx.themeDataStore.data

        val alertsEnabled = prefs.map {
            it[ThemeKeys.KP_ALERTS_ENABLED] ?: false
        }.first()

        val lastAlerted = prefs.map {
            it[ThemeKeys.LAST_ALERTED_KP] ?: -1f
        }.first()

        if (!alertsEnabled) return

        if (latestKp < 5.0) {
            ctx.themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = -1f
            }
            return
        }

        if (latestKp.toFloat() != lastAlerted) {

            NotificationHelper.sendKpNotification(
                context = ctx,
                message = "Kp Index has reached $latestKp"
            )

            ctx.themeDataStore.edit {
                it[ThemeKeys.LAST_ALERTED_KP] = latestKp.toFloat()
            }
        }
    }

    // ----------------------------------------------------------
    //   I S S   P R O X I M I T Y   A L E R T   L O G I C
    // ----------------------------------------------------------
    private suspend fun checkIssProximityAlert(repo: CelestiaRepository) {

        val prefs = ctx.themeDataStore.data

        val proximityEnabled = prefs.map {
            it[ThemeKeys.ISS_PROXIMITY_ENABLED] ?: false
        }.first()

        if (!proximityEnabled) return

        val useDeviceLoc = prefs.map {
            it[ThemeKeys.USE_DEVICE_LOCATION] ?: false
        }.first()

        if (!useDeviceLoc) return

        val userLoc = LocationUtils.getLastKnownLocation(ctx) ?: return

        // Fetch ISS location (your existing repository already has this)
        val iss = repo.refreshIssLocation() ?: return

        val issLat = iss.latitude
        val issLon = iss.longitude

        // Distance calculation
        val distance = LocationUtils.distanceKm(
            userLoc.latitude,
            userLoc.longitude,
            issLat,
            issLon
        )

        val lastDistance = prefs.map {
            it[ThemeKeys.LAST_ISS_DISTANCE] ?: Float.MAX_VALUE
        }.first()

        val approaching = distance < lastDistance

        ctx.themeDataStore.edit {
            it[ThemeKeys.LAST_ISS_DISTANCE] = distance.toFloat()
        }

        if (!approaching || distance > 1500) return

        // Cooldown period (90 min)
        val lastAlert = prefs.map {
            it[ThemeKeys.LAST_ISS_PROX_ALERT] ?: 0L
        }.first()

        val now = System.currentTimeMillis()
        val minutes = (now - lastAlert) / 60000

        if (minutes < 90) return

        NotificationHelper.sendIssProximityNotification(
            context = ctx,
            distance = distance

        )

        ctx.themeDataStore.edit {
            it[ThemeKeys.LAST_ISS_PROX_ALERT] = now
        }
    }
}
