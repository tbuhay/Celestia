package com.example.celestia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.R
import com.example.celestia.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpIndexScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    val settingsVM: SettingsViewModel = viewModel()

    val use24h by settingsVM.timeFormat24H.observeAsState(true)

    val readings by vm.readings.observeAsState(emptyList())
    val grouped by vm.groupedKp.observeAsState(emptyList())   // ⭐ NEW
    val lastUpdatedRaw by vm.lastUpdated.observeAsState("Never")

    val cardShape = RoundedCornerShape(14.dp)

    val lastUpdated = if (lastUpdatedRaw == "Never") {
        "Never"
    } else {
        FormatUtils.convertTimeFormat(lastUpdatedRaw, use24h)
    }

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

            val latest = readings.firstOrNull()

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

                val kp = latest.estimatedKp
                val status = when {
                    kp >= 7 -> "Severe Storm"
                    kp >= 5 -> "Active Storm"
                    kp >= 3 -> "Active"
                    else -> "Quiet"
                }

                val color = when {
                    kp >= 7 -> Color(0xFFD32F2F)
                    kp >= 5 -> Color(0xFFF57C00)
                    kp >= 3 -> Color(0xFFFFEB3B)
                    else -> Color(0xFF4CAF50)
                }

                // -----------------------------
                // Current KP Card
                // -----------------------------
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFFFFF), cardShape),
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
                                text = "Current Kp Index:",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color(0xFF2B8AD2)
                                )
                            )

                            Text(
                                text = kp.toString(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    color = Color(0xFF2B8AD2)
                                )
                            )

                            Text(
                                text = "Status: $status",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = color
                                )
                            )

                            Text(
                                text = when {
                                    kp >= 7 -> "Major geomagnetic storm — auroras visible far south!"
                                    kp >= 5 -> "Aurora likely visible in northern skies."
                                    kp >= 3 -> "Minor aurora possible near polar regions."
                                    else -> "Calm conditions, no aurora expected."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                            )

                            Text(
                                text = "High: ${readings.maxOfOrNull { it.estimatedKp } ?: kp}  |  Low: ${readings.minOfOrNull { it.estimatedKp } ?: kp}",
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

                // -----------------------------
                // Kp scale
                // -----------------------------
                item {
                    Text(
                        text = "Kp Scale: 0–2 Quiet  |  3–4 Active  |  5–6 Storm  |  7–9 Severe Storm",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // -----------------------------
                // Explanation card
                // -----------------------------
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = cardShape,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "The Kp Index measures magnetic disturbances caused by solar activity.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // -----------------------------
                // Recent readings
                // -----------------------------
                item {
                    Text(
                        "Recent Readings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // ⭐ REPLACED: Uses precomputed values from ViewModel
                items(grouped.take(12).withIndex().toList()) { indexed ->

                    val (i, triple) = indexed

                    val hour = triple.hour
                    val avg = triple.avg
                    val high = triple.high
                    val low = triple.low

                    val colorItem = when {
                        avg >= 7 -> Color(0xFFD32F2F)
                        avg >= 5 -> Color(0xFFF57C00)
                        avg >= 3 -> Color(0xFFFFEB3B)
                        else -> Color(0xFF4CAF50)
                    }

                    val arrowIconRes = if (i == grouped.lastIndex) {
                        R.drawable.ic_no_change
                    } else {
                        val prevAvg = grouped[i + 1].avg
                        when {
                            avg > prevAvg -> R.drawable.ic_trend_up
                            avg < prevAvg -> R.drawable.ic_trend_down
                            else -> R.drawable.ic_no_change
                        }
                    }

                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x33FFFFFF), cardShape),
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
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = String.format("%.2f", avg),
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            color = colorItem
                                        )
                                    )
                                    Text(
                                        text = when {
                                            avg >= 7 -> "Severe Storm"
                                            avg >= 5 -> "Storm"
                                            avg >= 3 -> "Active"
                                            else -> "Quiet"
                                        },
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
                                    contentDescription = "Trend Arrow",
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
