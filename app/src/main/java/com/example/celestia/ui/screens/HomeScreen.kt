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

/**
 * Home screen of the Celestia application.
 *
 * Displays a personalized greeting and a dashboard of space-weather data such as:
 * - Kp Index (geomagnetic activity)
 * - ISS location
 * - Asteroid approaches
 * - Current lunar phase
 *
 * The screen reacts to LiveData streams from [CelestiaViewModel] and respects
 * user preferences from [SettingsViewModel] (e.g., auto-refresh on app launch).
 *
 * @param navController Navigation controller used to move between screens.
 * @param vm Main Celestia ViewModel containing NOAA, ISS, Lunar, and Asteroid data.
 * @param settingsVM ViewModel managing user preferences such as dark mode and auto-refresh.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: CelestiaViewModel,
    settingsVM: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // LiveData observers for space-weather data + user preferences
    val readings by vm.readings.observeAsState(emptyList())
    val issReading by vm.issReading.observeAsState()
    val lunarPhase by vm.lunarPhase.observeAsState()
    val nextAsteroid by vm.nextAsteroid.observeAsState()
    val asteroidList by vm.asteroidList.observeAsState(emptyList())
    val refreshOnLaunch by settingsVM.refreshOnLaunchEnabled.observeAsState(false)

    val featuredAsteroid = vm.getFeaturedAsteroid(asteroidList)
    val scrollState = rememberScrollState()
    val cardShape = RoundedCornerShape(14.dp)

    // User identity
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Explorer"

    // Greeting based on time of day
    val greeting = remember { getGreetingMessage() }

    /**
     * Automatically refreshes all data when the user has enabled
     * "Refresh on App Launch" in settings.
     */
    LaunchedEffect(refreshOnLaunch) {
        if (refreshOnLaunch) vm.refresh()
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
                    // Manual refresh button
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reload Data",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Navigate to settings
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

            // Greeting header
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = "Your space weather briefing is ready.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Spacer(Modifier.height(24.dp))

            // Render dashboard cards
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

                ViewingCard(
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

/**
 * Returns a greeting string based on the user's local time.
 *
 * @return A greeting such as `"Good morning"`, `"Good afternoon"`, or `"Good evening"`.
 */
private fun getGreetingMessage(): String {
    val cal = Calendar.getInstance()
    val hour12 = cal.get(Calendar.HOUR)
    val amPm = cal.get(Calendar.AM_PM)

    return when {
        amPm == Calendar.AM && hour12 in 5..11 -> "Good morning"
        amPm == Calendar.PM && hour12 in 0..4  -> "Good afternoon"
        amPm == Calendar.PM && hour12 in 5..10 -> "Good evening"
        else -> "Hello"
    }
}

/**
 * Card showing the current NOAA Kp Index (geomagnetic activity).
 *
 * @param readings List of recent Kp readings.
 * @param vm The main Celestia ViewModel used to compute the latest valid Kp.
 * @param cardShape Rounded corner shape for UI consistency.
 * @param navController Navigation controller for card tap events.
 */
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        description = "Current geomagnetic activity level",
        shape = cardShape,
        onClick = { navController.navigate("kp_index") }
    )
}

/**
 * Card showing the current ISS latitude/longitude and altitude/velocity metadata.
 *
 * @param issReading Current ISS data from ViewModel or `null` if loading.
 * @param cardShape Shape of the card container.
 * @param navController Navigation controller for tapping into ISS details.
 */
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

/**
 * Card summarizing asteroid approach data including:
 * - Featured asteroid name
 * - Next predicted close approach
 *
 * @param featuredAsteroid Largest or most significant asteroid today.
 * @param nextAsteroid The soonest upcoming close-approach asteroid.
 * @param cardShape Shape of the dashboard card.
 * @param navController Navigation to the asteroid tracking screen.
 */
@Composable
private fun AsteroidCard(
    featuredAsteroid: com.example.celestia.data.model.AsteroidApproach?,
    nextAsteroid: com.example.celestia.data.model.AsteroidApproach?,
    cardShape: RoundedCornerShape,
    navController: NavController
) {
    val primaryAsteroid = featuredAsteroid ?: nextAsteroid

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
                text = primaryAsteroid?.name ?: "No data",
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        description =
            primaryAsteroid?.let { asteroid ->
                val date = asteroid.approachDate
                val distance = String.format("%.3f AU", asteroid.missDistanceAu)
                val avgDiameter =
                    ((asteroid.diameterMinMeters + asteroid.diameterMaxMeters) / 2).toInt()

                "Approach: $date | $distance | Size: ~${avgDiameter} m"
            } ?: "Tap Reload to fetch asteroid data.",
        shape = cardShape,
        onClick = { navController.navigate("asteroid_tracking") }
    )
}

/**
 * Card displaying the current moon phase, illumination percent,
 * and estimated moon age.
 *
 * @param lunarPhase Entity containing today’s lunar metadata.
 * @param cardShape Rounded card corners for UI consistency.
 * @param navController Navigation to the lunar phase details screen.
 */
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
        title = "Moon Phase",
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

/**
 * Placeholder card used for future features such as:
 * - Aurora visibility
 * - Local stargazing recommendations
 * - Watchlist notifications
 */
@Composable
private fun ViewingCard(
    cardShape: RoundedCornerShape,
    navController: NavController
) {
    CelestiaCard(
        iconRes = R.drawable.ic_pencil,
        iconTint = CelestiaTeal,
        title = "Placeholder",
        mainRow = {
            Icon(
                painter = painterResource(id = R.drawable.ic_note),
                contentDescription = "Viewing Icon",
                tint = CelestiaTeal,
                modifier = Modifier
                    .size(18.dp)
                    .alignByBaseline()
            )
            Text(
                text = "Placeholder",
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        description = "Placeholder",
        shape = cardShape,
        onClick = { navController.navigate("observation_history") }
    )
}

/**
 * Generic UI component representing a Celestia dashboard card.
 *
 * This component provides:
 * - An icon
 * - A title
 * - A customizable main row for key data
 * - A description line
 *
 * @param iconRes Icon resource to display.
 * @param iconTint Tint color for the icon.
 * @param title Title text of the card section.
 * @param mainRow Composable row used to display main summary data.
 * @param description A concise explanation or metadata line.
 * @param shape Card corner shape.
 * @param onClick Optional lambda triggered when card is tapped.
 */
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}