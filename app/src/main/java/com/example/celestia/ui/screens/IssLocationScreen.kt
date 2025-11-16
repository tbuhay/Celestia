package com.example.celestia.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.FormatUtils
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import androidx.compose.material.icons.filled.Refresh

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssLocationScreen(
    navController: NavController,
    vm: CelestiaViewModel
) {
    val issReading by vm.issReading.observeAsState()
    val astronautCount by vm.astronautCount.observeAsState(0)

    val settingsVM: SettingsViewModel = viewModel()
    val use24h = settingsVM.timeFormat24H.observeAsState(true).value

    val cardShape = RoundedCornerShape(14.dp)
    val scrollState = rememberScrollState()

    // Load crew list once
    LaunchedEffect(Unit) {
        vm.fetchAstronauts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ISS Location",
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
                actions = {
                    IconButton(
                        onClick = {
                            vm.refresh()
                            vm.fetchAstronauts()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh ISS Data",
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
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --------------------------------------------------------------------
            // MAIN ISS DATA CARD
            // --------------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33FFFFFF), cardShape),
                shape = cardShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // TITLE ROW
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
                                Icons.Default.Public,
                                contentDescription = "ISS",
                                tint = Color(0xFFB39DDB)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text("International Space Station",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Live Position",
                                style = MaterialTheme.typography.labelSmall
                                    .copy(color = Color.LightGray)
                            )
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 6.dp))

                    // STATS
                    if (issReading != null) {
                        StatRow(
                            icon = Icons.Default.LocationOn,
                            label = "Coordinates",
                            value = FormatUtils.formatCoordinates(
                                issReading!!.latitude,
                                issReading!!.longitude
                            )
                        )
                        StatRow(
                            icon = Icons.Default.Public,
                            label = "Altitude",
                            value = FormatUtils.formatAltitude(issReading!!.altitude)
                        )
                        StatRow(
                            icon = Icons.Default.Speed,
                            label = "Velocity",
                            value = FormatUtils.formatVelocity(issReading!!.velocity)
                        )
                        StatRow(
                            icon = Icons.Default.People,
                            label = "Crew",
                            value = "$astronautCount aboard"
                        )

                        val formattedTime = FormatUtils.convertTimeFormat(
                            issReading!!.timestamp,
                            use24h
                        )

                        Text(
                            "Updated: $formattedTime")
                    } else {
                        Text(
                            "No ISS data available yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            }

            // --------------------------------------------------------------------
            // MAP CARD
            // --------------------------------------------------------------------
            val issPos = issReading?.let { LatLng(it.latitude, it.longitude) }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                )
            ) {
                if (issPos != null) {
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(issPos, 4.5f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState
                    ) {
                        Marker(
                            state = MarkerState(issPos),
                            title = "ISS",
                            snippet = FormatUtils.formatCoordinates(
                                issReading!!.latitude,
                                issReading!!.longitude
                            )
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading ISS location...", color = Color.Gray)
                    }
                }
            }

            // --------------------------------------------------------------------
            // INFO CARD
            // --------------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        "About the ISS",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Text(
                        "The International Space Station is a modular space station in low Earth orbit. " +
                                "It serves as a microgravity research laboratory for many scientific fields.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        ),
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {

        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFFB39DDB)
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
