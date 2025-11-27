package com.example.celestia.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpIndexScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    val settingsVM: SettingsViewModel = viewModel()
    val use24h by settingsVM.timeFormat24h.observeAsState(true)

    val readings by vm.readings.observeAsState(emptyList())
    val grouped by vm.groupedKp.observeAsState(emptyList())
    val lastUpdatedRaw by vm.lastUpdated.observeAsState("Never")

    val cardShape = RoundedCornerShape(14.dp)

    val lastUpdated = remember(lastUpdatedRaw, use24h) {
        if (lastUpdatedRaw == "Never") {
            "Never"
        } else {
            FormatUtils.convertTimeFormat(lastUpdatedRaw, use24h)
        }
    }

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

                // --- CURRENT KP (NOAA SCALE) ---
                val kp = latest.estimatedKp
                val (status, statusColor, description) = FormatUtils.getNoaaKpInfo(kp)

                // Current KP card
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFFFFF), cardShape)
                            .clearAndSetSemantics { },
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
                                ),
                            )

                            Text(
                                text = kp.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = statusColor
                                )
                            )

                            Text(
                                text = status,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = statusColor
                                ),
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

                // Kp scale explanation (NOAA style)
                item {
                    Text(
                        text = "NOAA Kp Scale: 0–2 Calm | 2–3 Calm | 4 Disturbed | 5 Moderate Storm | 6 Strong Storm | 7 Severe Storm | 8-9 Extreme",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Short explanation card
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clearAndSetSemantics { },
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

                // Recent readings header
                item {
                    Text(
                        "Recent Readings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Recent grouped readings (hourly aggregates)
                itemsIndexed(recentGroups) { index, triple ->

                    val hour = triple.hour
                    val avg = triple.avg
                    val high = triple.high
                    val low = triple.low

                    val (hourStatus, hourColor) = FormatUtils.getNoaaKpStatusAndColor(avg)

                    val trendDescription: String
                    val arrowIconRes: Int

                    if (index == recentGroups.lastIndex) {
                        arrowIconRes = R.drawable.ic_no_change
                        trendDescription = "No change in Kp trend"
                    } else {
                        val prevAvg = recentGroups[index + 1].avg

                        when {
                            avg > prevAvg -> {
                                arrowIconRes = R.drawable.ic_trend_up
                                trendDescription = "Increasing Kp trend"
                            }
                            avg < prevAvg -> {
                                arrowIconRes = R.drawable.ic_trend_down
                                trendDescription = "Decreasing Kp trend"
                            }
                            else -> {
                                arrowIconRes = R.drawable.ic_no_change
                                trendDescription = "No change in Kp trend"
                            }
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFFFFF), cardShape)
                            .clearAndSetSemantics { },
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
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            color = hourColor
                                        )
                                    )
                                    Text(
                                        text = hourStatus,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                }

                                Text(
                                    text = FormatUtils.formatTime(
                                        hour.toInstant().toEpochMilli(),
                                        use24h
                                    ),
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
            }
        }
    }
}


