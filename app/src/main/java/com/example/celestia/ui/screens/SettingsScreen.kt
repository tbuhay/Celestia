package com.example.celestia.ui.screens

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
    val use24h by settingsVM.timeFormat24H.observeAsState(true)
    val scrollState = rememberScrollState()

    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            settingsVM.setUseDeviceLocation(true)
            Toast.makeText(context, "Location enabled.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

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
                .fillMaxSize()
                .verticalScroll(scrollState),
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
                    val refreshOnLaunch by settingsVM.refreshOnLaunch.observeAsState(false)

                    SettingsToggleRow(
                        title = "Refresh on Launch",
                        subtitle = "Update data automatically when opening the app",
                        icon = Icons.Default.Refresh,
                        checked = refreshOnLaunch,
                        onCheckedChange = { settingsVM.setRefreshOnLaunch(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    SettingsToggleRow(
                        title = "24-Hour Time",
                        subtitle = if (use24h) "Using 24-hour format" else "Using AM/PM format",
                        icon = Icons.Default.AccessTime,
                        checked = use24h,
                        onCheckedChange = { settingsVM.setTimeFormat(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    val useDeviceLocation by settingsVM.useDeviceLocation.observeAsState(false)

                    SettingsToggleRow(
                        title = "Use Device Location",
                        subtitle = "Get lunar phase data using your actual location",
                        icon = Icons.Default.Public,
                        checked = useDeviceLocation,
                        onCheckedChange = { enabled ->

                            if (enabled) {
                                // User wants to turn ON — ask for permission first
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    settingsVM.setUseDeviceLocation(true)
                                } else {
                                    locationPermissionLauncher.launch(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                }
                            } else {
                                // User turned it OFF
                                settingsVM.setUseDeviceLocation(false)
                            }
                        }
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
