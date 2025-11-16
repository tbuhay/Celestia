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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.R
import com.example.celestia.data.model.AsteroidApproach
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsteroidTrackingScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel()
) {
    // Raw asteroid data from Room
    val rawList = vm.asteroidList.observeAsState(emptyList()).value

    val featured = vm.getFeaturedAsteroid(rawList)
    val weekList = vm.getNext7DaysList(rawList)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Asteroid Tracking",
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

            // FEATURED ASTEROID
            if (featured != null) {

                item {
                    Text(
                        "Featured Asteroid",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Most meaningful object based on size and close-approach distance",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }

                item {
                    FeaturedAsteroidCard(asteroid = featured)
                }
            }

            item {
                Text(
                    "Upcoming Close Approaches (Next 7 Days)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // LIST OF FILTERED ASTEROIDS
            items(weekList) { asteroid ->
                AsteroidListCard(asteroid)
            }

            // CLASSIFICATION CARD
            item {
                ClassificationCard()
            }
        }
    }
}

@Composable
fun FeaturedAsteroidCard(asteroid: AsteroidApproach) {

    val borderColor = if (asteroid.isPotentiallyHazardous)
        Color(0xFFD32F2F)
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    asteroid.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(Modifier.width(12.dp))

                if (asteroid.isPotentiallyHazardous) {
                    HazardBadge()
                }
            }

            Text(
                if (asteroid.isPotentiallyHazardous)
                    "Potentially Hazardous Asteroid"
                else
                    "Near-Earth Asteroid",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            MetricsRow(asteroid)

            if (asteroid.isPotentiallyHazardous) {
                HazardWarningBox()
            }
        }
    }
}

@Composable
fun AsteroidListCard(asteroid: AsteroidApproach) {

    val borderColor = if (asteroid.isPotentiallyHazardous)
        Color(0xFFD32F2F)
    else
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    asteroid.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (asteroid.isPotentiallyHazardous) {
                    HazardBadge()
                }
            }

            Text(
                if (asteroid.isPotentiallyHazardous)
                    "Potentially Hazardous Asteroid"
                else
                    "Near-Earth Asteroid",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            MetricsRow(asteroid)

            if (asteroid.isPotentiallyHazardous) {
                HazardWarningBox()
            }
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

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
            Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun HazardBadge() {
    Surface(
        color = Color(0xFFD32F2F),
        shape = RoundedCornerShape(50),
        tonalElevation = 4.dp
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
        color = Color(0x22D32F2F),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFD32F2F))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This asteroid is classified as potentially hazardous due to its size and close approach distance to Earth.",
                color = Color(0xFFEF6969),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun ClassificationCard() {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Classification",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Potentially Hazardous Asteroids (PHAs) are defined as asteroids larger than 140 meters that approach Earth within 0.05 AU.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
            )

            Text(
                "Data sourced from NASA’s Center for Near-Earth Object Studies (CNEOS)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
