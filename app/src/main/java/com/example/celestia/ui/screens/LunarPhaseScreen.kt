package com.example.celestia.ui.screens

import android.os.Build
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
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.LunarHelper
import java.util.Locale


// -----------------------------------------------------------------------------
// Phase Card
// -----------------------------------------------------------------------------

/**
 * A reusable Celestia-styled card layout used throughout the Lunar Phase screen.
 *
 * This card:
 * - Provides a rounded elevated surface
 * - Includes a subtle outline border
 * - Applies consistent padding and styling
 *
 * @param modifier Optional modifier for sizing and layout control.
 * @param content ColumnScope content inside the card.
 */
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

/**
 * Screen displaying detailed lunar phase information including:
 *
 * - Current moon phase name and icon
 * - Illumination percentage
 * - Moon age (days since new moon)
 * - Distance from Earth
 * - Moonrise & moonset times (12h/24h based on user settings)
 * - Timestamp of last update
 * - Days until next full moon & new moon
 *
 * Data is sourced from:
 * [CelestiaViewModel.lunarPhase], [CelestiaViewModel.isLunarLoading],
 * [CelestiaViewModel.lunarUpdated], and lunar helper utilities.
 *
 * This screen also uses [CelestiaPhaseCard] to maintain a unified look.
 *
 * @param navController Navigation controller for screen transitions.
 * @param vm Main Celestia ViewModel providing lunar data and helpers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunarPhaseScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    // Live lunar data states
    val lunarPhase by vm.lunarPhase.observeAsState()
    val isLoading by vm.isLunarLoading.observeAsState(false)
    val errorMessage by vm.lunarError.observeAsState()
    val updatedText by vm.lunarUpdated.observeAsState("Unknown")

    // User time format settings
    val settingsVM: SettingsViewModel = viewModel()
    val use24h = settingsVM.timeFormat24h.observeAsState(true).value

    // Extract lunar properties
    val phaseName = LunarHelper.formatMoonPhaseName(lunarPhase?.moonPhase)
    val illumination = LunarHelper.parseIlluminationPercent(lunarPhase)
    val ageDays = vm.getMoonAge()
    val distanceKm = lunarPhase?.moonDistanceKm

    // Rise/Set formatting
    val moonriseRaw = lunarPhase?.moonRise ?: "N/A"
    val moonsetRaw = lunarPhase?.moonSet ?: "N/A"
    val formattedMoonrise = FormatUtils.convertLunarTime(moonriseRaw, use24h)
    val formattedMoonset = FormatUtils.convertLunarTime(moonsetRaw, use24h)

    // Phase icon
    val moonIconRes = vm.getMoonPhaseIconRes(lunarPhase?.moonPhase)

    /**
     * Refresh lunar data on initial screen load.
     */
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vm.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Moon Phase",
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

            // ---------------------------------------------------------
            // LOADING / ERROR STATE
            // ---------------------------------------------------------
            when {
                isLoading -> Text(
                    "Loading current moon data...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                errorMessage != null -> Text(
                    errorMessage ?: "Error loading moon data",
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ---------------------------------------------------------
            // MAIN PHASE CARD
            // ---------------------------------------------------------
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
                        "Current Moon Phase",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ---------------------------------------------------------
            // CURRENT DETAILS CARD
            // ---------------------------------------------------------
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

                // Row 1 — Illumination & Age
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

                // Row 2 — Distance & Updated Time
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

                        val formattedUpdated = FormatUtils.formatUpdatedTimestamp(updatedText, use24h)

                        Text(
                            formattedUpdated,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // TODAY’S SCHEDULE (Moonrise / Moonset)
            // ---------------------------------------------------------
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
                        Text(formattedMoonrise, color = MaterialTheme.colorScheme.onSurface)
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
                        Text(formattedMoonset, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // ---------------------------------------------------------
            // UPCOMING PHASES (Full & New Moon countdowns)
            // ---------------------------------------------------------
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
                    // Full Moon countdown
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

                    // New Moon countdown
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