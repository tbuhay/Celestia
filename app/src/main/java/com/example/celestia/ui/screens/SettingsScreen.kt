package com.example.celestia.ui.screens

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.*
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
import com.example.celestia.ui.theme.CelestiaHazardRed
import com.example.celestia.ui.viewmodel.AuthViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel

/**
 * A reusable settings card styled specifically for Celestia.
 *
 * Provides:
 * - A rounded elevated surface
 * - A subtle outline border
 * - A unified layout for all settings sections
 *
 * @param modifier Optional layout modifier.
 * @param content The column content of the card.
 */
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

/**
 * Main Settings screen for Celestia.
 *
 * Includes categories:
 * - **Preferences**: Dark mode, refresh on launch, 24-hour time, device location.
 * - **Accessibility**: Text size options.
 * - **Data & Storage**: Notification preferences + clearing cache.
 * - **Account Management**: Logout action.
 * - **App Info**: Version and credits.
 *
 * This screen persists user choices with DataStore-backed [SettingsViewModel].
 *
 * @param navController Used for navigating back or to notification preferences.
 * @param authVM Handles logging out of the user.
 * @param settingsVM Accesses and updates user settings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authVM: AuthViewModel = viewModel(),
    settingsVM: SettingsViewModel = viewModel()
) {
    // Preference states
    val isDark by settingsVM.darkModeEnabled.observeAsState(true)
    val use24h by settingsVM.timeFormat24h.observeAsState(true)
    val textSize by settingsVM.textSize.observeAsState(1)
    val scrollState: ScrollState = rememberScrollState()
    val context = LocalContext.current

    // Permission launcher for enabling device location
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

                    // Dark Mode toggle
                    SettingsToggleRow(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        icon = Icons.Default.DarkMode,
                        checked = isDark,
                        onCheckedChange = { settingsVM.setDarkMode(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Refresh on launch toggle
                    val refreshOnLaunch by settingsVM.refreshOnLaunchEnabled.observeAsState(false)
                    SettingsToggleRow(
                        title = "Refresh on Launch",
                        subtitle = "Update data automatically when opening the app",
                        icon = Icons.Default.Refresh,
                        checked = refreshOnLaunch,
                        onCheckedChange = { settingsVM.setRefreshOnLaunch(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // 24-hour clock formatting
                    SettingsToggleRow(
                        title = "24-Hour Time",
                        subtitle = if (use24h) "Using 24-hour format" else "Using AM/PM format",
                        icon = Icons.Default.AccessTime,
                        checked = use24h,
                        onCheckedChange = { settingsVM.setTimeFormat(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Device location toggle
                    val useDeviceLocation by settingsVM.deviceLocationEnabled.observeAsState(false)
                    SettingsToggleRow(
                        title = "Use Device Location",
                        subtitle = "Get moon phase data using your actual location",
                        icon = Icons.Default.Public,
                        checked = useDeviceLocation,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) settingsVM.setUseDeviceLocation(true)
                                else locationPermissionLauncher.launch(
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            } else {
                                settingsVM.setUseDeviceLocation(false)
                            }
                        }
                    )
                }
            }

            // ----------------------------------------------------
            // ACCESSIBILITY SECTION
            // ----------------------------------------------------
            Text(
                text = "Accessibility",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            CelestiaSettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    Text("Text Size", style = MaterialTheme.typography.titleMedium)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val options = listOf("Small", "Medium", "Large")

                        options.forEachIndexed { index, label ->
                            AssistChip(
                                onClick = { settingsVM.setTextSize(index) },
                                label = { Text(label) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor =
                                        if (textSize == index)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
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

                    // Navigate to notification preference screen
                    SettingsActionRow(
                        title = "Notifications",
                        subtitle = "Manage alert preferences",
                        icon = Icons.Default.Notifications,
                        actionText = "Edit",
                        onClick = { navController.navigate("notification_preferences") }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Clear cache confirmation dialog
                    var showClearCacheDialog by remember { mutableStateOf(false) }

                    if (showClearCacheDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearCacheDialog = false },
                            title = { Text("Clear Cache?") },
                            text = { Text("This will delete all saved Kp Index, ISS, asteroid, and lunar data.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showClearCacheDialog = false
                                    settingsVM.clearCache {
                                        Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Text("Confirm")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showClearCacheDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    SettingsActionRow(
                        title = "Clear Cache",
                        subtitle = "Free up storage space",
                        icon = Icons.Default.Delete,
                        actionText = "Clear",
                        onClick = { showClearCacheDialog = true }
                    )
                }
            }

            // ----------------------------------------------------
            // LOGOUT SECTION
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
                            containerColor = CelestiaHazardRed
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Log Out",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onTertiary
                            )
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // APP INFO SECTION
            // ----------------------------------------------------
            CelestiaSettingsCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "© 2025 Celestia",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        "Version 1.0.2",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        "Developed by Tyler Buhay",
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
