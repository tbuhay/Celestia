package com.example.celestia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    navController: NavController,
    settingsVM: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
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

            // KP Alerts
            NotificationToggleCard(
                title = "Kp Index Alerts",
                description = "Receive alerts when Kp Index is 5 or higher (Minor Storm).",
                enabled = kpAlertsEnabled,
                icon = Icons.Default.Bolt,
                onToggle = { settingsVM.setKpAlertsEnabled(it) }
            )

            // ISS Alerts
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

@Composable
private fun NotificationToggleCard(
    title: String,
    description: String,
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodySmall)
            }

            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
    }
}
