package com.example.celestia.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestia.data.model.ObservationEntry
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Photo
import androidx.compose.ui.text.input.KeyboardType
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.FormatUtils

// -----------------------------------------------------------------------------
// OBSERVATION HISTORY SCREEN (LIST OF ENTRIES)
// -----------------------------------------------------------------------------

/**
 * **Observation History Screen**
 *
 * Displays a scrollable chronological list of all user-created observation journal
 * entries, showing:
 * - Entry title and timestamp
 * - Saved location
 * - Weather and sky metrics (Kp Index, weather summary)
 * - Optional thumbnail photo
 *
 * Features:
 * - Clicking an entry opens its detail view (`observation_detail/{id}`)
 * - Top bar includes an action button to create a new entry
 * - Automatically formats timestamps according to the user's 12h/24h preference
 *
 * @param navController Navigation controller for moving to entry details or new entry.
 * @param vm The main CelestiaViewModel used to fetch journal entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationHistoryScreen(
    navController: NavController,
    vm: CelestiaViewModel
) {
    val entries by vm.allJournalEntries.observeAsState(emptyList())

    val settingsVM: SettingsViewModel = viewModel()
    val use24h by settingsVM.timeFormat24h.observeAsState(true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Observation Journal", style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("observation_new") }) {
                        Icon(Icons.Default.Add, contentDescription = "New Observation")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        if (entries.isEmpty()) {
            EmptyHistoryContent(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(entries) { entry ->
                    ObservationHistoryItem(
                        entry = entry,
                        use24h = use24h,
                        onClick = {
                            navController.navigate("observation_detail/${entry.id}")
                        }
                    )
                }
            }
        }
    }
}

/**
 * Displays a centered message when the user has no observation entries.
 *
 * @param padding Outer padding passed from the Scaffold content.
 */
@Composable
private fun EmptyHistoryContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "No observations yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap the + icon to create your first entry.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Represents a single journal entry in the Observation History list.
 *
 * Shows:
 * - Title (or timestamp if untitled)
 * - Location name
 * - Weather + Kp Index summary
 * - Optional notes preview
 * - Optional thumbnail photo
 *
 * @param entry The journal entry model.
 * @param use24h Whether timestamps should be formatted using 24-hour format.
 * @param onClick Lambda triggered when the card is tapped.
 */
