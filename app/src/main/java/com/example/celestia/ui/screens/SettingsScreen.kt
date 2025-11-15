package com.example.celestia.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.components.SettingsActionRow
import com.example.celestia.ui.components.SettingsToggleRow
import com.example.celestia.ui.viewmodel.AuthViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel

// -----------------------------------------------------------------------------
//  Shared Celestia Settings Card (Theme-Aware)
// -----------------------------------------------------------------------------
@Composable
fun CelestiaSettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier
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
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authVM: AuthViewModel = viewModel(),
    settingsVM: SettingsViewModel = viewModel()
) {
    // Observe Dark Mode setting
    val isDark by settingsVM.darkModeEnabled.observeAsState(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ----------------------------------------------------
            // PREFERENCES SECTION
            // ----------------------------------------------------
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            CelestiaSettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    // Notifications
                    SettingsToggleRow(
                        title = "Notifications",
                        subtitle = "Alert me about space events",
                        icon = Icons.Default.Notifications,
                        checked = false,
                        onCheckedChange = {}
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Dark Mode
                    SettingsToggleRow(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        icon = Icons.Default.DarkMode,
                        checked = isDark,
                        onCheckedChange = { settingsVM.setDarkMode(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Auto Refresh
                    SettingsToggleRow(
                        title = "Auto Refresh",
                        subtitle = "Update data automatically",
                        icon = Icons.Default.Refresh,
                        checked = false,
                        onCheckedChange = {}
                    )
                }
            }

            // ----------------------------------------------------
            // DATA & STORAGE SECTION
            // ----------------------------------------------------
            Text(
                text = "Data & Storage",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            CelestiaSettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    SettingsActionRow(
                        title = "Units",
                        subtitle = "Metric",
                        icon = Icons.Default.Public,
                        actionText = "Change",
                        onClick = { }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    SettingsActionRow(
                        title = "Clear Cache",
                        subtitle = "Free up storage space",
                        icon = Icons.Default.Delete,
                        actionText = "Clear",
                        onClick = { }
                    )
                }
            }

            // ----------------------------------------------------
            // LOGOUT BUTTON
            // ----------------------------------------------------
            CelestiaSettingsCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            authVM.logout()
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Log Out",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // APP INFO
            // ----------------------------------------------------
            CelestiaSettingsCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Space Weather Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        "Built with data from NOAA, NASA, and other space agencies",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
