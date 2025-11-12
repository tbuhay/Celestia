package com.example.celestia.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssLocationScreen(
    navController: NavController,
    vm: CelestiaViewModel
) {
    val issReading by vm.issReading.observeAsState()
    val cardShape = RoundedCornerShape(14.dp)

    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ISS Location") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(16.dp)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // === Main ISS Data Card ===
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33FFFFFF), cardShape),
                shape = cardShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha=0.2f)
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "ISS",
                                tint = Color(0xFFB39DDB)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "International Space Station",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Live Position",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    if (issReading != null) {
                        StatRow(
                            icon = Icons.Default.LocationOn,
                            label = "Coordinates",
                            value = "${formatCoord(issReading!!.latitude, 'N', 'S')}, " +
                                    "${formatCoord(issReading!!.longitude, 'E', 'W')}"
                        )
                        StatRow(
                            icon = Icons.Default.Public,
                            label = "Altitude",
                            value = "${formatNumber(issReading!!.altitude)} km"
                        )
                        StatRow(
                            icon = Icons.Default.Speed,
                            label = "Velocity",
                            value = "${formatNumber(issReading!!.velocity)} km/h"
                        )
                        Text(
                            "Updated: ${issReading!!.timestamp}",
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    } else {
                        Text(
                            text = "No ISS data available yet.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // === Map View ===
            val issPosition = issReading?.let { LatLng(it.latitude, it.longitude) }

            if (issPosition != null) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(issPosition, 4.5f)
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Box(Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState
                        ) {
                            Marker(
                                state = MarkerState(position = issPosition),
                                title = "ISS",
                                snippet = "Lat: ${"%.2f".format(issReading!!.latitude)}, " +
                                        "Lon: ${"%.2f".format(issReading!!.longitude)}"
                            )
                        }
                    }
                }
            } else {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading ISS location...", color = Color.Gray)
                    }
                }
            }

            // === Info Card ===
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(14.dp)
                    ),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                )  {
                    Text(
                        "About the ISS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = "The International Space Station is a modular space station in low Earth orbit. " +
                                "It serves as a microgravity and space environment research laboratory where scientific " +
                                "research is conducted in astrobiology, astronomy, meteorology, physics, and other fields.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                    )

                    Spacer(Modifier.width(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            tonalElevation = 3.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Mass", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                Text("420,000 kg", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .border(
                                    width = 1.dp,
                                    color = Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            tonalElevation = 3.dp,
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Launch Date", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                Text("Nov 20, 1998", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFFB39DDB) // lighter purple
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

fun formatCoord(deg: Double, pos: Char, neg: Char): String {
    val hemi = if (deg >= 0) pos else neg
    val absDeg = abs(deg)
    return "${"%.4f".format(absDeg)}° $hemi"
}

fun formatNumber(n: Double): String {
    val rounded = if ((n * 10).roundToInt() % 10 == 0) n.toInt().toString() else "%.1f".format(n)
    val parts = rounded.split(".")
    val intPart = parts[0]
    val withCommas = intPart.reversed().chunked(3).joinToString(",").reversed()
    return if (parts.size == 2) "$withCommas.${parts[1]}" else withCommas
}
