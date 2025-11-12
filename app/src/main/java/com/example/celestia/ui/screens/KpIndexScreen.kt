package com.example.celestia.ui.screens

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpIndexScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    val readings by vm.readings.observeAsState(emptyList())
    val lastUpdatedRaw by vm.lastUpdated.observeAsState("Never")

    val cardShape = RoundedCornerShape(14.dp)

    val lastUpdated = lastUpdatedRaw

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    text = "Kp Index",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface)
                ) },
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
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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

                // ---------- Current KP Index Card ----------
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
                                "Current Kp Index: " + vm.formatKpValue(kp, decimals = 0),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = "Status: $status",
                                color = color,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = when {
                                    kp >= 7 -> "Major geomagnetic storm — auroras visible far south!"
                                    kp >= 5 -> "Aurora likely visible in northern skies."
                                    kp >= 3 -> "Minor aurora activity possible near polar regions."
                                    else -> "Calm conditions, no aurora expected."
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                )
                            )
                            Text(
                                "High: ${readings.maxOfOrNull { it.kpIndex } ?: kp}  |  Low: ${readings.minOfOrNull { it.kpIndex } ?: kp}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            )
                            Text(
                                "Last updated: $lastUpdated",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            )

                            if (lastUpdated == "Never") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0x80FF7043), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "*Warning*\nData may be outdated. Please refresh on the Dashboard.",
                                        color = Color(0xFFFF7043),
                                        textAlign = TextAlign.Center,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // ---------- Explanation ----------
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x22FFFFFF), cardShape),
                        shape = cardShape,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "The Kp Index measures disturbances in Earth's magnetic field caused by solar activity. Higher Kp values mean stronger geomagnetic storms and a greater chance of seeing auroras.",
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                // ---------- Recent Readings ----------
                item {
                    Text(
                        "Recent Readings",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                items(readings.take(10)) { reading ->
                    val colorItem = when {
                        reading.kpIndex >= 7 -> Color(0xFFD32F2F)
                        reading.kpIndex >= 5 -> Color(0xFFF57C00)
                        reading.kpIndex >= 3 -> Color(0xFFFFEB3B)
                        else -> Color(0xFF4CAF50)
                    }

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        shape = cardShape
                    ) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = vm.formatKpValue(reading.estimatedKp),
                                        color = colorItem,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = when {
                                            reading.kpIndex >= 7 -> "Severe Storm"
                                            reading.kpIndex >= 5 -> "Storm"
                                            reading.kpIndex >= 3 -> "Active"
                                            else -> "Quiet"
                                        },
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = " " + vm.formatKpTimestamp(reading.timestamp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
