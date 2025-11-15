package com.example.celestia.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.utils.TimeUtils
import com.example.celestia.utils.FormatUtils
import com.example.celestia.R
import com.example.celestia.ui.theme.CelestiaYellow
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.util.Locale

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
                color = Color(0xFF3A3F5F),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1A1E33)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

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

    LaunchedEffect(Unit) {
        vm.loadLunarPhase()
    }

    val phaseName = vm.formatMoonPhaseName(lunarPhase?.moonPhase)
    val illumination = vm.parseIlluminationPercent(lunarPhase)
    val waxing = vm.isWaxing(lunarPhase?.moonPhase)
    val ageDays = vm.computeMoonAgeDays(lunarPhase)
    val distanceKm = lunarPhase?.moonDistance

    val updatedText by vm.lunarUpdated.observeAsState("Unknown")

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

            // Loading / Error states
            when {
                isLoading -> Text(
                    "Loading current lunar data...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )

                errorMessage != null -> Text(
                    errorMessage ?: "Error loading lunar data",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.error
                    )
                )
            }

            // -----------------------
            // MAIN PHASE CARD
            // -----------------------
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        "Current Lunar Phase",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                    )
                }
            }

            // -----------------------
            // CURRENT DETAILS CARD
            // -----------------------
            CelestiaPhaseCard {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_cresent_moon),
                        contentDescription = null,
                        tint = CelestiaYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Current Details",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Illumination",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                        Text(
                            FormatUtils.formatPercent(illumination),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Moon Age",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                        Text(
                            FormatUtils.formatMoonAge(ageDays),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
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
                        Text(
                            "Distance",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                        Text(
                            distanceKm?.let { FormatUtils.formatDistance(it) } ?: "N/A",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Updated",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                        )
                        Text(
                            updatedText,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // -----------------------
            // TODAY'S SCHEDULE CARD
            // -----------------------
            CelestiaPhaseCard {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Text(
                        "Today’s Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(12.dp))

                val moonriseText = lunarPhase?.moonrise?.let {
                    if (it == "-:-") "No moonrise today" else it
                } ?: "N/A"

                val moonsetText = lunarPhase?.moonset ?: "N/A"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Moonrise card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E335A))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Moonrise", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        Text(moonriseText, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }

                    // Moonset card
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E335A))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Moonset", color = Color.Gray, style = MaterialTheme.typography.labelMedium)
                        Text(moonsetText, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // -----------------------------------------------------------------------------
            // NEXT FULL / NEW MOON CARD
            // -----------------------------------------------------------------------------
            CelestiaPhaseCard {

                Text(
                    text = "Upcoming Phases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                val daysToFull = vm.daysUntilNextFullMoon(ageDays)
                val daysToNew = vm.daysUntilNextNewMoon(ageDays)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ---- Full Moon Card ----
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E335A))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Next Full Moon",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f days", daysToFull),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    // ---- New Moon Card ----
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2E335A))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Next New Moon",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = String.format(Locale.US, "%.1f days", daysToNew),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}
