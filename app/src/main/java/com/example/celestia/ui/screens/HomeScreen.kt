package com.example.celestia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.ui.theme.*
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: CelestiaViewModel
) {
    val readings by vm.readings.observeAsState(emptyList())
    val cardShape = RoundedCornerShape(14.dp)

    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Explorer"

    // --- Dynamic greeting based on time ---
    val greeting = remember {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..22 -> "Good evening"
            else -> "Hello"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Celestia",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // --- Greeting Header ---
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
            )

            Text(
                text = "Here’s what’s happening in the cosmos today.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Spacer(Modifier.height(24.dp))

            if (readings.isNotEmpty()) {
                val latest = readings.first()
                val kp = latest.estimatedKp
                val (status, _, _) = when {
                    kp >= 7 -> Triple("Severe Storm", MaterialTheme.colorScheme.error, "")
                    kp >= 5 -> Triple("Active Storm", MaterialTheme.colorScheme.tertiary, "")
                    kp >= 3 -> Triple("Active", MaterialTheme.colorScheme.secondary, "")
                    else -> Triple("Quiet", MaterialTheme.colorScheme.primary, "")
                }

                // ---------- KP Index ----------
                CelestiaCard(
                    iconRes = R.drawable.ic_kp_beat,
                    iconTint = CelestiaSkyBlue,
                    title = "KP Index",
                    mainRow = {
                        Text(
                            text = vm.formatKpValue(kp),
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    kp >= 7 -> Color(0xFFD32F2F)
                                    kp >= 5 -> Color(0xFFF57C00)
                                    kp >= 3 -> Color(0xFFFFEB3B)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                        )
                        Text(
                            text = status,
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Light,
                                fontSize = 15.sp
                            )
                        )
                    },
                    description = "Current geomagnetic activity level",
                    shape = cardShape,
                    onClick = { navController.navigate("kp_index") }
                )

                // ---------- ISS Location ----------
                val issReading by vm.issReading.observeAsState()

                CelestiaCard(
                    iconRes = R.drawable.ic_space_station,
                    iconTint = CelestiaPurple,
                    title = "ISS Location",
                    mainRow = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map_pin),
                            contentDescription = "Map Pin",
                            tint = CelestiaPurple,
                            modifier = Modifier.size(18.dp).alignByBaseline()
                        )
                        Text(
                            text = issReading?.let {
                                "${formatCoord(it.latitude, 'N', 'S')}, ${formatCoord(it.longitude, 'E', 'W')}"
                            } ?: "--° N, --° W",
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp
                            )
                        )
                    },
                    description = issReading?.let {
                        "Altitude: ${formatNumber(it.altitude)} km | Velocity: ${formatNumber(it.velocity)} km/h"
                    } ?: "Altitude: -- km | Velocity: -- km/h",
                    shape = cardShape,
                    onClick = { navController.navigate("iss_location") }
                )

                // ---------- Asteroid Tracking ----------
                CelestiaCard(
                    iconRes = R.drawable.ic_asteroid,
                    iconTint = CelestiaOrange,
                    title = "Asteroid Tracking",
                    mainRow = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar),
                            contentDescription = "Asteroid Calendar",
                            tint = CelestiaOrange,
                            modifier = Modifier
                                .size(18.dp)
                                .alignByBaseline()
                        )
                        Text(
                            text = "2024 MK",
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp
                            )
                        )
                    },
                    description = "Approach: Nov 21 | 0.015 AU",
                    shape = cardShape,
                    onClick = { navController.navigate("asteroid_tracking") }
                )
                // ---------- Lunar Phases ----------
                val lunarPhase by vm.lunarPhase.observeAsState()

                val illumination = lunarPhase?.let { vm.parseIlluminationPercent(it) }
                val moonAge = lunarPhase?.let { vm.computeMoonAgeDays(it) }

                CelestiaCard(
                    iconRes = R.drawable.ic_moon,
                    iconTint = CelestiaYellow,
                    title = "Lunar Phase",

                    mainRow = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_full_moon),
                            contentDescription = "Lunar Phase",
                            tint = CelestiaYellow,
                            modifier = Modifier
                                .size(18.dp)
                                .alignByBaseline()
                        )
                        Text(
                            text = vm.formatMoonPhaseName(lunarPhase?.moonPhase ?: "Loading..."),
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Light,
                                fontSize = 16.sp
                            )
                        )
                    },

                    description = if (lunarPhase != null)
                        "Illumination: ${String.format("%.1f", illumination)}% | Age: ${String.format("%.1f", moonAge)} days"
                    else
                        "Loading lunar data...",

                    shape = cardShape,
                    onClick = { navController.navigate("lunar_phase") }
                )
                // ---------- Placeholder ----------
//                CelestiaCard(
//                    iconRes = R.drawable.ic_moon,
//                    iconTint = CelestiaYellow,
//                    title = "Placeholder",
//                    mainRow = {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_moon),
//                            contentDescription = "",
//                            tint = CelestiaYellow,
//                            modifier = Modifier
//                                .size(18.dp)
//                                .alignByBaseline()
//                        )
//                        Text(
//                            text = "",
//                            modifier = Modifier.alignByBaseline(),
//                            style = MaterialTheme.typography.titleMedium.copy(
//                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
//                                fontWeight = FontWeight.Light,
//                                fontSize = 16.sp
//                            )
//                        )
//                    },
//                    description = "",
//                    shape = cardShape,
//                    onClick = { navController.navigate("asteroid_tracking") }
//                )

            } else {
                Text(
                    text = "No data loaded yet. Tap Reload to fetch current conditions.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { vm.refresh() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Reload")
            }
        }
    }
}

@Composable
fun CelestiaCard(
    iconRes: Int,
    iconTint: Color,
    title: String,
    mainRow: @Composable RowScope.() -> Unit,
    description: String,
    shape: RoundedCornerShape = RoundedCornerShape(14.dp),
    onClick: (() -> Unit)? = null
) {
    ElevatedCard(
        onClick = { onClick?.invoke() },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .border(
                width = 1.dp,
                color = Color(0x33FFFFFF),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // --- Title Row ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = "$title Icon",
                    tint = iconTint,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            // --- Custom middle row ---
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 8.dp),
                content = mainRow
            )

            // --- Description ---
            Text(
                text = description,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            )
        }
    }
}
