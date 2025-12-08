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
import com.example.celestia.utils.LunarHelper
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar

/**
 * **Home screen of the Celestia application.**
 *
 * This screen acts as the user’s personalized dashboard and displays a summary of
 * several space-weather systems, including:
 *
 * - **Kp Index** (geomagnetic activity)
 * - **International Space Station** location
 * - **Asteroid close-approach data**
 * - **Current lunar phase**
 * - **Latest Observation Journal entry**
 *
 * The UI reacts to state exposed by [CelestiaViewModel] and user preferences from
 * [SettingsViewModel]. If the user has enabled *Refresh on Launch*, the screen will
 * automatically trigger a full data refresh on first composition.
 *
 * @param navController Used for navigating to details screens (ISS, Kp Index, etc.).
 * @param vm The main Celestia ViewModel providing NOAA, ISS, lunar, asteroid, and journal data.
 * @param settingsVM ViewModel managing persistent user preferences stored in DataStore.
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    vm: CelestiaViewModel,
    settingsVM: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // ------------------------------
    // LiveData observers
    // ------------------------------
    val readings by vm.readings.observeAsState(emptyList())
    val issReading by vm.issReading.observeAsState()
    val lunarPhase by vm.lunarPhase.observeAsState()
    val nextAsteroid by vm.nextAsteroid.observeAsState()
    val asteroidList by vm.asteroidList.observeAsState(emptyList())
    val refreshOnLaunch by settingsVM.refreshOnLaunchEnabled.observeAsState(false)

    val featuredAsteroid = vm.getFeaturedAsteroid(asteroidList)
    val scrollState = rememberScrollState()
    val cardShape = RoundedCornerShape(14.dp)

    // ------------------------------
    // User identity & greeting
    // ------------------------------
    val user = FirebaseAuth.getInstance().currentUser
    val userName = user?.displayName ?: "Explorer"
    val greeting = remember { getGreetingMessage() }

    /**
     * Triggers an automatic full refresh on first composition when the user
     * has enabled "Refresh on Launch" in settings.
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

            // ------------------------------
            // Greeting header
            // ------------------------------
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

            // ------------------------------
            // Dashboard Cards
            // ------------------------------
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

                ObservationNotesCard(
                    cardShape = cardShape,
                    navController = navController,
                    vm = vm
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
 * Returns a greeting message based on the user's local time of day.
 *
 * @return `"Good morning"`, `"Good afternoon"`, `"Good evening"`, or `"Hello"` as fallback.
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
 * Card displaying the **current NOAA Kp Index**, including a numeric value and
 * descriptive severity. Tapping the card navigates to the full Kp Index screen.
 *
 * @param readings Recent Kp readings loaded from Room.
 * @param vm ViewModel used to compute the most accurate Kp value.
 * @param cardShape Card corner rounding for consistent styling.
 * @param navController Navigation controller for deeper Kp details.
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
                style = MaterialTheme.typography.displayMedium.copy(color = color)
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
 * Card presenting the **current International Space Station (ISS) position**, including
 * formatted latitude/longitude and basic orbital metadata such as altitude and velocity.
 *
 * @param issReading Current ISS reading or `null` if data is unavailable.
 * @param cardShape Rounded card surface styling.
 * @param navController Used to navigate into the ISS detail screen.
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
                modifier = Modifier.size(18.dp).alignByBaseline()
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
 * Card summarizing **asteroid approach data**, showing either:
 * - A featured asteroid for the current day, or
 * - The next predicted close-approach asteroid.
 *
 * Tapping the card opens the full asteroid tracking screen.
 *
 * @param featuredAsteroid Asteroid ranked as most significant today.
 * @param nextAsteroid Soonest upcoming asteroid approach.
 * @param cardShape Card styling.
 * @param navController Navigation controller for asteroid details.
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
                modifier = Modifier.size(18.dp).alignByBaseline()
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
 * Card showing current **moon phase**, illumination percentage, and lunar age.
 * Uses [LunarHelper] to derive illumination and moon age.
 *
 * @param lunarPhase Live lunar metadata from the repository.
 * @param cardShape Rounded card shape.
 * @param navController Navigation controller for the detailed lunar phase screen.
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
                modifier = Modifier.size(18.dp).alignByBaseline()
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
 * Card representing the **Observation Journal** module. Shows:
 * - A quick preview of the user's most recent journal entry
 * - A date summary
 * - A fallback message when no entries exist
 *
 * @param cardShape Shape applied to the card surface.
 * @param navController Navigation controller for history screen.
 * @param vm ViewModel exposing journal entry data.
 */
@Composable
private fun ObservationNotesCard(
    cardShape: RoundedCornerShape,
    navController: NavController,
    vm: CelestiaViewModel
) {
    val entries by vm.allJournalEntries.observeAsState(emptyList())
    val latest = entries.maxByOrNull { it.timestamp }

    val latestDateText = if (latest != null) {
        val formatter = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
        formatter.format(java.util.Date(latest.timestamp))
    } else "No entries yet"

    val descriptionText = if (latest != null) {
        "Last observation: ${latest.observationTitle.ifBlank { "Untitled" }}"
    } else {
        "Start a new observation"
    }

    CelestiaCard(
        iconRes = R.drawable.ic_book,
        iconTint = CelestiaGreen,
        title = "Observation Journal",
        mainRow = {
            Icon(
                painter = painterResource(id = R.drawable.ic_pencil),
                contentDescription = "Viewing Icon",
                tint = CelestiaGreen,
                modifier = Modifier.size(18.dp).alignByBaseline()
            )
            Text(
                text = latestDateText,
                modifier = Modifier.alignByBaseline(),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        description = descriptionText,
        shape = cardShape,
        onClick = { navController.navigate("observation_history") }
    )
}

/**
 * **Generic Celestia dashboard card** used throughout the app.
 *
 * Provides a consistent elevated Card layout containing:
 * - A section icon
 * - A section title
 * - A customizable main data row
 * - A one-line description
 *
 * Cards are tappable when an [onClick] callback is provided.
 *
 * @param iconRes Resource ID for the leading icon.
 * @param iconTint Tint applied to the icon.
 * @param title Section title text.
 * @param mainRow Composable content representing the main summary row.
 * @param description Supporting metadata shown beneath the main row.
 * @param shape Card shape (rounded corners).
 * @param onClick Optional callback invoked when the card is tapped.
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
