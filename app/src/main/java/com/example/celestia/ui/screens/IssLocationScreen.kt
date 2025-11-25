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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.FormatUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

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
    val use24h = settingsVM.timeFormat24h.observeAsState(true).value

    val scrollState = rememberScrollState()
    val cardShape = RoundedCornerShape(14.dp)

    // Load astronaut count when screen enters
    LaunchedEffect(Unit) { vm.fetchAstronauts() }

    // Auto-refresh ISS
    LaunchedEffect(Unit) {
        while (true) {
            vm.refresh()
            vm.fetchAstronauts()
            delay(2_000)
        }
    }

    fun lerp(a: LatLng, b: LatLng, t: Float): LatLng {
        val lat = a.latitude + (b.latitude - a.latitude) * t
        val lng = a.longitude + (b.longitude - a.longitude) * t
        return LatLng(lat, lng)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ISS Location") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ---------------------------------------------------------------------
            // MAIN ISS DATA CARD
            // ---------------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33FFFFFF), cardShape),
                shape = cardShape,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    // HEADER
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
                                contentDescription = null,
                                tint = Color(0xFFB39DDB)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text("International Space Station", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Live Position",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                            )
                        }
                    }

                    HorizontalDivider()

                    // DATA
                    issReading?.let { reading ->

                        StatRow(
                            icon = Icons.Default.LocationOn,
                            label = "Coordinates",
                            value = FormatUtils.formatCoordinates(reading.latitude, reading.longitude)
                        )

                        StatRow(
                            icon = Icons.Default.Public,
                            label = "Altitude",
                            value = FormatUtils.formatAltitude(reading.altitude)
                        )

                        StatRow(
                            icon = Icons.Default.Speed,
                            label = "Velocity",
                            value = FormatUtils.formatVelocity(reading.velocity)
                        )

                        StatRow(
                            icon = Icons.Default.People,
                            label = "Crew",
                            value = "$astronautCount aboard"
                        )

                        val formattedTime = FormatUtils.convertTimeFormat(reading.timestamp, use24h)

                        Text(
                            "Updated: $formattedTime",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    } ?: Text(
                        "No ISS data available yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            // ---------------------------------------------------------------------
            // MAP CARD
            // ---------------------------------------------------------------------
            val issPos = issReading?.let { LatLng(it.latitude, it.longitude) }

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = cardShape
            ) {
                if (issPos != null) {

                    // Track map gestures
                    var isUserInteracting by remember { mutableStateOf(false) }

                    // Keep previous & target positions for smooth interpolation
                    var previousPos by remember { mutableStateOf(issPos) }
                    var targetPos by remember { mutableStateOf(issPos) }
                    var progress by remember { mutableFloatStateOf(1f) }

                    // Map camera state
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(issPos, 4.5f)
                    }

                    // Track map loaded
                    var mapLoaded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxSize()) {

                        GoogleMap(
                            modifier = Modifier.matchParentSize(),
                            cameraPositionState = cameraState,
                            onMapLoaded = { mapLoaded = true }
                        )

                        // STATIC ICON (center marker)
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "ISS Marker",
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )

                        // Detect when user is moving the map (pan/zoom)
                        LaunchedEffect(cameraState.isMoving) {
                            isUserInteracting = cameraState.isMoving
                        }
                    }

                    // Update interpolation targets when ISS moves
                    LaunchedEffect(issPos) {
                        if (mapLoaded) {
                            previousPos = targetPos
                            targetPos = issPos
                            progress = 0f
                        }
                    }

                    // Smoothly animate ISS movement at 60FPS
                    LaunchedEffect(previousPos, targetPos, mapLoaded) {
                        if (!mapLoaded) return@LaunchedEffect

                        val duration = 1200L  // smooth but shorter than refresh
                        val startTime = System.currentTimeMillis()

                        while (progress < 1f) {

                            // Break out if user is interacting (restore zoom!)
                            if (isUserInteracting) break

                            val elapsed = System.currentTimeMillis() - startTime
                            progress = (elapsed / duration.toFloat()).coerceIn(0f, 1f)

                            val interpolated = LatLng(
                                previousPos.latitude + (targetPos.latitude - previousPos.latitude) * progress,
                                previousPos.longitude + (targetPos.longitude - previousPos.longitude) * progress
                            )

                            cameraState.position = CameraPosition.fromLatLngZoom(interpolated, cameraState.position.zoom)
                            cameraState.move(CameraUpdateFactory.newLatLng(interpolated))

                            delay(16) // ~60FPS
                        }
                    }

                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Loading ISS location...", color = Color.Gray)
                    }
                }
            }
            // ---------------------------------------------------------------------
            // INFO CARD
            // ---------------------------------------------------------------------
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0x33FFFFFF), cardShape),
                shape = cardShape
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "About the ISS",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )

                    Text(
                        "The International Space Station is a modular space station in low Earth orbit. " +
                                "It serves as a microgravity research laboratory for many scientific fields.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                        )
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
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