@Composable
private fun ObservationHistoryItem(
    entry: ObservationEntry,
    use24h: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Optional thumbnail photo
            if (entry.photoUrl != null) {
                AsyncImage(
                    model = entry.photoUrl,
                    contentDescription = "Observation photo",
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f)) {

                val tsText = FormatUtils.formatUpdatedTimestamp(entry.timestamp.toString(), use24h)

                Text(
                    text = if (entry.observationTitle.isNotBlank())
                        "${entry.observationTitle} • $tsText"
                    else tsText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    entry.locationName ?: "Unknown location",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                val kpText = entry.kpIndex?.let { "Kp ${"%.1f".format(it)}" }
                val weatherText = entry.weatherSummary

                Text(
                    listOfNotNull(kpText, weatherText)
                        .joinToString(" • ")
                        .ifBlank { "No sky data captured" },
                    style = MaterialTheme.typography.bodySmall
                )

                if (entry.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        entry.notes,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// OBSERVATION EDITOR (NEW / EDIT ENTRY)
// -----------------------------------------------------------------------------

/**
 * **Observation Editor Screen**
 *
 * Allows the user to create a new observation journal entry or edit an existing one.
 * The editor supports:
 *
 * - Title, notes, and location entry
 * - Auto-filled environmental data (Kp Index, weather, device/home coordinates)
 * - Optional photo attachment with thumbnail and full-screen preview
 * - ISS positional data (optional)
 *
 * Behavior:
 * - If `entryId` is null → creates a new entry
 * - If `entryId` is provided → loads existing entry into the form
 * - When auto-fill is enabled, the ViewModel injects real-time sky data
 *
 * @param navController Navigation controller for returning after save.
 * @param vm The main CelestiaViewModel providing journal data + auto-fill fields.
 * @param entryId ID of the entry being edited (or `null` for new).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationEditorScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel(),
    entryId: Int?
) {
    val context = LocalContext.current

    // User preferences
    val settingsVM: SettingsViewModel = viewModel()
    val useDeviceLocation by settingsVM.deviceLocationEnabled.observeAsState(false)
    val use24h by settingsVM.timeFormat24h.observeAsState(true)

    // Auto-filled values
    val autoFields by vm.autoObservationFields.observeAsState()

    // Editing existing entry?
    var isLoading by remember { mutableStateOf(entryId != null) }
    var existingEntry by remember { mutableStateOf<ObservationEntry?>(null) }

    // ----------------------------------------
    // INITIAL LOAD → load entry or auto-fill
    // ----------------------------------------
    LaunchedEffect(entryId, useDeviceLocation) {
        if (entryId == null) {
            vm.refreshAutoObservationFieldsForNewEntry()
        } else {
            existingEntry = vm.getJournalEntry(entryId)
        }
        isLoading = false
    }

    // Show loading spinner
    if (isLoading) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Observation") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // ----------------------------------------
    // FORM STATE
    // ----------------------------------------
    val initial = existingEntry
    val baseTimestamp = existingEntry?.timestamp ?: System.currentTimeMillis()
    val formattedDate = remember(baseTimestamp, use24h) {
        FormatUtils.formatUpdatedTimestamp(baseTimestamp.toString(), use24h)
    }

    var observationTitle by remember(initial) { mutableStateOf(initial?.observationTitle ?: "") }
    var titleError by remember { mutableStateOf<String?>(null) }

    var locationName by remember(initial) { mutableStateOf(initial?.locationName ?: "") }
    var latitudeText by remember(initial) { mutableStateOf(FormatUtils.formatLatitudePlain(initial?.latitude)) }
    var longitudeText by remember(initial) { mutableStateOf(FormatUtils.formatLongitudePlain(initial?.longitude)) }

    var kpText by remember(initial) { mutableStateOf(initial?.kpIndex?.toString() ?: "") }
    var issLatText by remember(initial) { mutableStateOf(FormatUtils.formatLatitudePlain(initial?.issLat)) }
    var issLonText by remember(initial) { mutableStateOf(FormatUtils.formatLongitudePlain(initial?.issLon)) }

    var weatherSummary by remember(initial) { mutableStateOf(initial?.weatherSummary ?: "") }
    var temperatureText by remember(initial) { mutableStateOf(initial?.temperatureC?.toString() ?: "") }
    var cloudCoverText by remember(initial) { mutableStateOf(initial?.cloudCoverPercent?.toString() ?: "") }

    var notes by remember(initial) { mutableStateOf(initial?.notes ?: "") }
    var notesError by remember { mutableStateOf<String?>(null) }

    var photoUrl by remember(initial) { mutableStateOf(initial?.photoUrl ?: "") }

    // ----------------------------------------
    // PHOTO PICKER
    // ----------------------------------------
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            photoUrl = it.toString()
        }
    }

    // ----------------------------------------
    // APPLY AUTO-FILL VALUES ON NEW ENTRY
    // ----------------------------------------
    LaunchedEffect(autoFields) {
        if (entryId == null && autoFields != null) {
            autoFields?.let { auto ->

                if (latitudeText.isBlank() && auto.latitude != null)
                    latitudeText = FormatUtils.formatLatitudePlain(auto.latitude)

                if (longitudeText.isBlank() && auto.longitude != null)
                    longitudeText = FormatUtils.formatLongitudePlain(auto.longitude)

                if (kpText.isBlank() && auto.kpIndex != null)
                    kpText = auto.kpIndex.toString()

                if (issLatText.isBlank() && auto.issLat != null)
                    issLatText = FormatUtils.formatLatitudePlain(auto.issLat)

                if (issLonText.isBlank() && auto.issLon != null)
                    issLonText = FormatUtils.formatLongitudePlain(auto.issLon)

                if (temperatureText.isBlank() && auto.temperatureC != null)
                    temperatureText = auto.temperatureC.toString()

                if (cloudCoverText.isBlank() && auto.cloudCoverPercent != null)
                    cloudCoverText = auto.cloudCoverPercent.toString()

                if (weatherSummary.isBlank() && auto.weatherSummary != null)
                    weatherSummary = auto.weatherSummary

                if (locationName.isBlank() && auto.locationName != null)
                    locationName = auto.locationName
            }
        }
    }

    // ----------------------------------------
    // SAVE LOGIC
    // ----------------------------------------
    val titleFocus = remember { FocusRequester() }
    val notesFocus = remember { FocusRequester() }

    fun handleSave() {
        if (observationTitle.isBlank()) {
            titleError = "Title cannot be empty"
            titleFocus.requestFocus()
            return
        }

        if (notes.isBlank()) {
            notesError = "Notes cannot be empty"
            notesFocus.requestFocus()
            return
        }

        val lat = latitudeText.toDoubleOrNull()
        val lon = longitudeText.toDoubleOrNull()
        val kp = kpText.toDoubleOrNull()
        val issLat = issLatText.toDoubleOrNull()
        val issLon = issLonText.toDoubleOrNull()
        val temp = temperatureText.toDoubleOrNull()
        val cloud = cloudCoverText.toIntOrNull()

        val entry =
            existingEntry?.copy(
                observationTitle = observationTitle.trim(),
                locationName = locationName.trim().ifBlank { null },
                latitude = lat,
                longitude = lon,
                kpIndex = kp,
                issLat = issLat,
                issLon = issLon,
                weatherSummary = weatherSummary.trim().ifBlank { null },
                temperatureC = temp,
                cloudCoverPercent = cloud,
                notes = notes.trim(),
                photoUrl = photoUrl.takeIf { it.isNotBlank() }
            )
                ?: ObservationEntry(
                    id = 0,
                    timestamp = baseTimestamp,
                    observationTitle = observationTitle.trim(),
                    locationName = locationName.trim().ifBlank { null },
                    latitude = lat,
                    longitude = lon,
                    kpIndex = kp,
                    issLat = issLat,
                    issLon = issLon,
                    weatherSummary = weatherSummary.trim().ifBlank { null },
                    temperatureC = temp,
                    cloudCoverPercent = cloud,
                    notes = notes.trim(),
                    photoUrl = photoUrl.takeIf { it.isNotBlank() }
                )

        vm.saveJournalEntry(entry)
        navController.popBackStack()
    }

    // ----------------------------------------
    // SCREEN UI
    // ----------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingEntry != null) "Edit Observation" else "New Observation",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // --------------------------------------------
                    // DELETE BUTTON (only when editing an entry)
                    // --------------------------------------------
                    if (existingEntry != null) {
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Entry",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Entry?") },
                                text = { Text("Are you sure you want to delete this observation? This cannot be undone.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            existingEntry?.let { vm.deleteJournalEntry(it) }
                                            showDeleteDialog = false
                                            navController.popBackStack()
                                        }
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                    IconButton(onClick = { handleSave() }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ----------------------------
            // Title field
            // ----------------------------
            OutlinedTextField(
                value = observationTitle,
                onValueChange = {
                    observationTitle = it
                    if (titleError != null && it.isNotBlank()) titleError = null
                },
                label = { Text("Title") },
                isError = titleError != null,
                supportingText = { titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocus),
                singleLine = true
            )

            Text("Observation time and location", style = MaterialTheme.typography.titleMedium)
            Text(formattedDate, style = MaterialTheme.typography.bodyMedium)

            // ----------------------------
            // Location
            // ----------------------------
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location") },
                placeholder = { Text("City, Province/State, Country") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Divider()

            // ----------------------------
            // Sky conditions
            // ----------------------------
            Text("Sky conditions", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = kpText,
                onValueChange = { kpText = it },
                label = { Text("Kp Index") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = weatherSummary,
                onValueChange = { weatherSummary = it },
                label = { Text("Weather summary") },
                placeholder = { Text("Clear, light clouds, windy…") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = temperatureText,
                    onValueChange = { temperatureText = it },
                    label = { Text("Temperature °C") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cloudCoverText,
                    onValueChange = { cloudCoverText = it },
                    label = { Text("Cloud cover %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Divider()

            // ----------------------------
            // ISS optional coordinates
            // ----------------------------
            Text("ISS coordinates (optional)", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = issLatText,
                    onValueChange = { issLatText = it },
                    label = { Text("ISS Lat") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = issLonText,
                    onValueChange = { issLonText = it },
                    label = { Text("ISS Lon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Divider()

            // ----------------------------
            // Notes field
            // ----------------------------
            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    if (notesError != null && it.isNotBlank()) notesError = null
                },
                label = { Text("Notes") },
                placeholder = { Text("What did you see? How did the sky look?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .focusRequester(notesFocus),
                maxLines = 6,
                isError = notesError != null,
                supportingText = {
                    notesError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            )

            // ----------------------------
            // PHOTO PICKER SECTION
            // ----------------------------
            var showFullImage by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Photo (optional)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Attach a photo from your gallery (e.g., sky or gear setup).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        // Thumbnail preview
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = "Photo thumbnail",
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { showFullImage = true },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "No image",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Photo action buttons
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.End
                        ) {

                            // ---- CHOOSE PHOTO BUTTON ----
                            OutlinedButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.widthIn(min = 130.dp) // <--- prevents wrapping on phones
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Photo,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Choose Photo",
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            // ---- REMOVE PHOTO ----
                            if (photoUrl.isNotBlank()) {
                                Text(
                                    text = "Remove Photo",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .clickable { photoUrl = "" }
                                        .padding(end = 8.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                    }
                }
            }

            // Full-screen image viewer
            if (showFullImage && photoUrl.isNotBlank()) {
                Dialog(onDismissRequest = { showFullImage = false }) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showFullImage = false }
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Full screen photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        IconButton(
                            onClick = { showFullImage = false },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close image",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
