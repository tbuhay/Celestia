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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.ui.theme.*
import com.example.celestia.utils.FormatUtils
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.LunarHelper
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

// -----------------------------------------------------------------------------
// HOME SCREEN
// -----------------------------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: CelestiaViewModel,
    settingsVM: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // -------------------------------------------------------------------------
    // State Observers
    // -------------------------------------------------------------------------
    val readings by vm.readings.observeAsState(emptyList())
    val issReading by vm.issReading.observeAsState()
    val lunarPhase by vm.lunarPhase.observeAsState()
    val nextAsteroid by vm.nextAsteroid.observeAsState()
    val asteroidList by vm.asteroidList.observeAsState(emptyList())
    val refreshOnLaunch by settingsVM.refreshOnLaunchEnabled.observeAsState(false)

    val featuredAsteroid = vm.getFeaturedAsteroid(asteroidList)
    val scrollState = rememberScrollState()
    val cardShape = RoundedCornerShape(14.dp)

    // User
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Explorer"

    // -------------------------------------------------------------------------
    // Greeting Logic
    // -------------------------------------------------------------------------
    val greeting = remember { getGreetingMessage() }

    // -------------------------------------------------------------------------
    // Refresh on Launch
    // -------------------------------------------------------------------------
    LaunchedEffect(refreshOnLaunch) {
        if (refreshOnLaunch) vm.refresh()
    }

    // -------------------------------------------------------------------------
    // Scaffold
    // -------------------------------------------------------------------------
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

        // ---------------------------------------------------------------------
        // Screen Content
        // ---------------------------------------------------------------------
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

            // -----------------------------------------------------------------
            // DATA CARDS
            // -----------------------------------------------------------------
            if (readings.isNotEmpty()) {

                KpCard(
                    readings = readings,
                    vm = vm,
                    cardShape = cardShape,
                    navController = navController
                )

                IssCard(
                    issReading = issReading,
                    cardShape = cardShape,
                    navController = navController
                )

                AsteroidCard(
                    featuredAsteroid = featuredAsteroid,
                    nextAsteroid = nextAsteroid,
                    cardShape = cardShape,
                    navController = navController
                )

                LunarCard(
                    lunarPhase = lunarPhase,
                    cardShape = cardShape,
                    navController = navController
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

// -----------------------------------------------------------------------------
// HELPERS
// -----------------------------------------------------------------------------
private fun getGreetingMessage(): String {
    val cal = Calendar.getInstance()
    val hour12 = cal.get(Calendar.HOUR)
    val amPm = cal.get(Calendar.AM_PM)

    return when {
        amPm == Calendar.AM && hour12 in 5..11 -> "Good morning"
        amPm == Calendar.PM && hour12 in 0..4  -> "Good afternoon"
        amPm == Calendar.PM && hour12 in 5..10 -> "Good evening"
        else -> "Good night"
    }
}

// -----------------------------------------------------------------------------
// DASHBOARD CARD COMPOSABLES
// -----------------------------------------------------------------------------

@Composable
private fun KpCard(
    readings: List<com.example.celestia.data.model.KpReading>,
    vm: CelestiaViewModel,
    cardShape: RoundedCornerShape,
    navController: NavController
) {
    val latest = vm.getLatestValidKp(readings)
    val kp = latest?.estimatedKp ?: 0.0

    val (status, color) = FormatUtils.getNoaaKpStatusAndColor(kp)

    CelestiaCard(
        iconRes = R.drawable.ic_kp_beat,
        iconTint = CelestiaSkyBlue,
        title = "KP Index",
        mainRow = {
            Text(
                text = kp.toString(),
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.displayMedium.copy(
                    color = color
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
}

@Composable
private fun IssCard(
    issReading: com.example.celestia.data.model.IssReading?,
    cardShape: RoundedCornerShape,
    navController: NavController
) {
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
}

@Composable
private fun AsteroidCard(
    featuredAsteroid: com.example.celestia.data.model.AsteroidApproach?,
    nextAsteroid: com.example.celestia.data.model.AsteroidApproach?,
    cardShape: RoundedCornerShape,
    navController: NavController
) {
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
        description =
            nextAsteroid?.let {
                val date = it.approachDate
                val distance = String.format("%.3f AU", it.missDistanceAu)
                "Approach: $date | $distance"
            } ?: "Tap Reload to fetch asteroid data.",
        shape = cardShape,
        onClick = { navController.navigate("asteroid_tracking") }
    )
}

@Composable
private fun LunarCard(
    lunarPhase: com.example.celestia.data.model.LunarPhaseEntity?,
    cardShape: RoundedCornerShape,
    navController: NavController
) {
    val illumination = lunarPhase?.let { LunarHelper.parseIlluminationPercent(it) }
    val moonAge = LunarHelper.getMoonAge()

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
                text = LunarHelper.formatMoonPhaseName(lunarPhase?.moonPhase ?: "Loading..."),
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
}

// -----------------------------------------------------------------------------
// ORIGINAL CelestiaCard
// -----------------------------------------------------------------------------
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 8.dp),
                content = mainRow
            )

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
