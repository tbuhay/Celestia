package com.example.celestia.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.ui.theme.CelestiaHazardRed
import com.example.celestia.ui.theme.CelestiaHazardRedLight
import com.example.celestia.ui.theme.TextPrimary
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsteroidTrackingScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    val rawList = vm.asteroidList.observeAsState(emptyList()).value

    val featured = vm.getFeaturedAsteroid(rawList)
    val weekList = vm.getNext7DaysList(rawList)

    val cardShape = RoundedCornerShape(20.dp)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Asteroid Tracking",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
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

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // FEATURED
            if (featured != null) {
                item {
                    Text(
                        "Featured Asteroid",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        "Most significant object based on size + approach distance",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                item {
                    FeaturedAsteroidCard(
                        asteroid = featured,
                        shape = cardShape
                    )
                }
            }

            // UPCOMING LIST
            item {
                Text(
                    "Upcoming Close Approaches (Next 7 Days)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            items(weekList) { asteroid ->
                AsteroidListCard(
                    asteroid = asteroid,
                    shape = cardShape
                )
            }

            item { ClassificationCard(shape = cardShape) }
        }
    }
}

@Composable
fun FeaturedAsteroidCard(
    asteroid: AsteroidApproach,
    shape: RoundedCornerShape
) {
    val borderColor =
        if (asteroid.isPotentiallyHazardous) CelestiaHazardRed
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, shape)
            .clearAndSetSemantics {},
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    asteroid.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (asteroid.isPotentiallyHazardous) { HazardBadge() }
            }

            Text(
                text = if (asteroid.isPotentiallyHazardous)
                    "Potentially Hazardous Asteroid"
                else
                    "Near-Earth Asteroid",
                modifier = Modifier.semantics {
                    contentDescription =
                        if (asteroid.isPotentiallyHazardous)
                            "Hazard classification: Potentially hazardous asteroid"
                        else
                            "Classification: Near-Earth asteroid"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            MetricsRow(asteroid)

            if (asteroid.isPotentiallyHazardous) { HazardWarningBox() }
        }
    }
}

@Composable
fun AsteroidListCard(
    asteroid: AsteroidApproach,
    shape: RoundedCornerShape
) {
    val borderColor =
        if (asteroid.isPotentiallyHazardous) CelestiaHazardRed
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, shape)
            .clearAndSetSemantics {},
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = shape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    asteroid.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )
                if (asteroid.isPotentiallyHazardous) { HazardBadge() }
            }

            Text(
                if (asteroid.isPotentiallyHazardous)
                    "Potentially Hazardous Asteroid"
                else
                    "Near-Earth Asteroid",
                modifier = Modifier.semantics {
                    contentDescription =
                        if (asteroid.isPotentiallyHazardous)
                            "Hazard classification: Potentially hazardous asteroid"
                        else
                            "Classification: Near-Earth asteroid"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            MetricsRow(asteroid)

            if (asteroid.isPotentiallyHazardous) { HazardWarningBox() }
        }
    }
}

@Composable
fun MetricsRow(asteroid: AsteroidApproach) {
    val date = LocalDate.parse(asteroid.approachDate)
    val formattedDate = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

    val avgDiameter = ((asteroid.diameterMinMeters + asteroid.diameterMaxMeters) / 2).toInt()
    val kmPerSecond = asteroid.relativeVelocityKph / 3600.0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                icon = R.drawable.ic_calendar,
                label = "Closest Approach",
                value = formattedDate
            )
            MetricItem(
                icon = R.drawable.ic_distance,
                label = "Distance",
                value = String.format("%.3f AU", asteroid.missDistanceAu)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricItem(
                icon = R.drawable.ic_ruler,
                label = "Diameter",
                value = "~${avgDiameter} m"
            )
            MetricItem(
                icon = R.drawable.ic_speed,
                label = "Velocity",
                value = String.format("%.1f km/s", kmPerSecond)
            )
        }
    }
}

@Composable
fun MetricItem(icon: Int, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics {
            contentDescription = "$label: $value"
        }
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun HazardBadge() {
    Surface(
        color = CelestiaHazardRed,
        shape = RoundedCornerShape(50),
        shadowElevation = 4.dp,
        modifier = Modifier.semantics {
            contentDescription = "Hazard status: hazardous asteroid"
        }
    ) {
        Text(
            text = "Hazardous",
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun HazardWarningBox() {
    Surface(
        color = CelestiaHazardRed.copy(alpha = 0.25f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CelestiaHazardRed),
        modifier = Modifier.semantics {
            contentDescription =
                "Hazard explanation: This asteroid is classified as potentially hazardous due to its size and close approach distance to Earth."   // ★ A11Y FIX
        }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This asteroid is classified as potentially hazardous due to its size and close approach distance to Earth.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            )
        }
    }
}

@Composable
fun ClassificationCard(shape: RoundedCornerShape) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Classification",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )

            Text(
                "Potentially Hazardous Asteroids (PHAs) are defined as asteroids larger than 140 meters that approach Earth within 0.05 AU.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Text(
                "Data sourced from NASA’s Center for Near-Earth Object Studies (CNEOS)",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}
