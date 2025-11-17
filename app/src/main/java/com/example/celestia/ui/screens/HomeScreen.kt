package com.example.celestia.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.ui.theme.*
import com.example.celestia.utils.FormatUtils
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: CelestiaViewModel,
    settingsVM: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val readings by vm.readings.observeAsState(emptyList())
    val issReading by vm.issReading.observeAsState()
    val lunarPhase by vm.lunarPhase.observeAsState()
    val nextAsteroid by vm.nextAsteroid.observeAsState()
    val asteroidList = vm.asteroidList.observeAsState(emptyList()).value
    val featuredAsteroid = vm.getFeaturedAsteroid(asteroidList)
    val refreshOnLaunch by settingsVM.refreshOnLaunch.observeAsState(false)

    val scrollState = rememberScrollState()
    val cardShape = RoundedCornerShape(14.dp)

    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Explorer"

    // Greeting logic
    val greeting = remember {
        val cal = java.util.Calendar.getInstance()
        val hour12 = cal.get(java.util.Calendar.HOUR)
        val amPm = cal.get(java.util.Calendar.AM_PM)

        when {
            amPm == Calendar.AM && hour12 in 5..11 -> "Good morning"
            amPm == Calendar.PM && hour12 in 0..4  -> "Good afternoon"
            amPm == Calendar.PM && hour12 in 5..10 -> "Good evening"
            else -> "Good night"
        }
    }

    LaunchedEffect(refreshOnLaunch) {
        if (refreshOnLaunch) {
            vm.refresh()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Celestia",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reload Data",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
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
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Greeting Header
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Here’s what’s happening in the cosmos today.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Spacer(Modifier.height(24.dp))

            // ------------ Data Section ------------
            if (readings.isNotEmpty()) {
                val latest = readings.first()
                val kp = latest.estimatedKp
                val status = when {
                    kp >= 7 -> "Severe Storm"
                    kp >= 5 -> "Active Storm"
                    kp >= 3 -> "Active"
                    else -> "Quiet"
                }

                // ---------- KP INDEX CARD ----------
                CelestiaCard(
                    iconRes = R.drawable.ic_kp_beat,
                    iconTint = CelestiaSkyBlue,
                    title = "KP Index",
                    mainRow = {
                        Text(
                            text = kp.toString(),
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.displayMedium.copy(
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        )
                    },
                    description = "Current geomagnetic activity level",
                    shape = cardShape,
                    onClick = { navController.navigate("kp_index") }
                )

                // ---------- ISS CARD ----------
                CelestiaCard(
                    iconRes = R.drawable.ic_space_station,
                    iconTint = CelestiaPurple,
                    title = "ISS Location",
                    mainRow = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map_pin),
                            contentDescription = "Map Pin",
                            tint = CelestiaPurple,
                            modifier = Modifier
                                .size(18.dp)
                                .alignByBaseline()
                        )
                        Text(
                            text = issReading?.let {
                                FormatUtils.formatCoordinates(it.latitude, it.longitude)
                            } ?: "--° N, --° W",
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    description = issReading?.let {
                        "Altitude: ${FormatUtils.formatAltitude(it.altitude)} | Velocity: ${FormatUtils.formatVelocity(it.velocity)}"
                    } ?: "Altitude: -- km | Velocity: -- km/h",
                    shape = cardShape,
                    onClick = { navController.navigate("iss_location") }
                )

                // ---------- ASTEROID CARD ----------
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
                            text = featuredAsteroid?.name ?: "No data",
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    description = nextAsteroid?.let {
                        val date = it.approachDate
                        val distance = String.format("%.3f AU", it.missDistanceAu)
                        "Approach: $date | $distance"
                    } ?: "Tap Reload to fetch asteroid data.",
                    shape = cardShape,
                    onClick = { navController.navigate("asteroid_tracking") }
                )

                // ---------- LUNAR CARD ----------
                val illumination = lunarPhase?.let { vm.parseIlluminationPercent(it) }
                val moonAge = lunarPhase?.let { vm.computeMoonAgeDays(it) }

                CelestiaCard(
                    iconRes = R.drawable.ic_moon,
                    iconTint = CelestiaYellow,
                    title = "Lunar Phase",
                    mainRow = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_full_moon),
                            contentDescription = "Lunar Icon",
                            tint = CelestiaYellow,
                            modifier = Modifier
                                .size(18.dp)
                                .alignByBaseline()
                        )
                        Text(
                            text = vm.formatMoonPhaseName(lunarPhase?.moonPhase ?: "Loading..."),
                            modifier = Modifier.alignByBaseline(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    },
                    description =
                        if (lunarPhase != null)
                            "Illumination: ${FormatUtils.formatPercent(illumination ?: 0.0)} | Age: ${FormatUtils.formatMoonAge(moonAge ?: 0.0)}"
                        else
                            "Loading lunar data...",
                    shape = cardShape,
                    onClick = { navController.navigate("lunar_phase") }
                )
            } else {
                Text(
                    text = "No data loaded yet. Tap Reload to fetch current conditions.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                )
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
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Title Row
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

            // Middle Row (dynamic)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 8.dp),
                content = mainRow
            )

            // Description
            Text(
                text = description,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            )
        }
    }
}