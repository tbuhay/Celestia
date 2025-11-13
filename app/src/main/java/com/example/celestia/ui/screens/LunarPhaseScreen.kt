package com.example.celestia.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.ui.theme.CelestiaYellow
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.util.Locale


// -----------------------------------------------------------------------------
//  Celestia Phase Card (deep navy + 1dp border)
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
                color = Color(0xFF3A3F5F),   // subtle gray-blue border
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color(0xFF1A1E33) // deep navy blue
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}


// -----------------------------------------------------------------------------
//  Dynamic Moon Phase (temporary until static images added)
// -----------------------------------------------------------------------------
@Composable
fun DynamicMoonPhase(
    illuminationPercent: Double,
    isWaxing: Boolean,
    modifier: Modifier = Modifier
) {
    val illumination = (illuminationPercent / 100.0)
        .coerceIn(0.0, 1.0)
        .toFloat()

    Box(
        modifier = modifier
            .size(180.dp)
            .clip(CircleShape)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_full_moon_static),
            contentDescription = null,
            modifier = Modifier.matchParentSize()
        )

        Canvas(modifier = Modifier.matchParentSize()) {

            val r = size.width / 2f
            val center = Offset(r, r)
            val direction = if (isWaxing) 1 else -1

            // Quarter phases → straight shadow
            if (illumination in 0.45f..0.55f) {
                val half = size.width / 2f
                val quarterShadowX = if (isWaxing) 0f else half

                drawRect(
                    color = Color.Black,
                    topLeft = Offset(quarterShadowX, 0f),
                    size = Size(half, size.height)
                )
            }
            // All other phases → circular shadow
            else {
                val shadowOffset = (1f - illumination) * r
                drawCircle(
                    color = Color.Black,
                    radius = r,
                    center = Offset(
                        x = center.x + shadowOffset * direction,
                        y = center.y
                    ),
                    blendMode = BlendMode.SrcOver
                )
            }
        }
    }
}


// -----------------------------------------------------------------------------
//  Lunar Phase Screen
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

    // -----------------------
    // Derived Values
    // -----------------------
    val phaseName = vm.formatMoonPhaseName(lunarPhase?.moonPhase)
    val illumination = vm.parseIlluminationPercent(lunarPhase)
    val waxing = vm.isWaxing(lunarPhase?.moonPhase)
    val ageDays = vm.computeMoonAgeDays(lunarPhase)
    val distanceKm = lunarPhase?.moonDistance
    val updatedText = lunarPhase?.let {
        vm.formatLunarTimestamp(it.date, it.currentTime)
    } ?: "Just now"


    // -----------------------
    // UI
    // -----------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lunar Phase") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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


            // -----------------------
            // Loading / Error
            // -----------------------
            when {
                isLoading -> Text(
                    "Loading current lunar data...",
                    color = Color.Gray
                )

                errorMessage != null -> Text(
                    errorMessage ?: "Error loading lunar data",
                    color = MaterialTheme.colorScheme.error
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

                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF444B6E)),
                        contentAlignment = Alignment.Center
                    ) { Text("🌕") }


                    if (lunarPhase != null) {
                        DynamicMoonPhase(
                            illuminationPercent = illumination,
                            isWaxing = waxing,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    Text(
                        text = phaseName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Current Lunar Phase",
                        color = Color.Gray
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
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // --- Row 1 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Illumination", color = Color.Gray)
                        Text(
                            String.format(Locale.US, "%.1f%%", illumination),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Moon Age", color = Color.Gray)
                        Text(
                            String.format(Locale.US, "%.1f days", ageDays),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // --- Row 2 ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Distance", color = Color.Gray)
                        Text(
                            text = distanceKm?.let { String.format("%,.0f km", it) } ?: "N/A",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Updated", color = Color.Gray)
                        Text(
                            updatedText,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E335A))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Moonrise: $moonriseText")
                    Text("Moonset: $moonsetText")
                }
            }
        }
    }
}
