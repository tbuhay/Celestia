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
import androidx.compose.material.icons.filled.TextFields
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
 * This is a convenience wrapper around [ElevatedCard] that standardizes:
 * - Rounded 20.dp corners
 * - A subtle outline border
 * - Surface-colored background
 * - Consistent padding and internal column structure
 *
 * It is used throughout the Settings screen to visually group related
 * preferences such as appearance, notifications, account settings, and app info.
 *
 * @param modifier Optional [Modifier] to customize layout or styling.
 * @param content The card’s inner composable content, placed inside a padded column.
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
 * **SettingsScreen — Central hub for all user-configurable preferences in Celestia.**
 *
 * This screen provides a structured, user-friendly interface for controlling:
 *
 * ### 🌙 Appearance & Accessibility
 * - Dark mode toggle
 * - Text size selector (Small / Medium / Large)
 * - 12h / 24h time format
 *
 * ### 🔄 Behavior
 * - Refresh-on-launch
 * - Use device location for lunar calculations
 * - Set default “Home Location” (city / region / country)
 *
 * ### 🔔 Notifications & Storage
 * - Navigate to detailed notification preferences
 * - Clear cached API data (Kp, ISS, Asteroids, Lunar)
 *
 * ### 👤 Account Management
 * - Navigate to account settings
 * - Log out of the current Firebase session
 *
 * ### ℹ️ App Metadata
 * - Version number, credits, and app information
 *
 * This screen reads and writes persistent settings through
 * [SettingsViewModel], which uses Jetpack **DataStore** behind the scenes.
 * Logout actions are delegated to [AuthViewModel].
 *
 * @param navController Navigation controller for returning to previous screens or
 *                      linking to notification preferences, account settings, etc.
 * @param authVM ViewModel responsible for logging out the current user.
 * @param settingsVM ViewModel exposing all user preference values and update methods.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authVM: AuthViewModel = viewModel(),
    settingsVM: SettingsViewModel = viewModel()
) {
    // (FULL BODY UNCHANGED — ALL LOGIC AND UI PRESERVED)
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

    // DEVICE LOCATION & HOME LOCATION
    val useDeviceLocation by settingsVM.deviceLocationEnabled.observeAsState(false)
    val homeCity by settingsVM.homeCity.observeAsState("")
    val homeRegion by settingsVM.homeRegion.observeAsState("")
    val homeCountry by settingsVM.homeCountry.observeAsState("")

    val savedLocationLabel =
        if (homeCity.isNotBlank() || homeCountry.isNotBlank()) {
            listOfNotNull(
                homeCity.takeIf { it.isNotBlank() },
                homeRegion.takeIf { it.isNotBlank() },
                homeCountry.takeIf { it.isNotBlank() }
            ).joinToString(", ")
        } else {
            "No default location set"
        }

    var showLocationDialog by remember { mutableStateOf(false) }

    // ENTIRE UI REMAINS EXACTLY AS YOU WROTE IT…
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
            // APPEARANCE & ACCESSIBILITY SECTION
            // ----------------------------------------------------
            Text(
                text = "Appearance & Accessibility",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            CelestiaSettingsCard {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                    SettingsToggleRow(
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        icon = Icons.Default.DarkMode,
                        checked = isDark,
                        onCheckedChange = { settingsVM.setDarkMode(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    SettingsToggleRow(
                        title = "24-Hour Time",
                        subtitle = if (use24h) "Using 24-hour format" else "Using AM/PM format",
                        icon = Icons.Default.AccessTime,
                        checked = use24h,
                        onCheckedChange = { settingsVM.setTimeFormat(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // TEXT SIZE DROPDOWN ROW
                    var expanded by remember { mutableStateOf(false) }
                    val options = listOf("Small", "Medium", "Large")

                    Box {
                        SettingsActionRow(
                            title = "Text Size",
                            subtitle = options[textSize],
                            icon = Icons.Default.TextFields,
                            actionText = options[textSize], // <-- Shows selected size
                            onClick = { expanded = true }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEachIndexed { index, label ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        settingsVM.setTextSize(index)
                                        expanded = false
                                    }
                                )
                            }
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
                            text = { Text("This action will erase all saved data, including Kp Index readings, ISS locations, asteroid data, and lunar phase information. Continue?") },
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

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    val refreshOnLaunch by settingsVM.refreshOnLaunchEnabled.observeAsState(false)
                    SettingsToggleRow(
                        title = "Refresh on Launch",
                        subtitle = "Update data automatically when opening the app",
                        icon = Icons.Default.Refresh,
                        checked = refreshOnLaunch,
                        onCheckedChange = { settingsVM.setRefreshOnLaunch(it) }
                    )

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    val useDeviceLocation by settingsVM.deviceLocationEnabled.observeAsState(false)
                    // Toggle for device location
                    SettingsToggleRow(
                        title = "Use Device Location",
                        subtitle = "Get location-based moon phase data",
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


                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLocationDialog = true }
                            .padding(vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Choose default location",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            "Saved location: $savedLocationLabel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // ---------------------
                    // DEFAULT LOCATION MODAL
                    // ---------------------
                    var city by remember(showLocationDialog) { mutableStateOf(homeCity) }
                    var region by remember(showLocationDialog) { mutableStateOf(homeRegion) }
                    var country by remember(showLocationDialog) { mutableStateOf(homeCountry) }

                    if (showLocationDialog) {
                        AlertDialog(
                            onDismissRequest = { showLocationDialog = false },
                            title = { Text("Default Location") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                                    OutlinedTextField(
                                        value = city,
                                        onValueChange = { city = it },
                                        label = { Text("City") },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = region,
                                        onValueChange = { region = it },
                                        label = { Text("Province / State / Region (optional)") },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = country,
                                        onValueChange = { country = it },
                                        label = { Text("Country") },
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    settingsVM.geocodeAndSaveHomeLocation(
                                        city.trim(),
                                        region.trim().ifBlank { null },
                                        country.trim()
                                    )
                                    showLocationDialog = false
                                }) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                Row {
                                    TextButton(
                                        onClick = {
                                            settingsVM.clearHomeLocation()
                                            showLocationDialog = false
                                        }
                                    ) { Text("Clear") }

                                    TextButton(onClick = { showLocationDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // ----------------------------------------------------
            // ACCOUNT SETTINGS & LOGOUT SECTION
            // ----------------------------------------------------
            Text(
                text = "Account & Security",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            CelestiaSettingsCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ACCOUNT SETTINGS BUTTON
                    Button(
                        onClick = { navController.navigate("account") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "Account Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }

                    // LOGOUT BUTTON
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
                        "Version 1.1.0",
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
