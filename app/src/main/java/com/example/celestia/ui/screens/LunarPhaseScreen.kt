package com.example.celestia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.utils.FormatUtils
import com.example.celestia.R
import com.example.celestia.ui.theme.CelestiaYellow
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.util.Locale

// -----------------------------------------------------------------------------
// Phase Card
// -----------------------------------------------------------------------------
@Composable
fun CelestiaPhaseCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

// -----------------------------------------------------------------------------
// Lunar Phase Screen
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunarPhaseScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    val lunarPhase by vm.lunarPhase.observeAsState()
    val isLoading by vm.isLunarLoading.observeAsState(false)
    val errorMessage by vm.lunarError.observeAsState()
    val updatedText by vm.lunarUpdated.observeAsState("Unknown")

    // Load on launch
    LaunchedEffect(Unit) {
        vm.loadLunarPhase()
    }

    val phaseName = vm.formatMoonPhaseName(lunarPhase?.moonPhase)
    val illumination = vm.parseIlluminationPercent(lunarPhase)
    val ageDays = vm.computeMoonAgeDays(lunarPhase)
    val distanceKm = lunarPhase?.moonDistanceKm
    val moonriseText = lunarPhase?.moonRise ?: "N/A"
    val moonsetText = lunarPhase?.moonSet ?: "N/A"

    val moonIconRes = vm.getMoonPhaseIconRes(lunarPhase?.moonPhase)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Lunar Phase",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Loading / Error
            when {
                isLoading -> Text(
                    "Loading current lunar data...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                errorMessage != null -> Text(
                    errorMessage ?: "Error loading lunar data",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // MAIN PHASE CARD
            CelestiaPhaseCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Image(
                        painter = painterResource(id = moonIconRes),
                        contentDescription = "Moon phase icon",
                        modifier = Modifier.size(170.dp)
                    )

                    Text(
                        text = phaseName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        "Current Lunar Phase",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // CURRENT DETAILS CARD
            CelestiaPhaseCard {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cresent_moon),
                        contentDescription = null,
                        tint = CelestiaYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Current Details",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Illumination", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            FormatUtils.formatPercent(illumination),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Moon Age", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            FormatUtils.formatMoonAge(ageDays),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Distance", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            distanceKm?.let { FormatUtils.formatDistance(it) } ?: "N/A",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Updated", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            updatedText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // TODAY'S SCHEDULE
            CelestiaPhaseCard {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Today’s Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Moonrise
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Text("Moonrise", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(moonriseText, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Moonset
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Text("Moonset", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(moonsetText, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // UPCOMING PHASES
            CelestiaPhaseCard {

                Text(
                    "Upcoming Phases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(16.dp))

                val daysToFull = vm.daysUntilNextFullMoon(ageDays)
                val daysToNew = vm.daysUntilNextNewMoon(ageDays)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Full Moon
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Text("Next Full Moon", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            String.format(Locale.US, "%.1f days", daysToFull),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // New Moon
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Text("Next New Moon", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            String.format(Locale.US, "%.1f days", daysToNew),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
