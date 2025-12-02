package com.example.celestia.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.SettingsViewModel

/**
 * Screen allowing users to toggle notification preferences within Celestia.
 *
 * Users can enable/disable:
 * - **Kp Index Alerts** — Fired when geomagnetic activity reaches storm levels.
 * - **ISS Alerts** — Fired when the ISS is overhead or a relevant event occurs.
 *
 * These preferences are persisted using [SettingsViewModel] and DataStore.
 *
 * @param navController Navigation controller for returning to previous screens.
 * @param settingsVM ViewModel managing notification preference state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    navController: NavController,
    settingsVM: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Observed states for toggles
    val kpAlertsEnabled by settingsVM.kpAlertsEnabled.observeAsState(false)
    val issAlertsEnabled by settingsVM.issAlertsEnabled.observeAsState(false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification Preferences") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Text(
                "Alerts",
                style = MaterialTheme.typography.titleLarge
            )

            // Kp Index Alerts toggle
            NotificationToggleCard(
                title = "Kp Index Alerts",
                description = "Receive alerts when Kp Index is 3.5 or higher (Minor Storm).",
                enabled = kpAlertsEnabled,
                icon = Icons.Default.Bolt,
                onToggle = { settingsVM.setKpAlertsEnabled(it) }
            )

            // ISS Alerts toggle
            NotificationToggleCard(
                title = "ISS Alerts",
                description = "Receive alerts when the ISS is overhead or at key events.",
                enabled = issAlertsEnabled,
                icon = Icons.Default.Public,
                onToggle = { settingsVM.setIssAlertsEnabled(it) }
            )
        }
    }
}

/**
 * A reusable card component displaying a single notification toggle option.
 *
 * Each card provides:
 * - An icon
 * - A title describing the alert
 * - A short explanation
 * - A switch for enabling or disabling the alert
 *
 * @param title The name of the alert category.
 * @param description Extra information describing when the alert is triggered.
 * @param enabled Current toggle state.
 * @param icon Icon representing the alert type (e.g., lightning = Kp).
 * @param onToggle Callback fired when the user toggles the switch.
 */
@Composable
private fun NotificationToggleCard(
    title: String,
    description: String,
    enabled: Boolean,
    icon: ImageVector,
    onToggle: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}
