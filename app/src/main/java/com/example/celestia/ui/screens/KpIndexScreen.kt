package com.example.celestia.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.FormatUtils

/**
 * Screen displaying detailed Kp Index information, including:
 *
 * - Current NOAA Kp value
 * - NOAA storm status + severity color coding
 * - High/low values from the most recent set of readings
 * - Human-readable storm description
 * - Hourly grouped Kp readings (trend up, down, or stable)
 * - Timestamp of last refresh, respecting 12h/24h user settings
 *
 * This screen is fed by:
 * [CelestiaViewModel.readings]              ← raw readings
 * [CelestiaViewModel.groupedKp]             ← hourly aggregates
 * [CelestiaViewModel.lastUpdated]           ← timestamp of refresh
 *
 * @param navController Used for navigation back to the dashboard.
 * @param vm ViewModel providing NOAA readings & grouped data.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpIndexScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    // User settings (12h / 24h)
    val settingsVM: SettingsViewModel = viewModel()
    val use24h by settingsVM.timeFormat24h.observeAsState(true)

    // Raw NOAA data
    val readings by vm.readings.observeAsState(emptyList())

    // Grouped hourly data from ViewModel
    val grouped by vm.groupedKp.observeAsState(emptyList())

    // Last refresh timestamp
    val lastUpdatedRaw by vm.lastUpdated.observeAsState("Never")

    val cardShape = RoundedCornerShape(14.dp)

    /**
     * Convert timestamp based on user's preferred format.
     */
    val lastUpdated = remember(lastUpdatedRaw, use24h) {
        if (lastUpdatedRaw == "Never") "Never"
        else FormatUtils.convertTimeFormat(lastUpdatedRaw, use24h)
    }

    /**
     * Limit grouped readings to the most recent 12 hours.
     */
    val recentGroups = remember(grouped) { grouped.take(12) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Kp Index",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val latest = vm.getLatestValidKp(readings)

            // ================================================================
            // No Data Available
            // ================================================================
            if (latest == null) {
                item {
                    Text(
                        "No Kp Index data available.\nReturn to Dashboard and tap Reload.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {

                // ================================================================
                // CURRENT KP INDEX SUMMARY CARD
                // ================================================================
                val kp = latest.estimatedKp
                val (status, statusColor, description) = FormatUtils.getNoaaKpInfo(kp)

                item {
                    KpSummaryCard(
                        kp = kp,
                        readings = readings,
                        status = status,
                        statusColor = statusColor,
                        description = description,
                        lastUpdated = lastUpdated,
                        cardShape = cardShape
                    )
                }

                // NOAA scale reference text
                item {
                    Text(
                        text = "NOAA Kp Scale: 0–1 Calm | 2–3 Unsettled | 4 Active | 5 Minor Storm | 6 Major Storm | 7–9 Severe/Extreme Storm",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ================================================================
                // Description Card (What Is the Kp Index?)
                // ================================================================
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics {},
                        shape = cardShape,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "The Kp Index measures global geomagnetic activity caused by solar wind and coronal mass ejections. Higher values indicate stronger geomagnetic storms and higher aurora potential.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // ================================================================
                // RECENT GROUPED READINGS
                // ================================================================
                item {
                    Text(
                        "Recent Readings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                itemsIndexed(recentGroups) { index, triple ->
                    val hour = triple.hour
                    val avg = triple.avg
                    val high = triple.high
                    val low = triple.low

                    // Color-code based on NOAA status for avg Kp
                    val (hourStatus, hourColor) = FormatUtils.getNoaaKpStatusAndColor(avg)

                    // Determine trend direction
                    val (arrowRes, trendLabel) = when {
                        index == recentGroups.lastIndex -> R.drawable.ic_no_change to "No change"
                        avg > recentGroups[index + 1].avg -> R.drawable.ic_trend_up to "Increasing Kp trend"
                        avg < recentGroups[index + 1].avg -> R.drawable.ic_trend_down to "Decreasing Kp trend"
                        else -> R.drawable.ic_no_change to "No change in Kp trend"
                    }

                    KpHourlyCard(
                        avg = avg,
                        high = high,
                        low = low,
                        hourStatus = hourStatus,
                        hourColor = hourColor,
                        arrowIconRes = arrowRes,
                        trendDescription = trendLabel,
                        epochMillis = hour.toInstant().toEpochMilli(),
                        use24h = use24h,
                        cardShape = cardShape
                    )
                }
            }
        }
    }
}

/**
 * Card showing the current Kp Index with:
 * - Large numeric Kp value
 * - NOAA status text + color
 * - Status description
 * - High/Low values from raw readings
 * - Timestamp of last update
 */
@Composable
private fun KpSummaryCard(
    kp: Double,
    readings: List<com.example.celestia.data.model.KpReading>,
    status: String,
    statusColor: Color,
    description: String,
    lastUpdated: String,
    cardShape: RoundedCornerShape
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x33FFFFFF), cardShape)
            .clearAndSetSemantics {},
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Text(
                text = "Current Kp Index",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = kp.toString(),
                style = MaterialTheme.typography.headlineLarge.copy(color = statusColor)
            )

            Text(
                text = status,
                style = MaterialTheme.typography.titleMedium.copy(color = statusColor),
                modifier = Modifier.semantics {
                    contentDescription = "Kp index status: $status"
                }
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            )

            val high = readings.maxOfOrNull { it.estimatedKp } ?: kp
            val low = readings.minOfOrNull { it.estimatedKp } ?: kp

            Text(
                text = "High: $high  |  Low: $low",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Text(
                text = "Last updated: $lastUpdated",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

/**
 * Card representing a single hourly group of Kp readings.
 * Shows:
 * - Hourly average (NOAA color-coded)
 * - Status label
 * - Timestamp of the hour
 * - High/Low values within that hour
 * - Trend indicator (arrow up, down, none)
 */
@Composable
private fun KpHourlyCard(
    avg: Double,
    high: Double,
    low: Double,
    hourStatus: String,
    hourColor: Color,
    arrowIconRes: Int,
    trendDescription: String,
    epochMillis: Long,
    use24h: Boolean,
    cardShape: RoundedCornerShape
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0x33FFFFFF), cardShape)
            .clearAndSetSemantics {},
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format("%.2f", avg),
                        style = MaterialTheme.typography.headlineSmall.copy(color = hourColor)
                    )
                    Text(
                        text = hourStatus,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Text(
                    text = FormatUtils.formatTime(epochMillis, use24h),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.End
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "High: ${"%.2f".format(high)} | Low: ${"%.2f".format(low)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )

                Image(
                    painter = painterResource(id = arrowIconRes),
                    contentDescription = trendDescription,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}