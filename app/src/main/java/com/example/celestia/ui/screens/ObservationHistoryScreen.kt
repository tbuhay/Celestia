package com.example.celestia.ui.screens

import android.text.format.DateFormat
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.ui.text.input.KeyboardType
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.example.celestia.utils.FormatUtils
import java.util.Locale

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
                    Text(
                        "Observation Journal",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("observation_new") }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Observation"
                        )
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

@Composable
private fun ObservationHistoryItem(
    entry: ObservationEntry,
    use24h: Boolean,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PHOTO THUMBNAIL (if photo exists)
            if (entry.photoUrl != null) {
                AsyncImage(
                    model = entry.photoUrl,
                    contentDescription = "Observation photo",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f)) {

                // Title + timestamp line
                val tsText = formatTimestamp(entry.timestamp, use24h)

                if (entry.observationTitle.isNotBlank()) {
                    Text(
                        text = "${entry.observationTitle} • $tsText",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = tsText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                // Location name
                Text(
                    text = entry.locationName ?: "Unknown location",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.height(4.dp))

                // Quick stats (Kp + weather)
                val kpText = entry.kpIndex?.let { "Kp ${"%.1f".format(it)}" }
                val weatherText = entry.weatherSummary

                Text(
                    text = listOfNotNull(kpText, weatherText)
                        .joinToString(" • ")
                        .ifBlank { "No sky data captured" },
                    style = MaterialTheme.typography.bodySmall
                )

                // Notes preview
                if (entry.notes.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.notes,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


private fun formatTimestamp(timestamp: Long, use24h: Boolean): String {
    return FormatUtils.formatUpdatedTimestamp(timestamp.toString(), use24h)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationEditorScreen(
    navController: NavController,
    vm: CelestiaViewModel = viewModel(),
    entryId: Int?
) {
    // ---------------------------------------------------------
    // STATE: loading existing entry (for edit mode)
    // ---------------------------------------------------------
    var isLoading by remember { mutableStateOf(entryId != null) }
    var existingEntry by remember { mutableStateOf<ObservationEntry?>(null) }

    // Settings (12h/24h)
    val settingsVM: SettingsViewModel = viewModel()
    val use24h by settingsVM.timeFormat24h.observeAsState(true)

    // Use existing timestamp or new timestamp for creation
    val baseTimestamp = remember(existingEntry, entryId) {
        existingEntry?.timestamp ?: System.currentTimeMillis()
    }

    val formattedDate = remember(baseTimestamp, use24h) {
        FormatUtils.formatUpdatedTimestamp(baseTimestamp.toString(), use24h)
    }

    val autoFields by vm.autoObservationFields.observeAsState()

    LaunchedEffect(entryId) {
        if (entryId != null) {
            // Editing existing entry
            existingEntry = vm.getJournalEntry(entryId)
        } else {
            // New entry — kick off auto-fill
            vm.refreshAutoObservationFieldsForNewEntry()
        }
        isLoading = false
    }

    // While loading existing entry
    if (isLoading) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Observation") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // ---------------------------------------------------------
    // MODE: is this a new or existing entry?
    // ---------------------------------------------------------
    val isEditing = existingEntry != null

    // ---------------------------------------------------------
    // FORM STATE (initialized from existing entry if editing)
    // ---------------------------------------------------------
    val initial = existingEntry
    var observationTitle by remember(initial) { mutableStateOf(initial?.observationTitle ?: "") }

    var locationName by remember(initial) { mutableStateOf(initial?.locationName ?: "") }
    var latitudeText by remember(initial) {
        mutableStateOf(FormatUtils.formatLatitudePlain(initial?.latitude))
    }
    var longitudeText by remember(initial) {
        mutableStateOf(FormatUtils.formatLongitudePlain(initial?.longitude))
    }

    var kpText by remember(initial) { mutableStateOf(initial?.kpIndex?.toString() ?: "") }
    var issLatText by remember(initial) {
        mutableStateOf(FormatUtils.formatLatitudePlain(initial?.issLat))
    }
    var issLonText by remember(initial) {
        mutableStateOf(FormatUtils.formatLongitudePlain(initial?.issLon))
    }

    var weatherSummary by remember(initial) { mutableStateOf(initial?.weatherSummary ?: "") }
    var temperatureText by remember(initial) { mutableStateOf(initial?.temperatureC?.toString() ?: "") }
    var cloudCoverText by remember(initial) { mutableStateOf(initial?.cloudCoverPercent?.toString() ?: "") }

    var notes by remember(initial) { mutableStateOf(initial?.notes ?: "") }
    var notesError by remember { mutableStateOf<String?>(null) }

    var photoUrl by remember(initial) { mutableStateOf(initial?.photoUrl ?: "") }

    // Photo picker for gallery image (URI → string)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUrl = uri.toString()
        }
    }

    // Delete dialog state
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(autoFields) {
        if (!isEditing && autoFields != null) {
            autoFields?.let { auto ->

                if (latitudeText.isBlank() && auto.latitude != null) {
                    latitudeText = auto.latitude.toString()
                }
                if (longitudeText.isBlank() && auto.longitude != null) {
                    longitudeText = auto.longitude.toString()
                }
                if (kpText.isBlank() && auto.kpIndex != null) {
                    kpText = auto.kpIndex.toString()
                }
                if (issLatText.isBlank() && auto.issLat != null) {
                    issLatText = auto.issLat.toString()
                }
                if (issLonText.isBlank() && auto.issLon != null) {
                    issLonText = auto.issLon.toString()
                }
                if (temperatureText.isBlank() && auto.temperatureC != null) {
                    temperatureText = auto.temperatureC.toString()
                }
                if (cloudCoverText.isBlank() && auto.cloudCoverPercent != null) {
                    cloudCoverText = auto.cloudCoverPercent.toString()
                }
                if (weatherSummary.isBlank() && auto.weatherSummary != null) {
                    weatherSummary = auto.weatherSummary
                }
            }
        }
    }

    // ---------------------------------------------------------
    // SAVE HANDLER
    // ---------------------------------------------------------
    fun handleSave() {
        if (notes.isBlank()) {
            notesError = "Notes cannot be empty"
            return
        } else {
            notesError = null
        }

        val latitude = latitudeText.toDoubleOrNull()
        val longitude = longitudeText.toDoubleOrNull()
        val kpIndex = kpText.toDoubleOrNull()
        val issLat = issLatText.toDoubleOrNull()
        val issLon = issLonText.toDoubleOrNull()
        val tempC = temperatureText.toDoubleOrNull()
        val cloudCover = cloudCoverText.toIntOrNull()

        val cleanedLocation = locationName.trim().ifEmpty { null }
        val cleanedWeather = weatherSummary.trim().ifEmpty { null }
        val cleanedPhoto = photoUrl.takeIf { it.isNotBlank() }

        val normalizedLatitude = latitude?.let { String.format(Locale.US, "%.4f", it).toDouble() }
        val normalizedLongitude = longitude?.let { String.format(Locale.US, "%.4f", it).toDouble() }
        val normalizedIssLat = issLat?.let { String.format(Locale.US, "%.4f", it).toDouble() }
        val normalizedIssLon = issLon?.let { String.format(Locale.US, "%.4f", it).toDouble() }

        val entryToSave: ObservationEntry =
            if (existingEntry != null) {
                existingEntry!!.copy(
                    observationTitle = observationTitle.trim(),
                    locationName = cleanedLocation,
                    latitude = normalizedLatitude,
                    longitude = normalizedLongitude,
                    kpIndex = kpIndex,
                    issLat = normalizedIssLat,
                    issLon = normalizedIssLon,
                    weatherSummary = cleanedWeather,
                    temperatureC = tempC,
                    cloudCoverPercent = cloudCover,
                    notes = notes.trim(),
                    photoUrl = cleanedPhoto
                )
            } else {
                ObservationEntry(
                    id = 0,
                    timestamp = baseTimestamp,
                    observationTitle = observationTitle.trim(),
                    locationName = cleanedLocation,
                    latitude = normalizedLatitude,
                    longitude = normalizedLongitude,
                    kpIndex = kpIndex,
                    issLat = normalizedIssLat,
                    issLon = normalizedIssLon,
                    weatherSummary = cleanedWeather,
                    temperatureC = tempC,
                    cloudCoverPercent = cloudCover,
                    notes = notes.trim(),
                    photoUrl = cleanedPhoto
                )
            }


        vm.saveJournalEntry(entryToSave)
        navController.popBackStack()
    }

    // ---------------------------------------------------------
    // UI
    // ---------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Observation" else "New Observation",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { handleSave() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }

                    if (isEditing && existingEntry != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete observation"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TITLE
            OutlinedTextField(
                value = observationTitle,
                onValueChange = { observationTitle = it },
                label = { Text("Title (optional)") },
                placeholder = { Text("Short title to identify this observation") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))


            // Timestamp display
            Text(
                text = "Observation time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium
            )

            Divider(Modifier.padding(vertical = 8.dp))

            // LOCATION
            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location") },
                placeholder = { Text("City, Province/State, Country") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = latitudeText,
                    onValueChange = { latitudeText = it },
                    label = { Text("Latitude (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitudeText,
                    onValueChange = { longitudeText = it },
                    label = { Text("Longitude (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // SKY CONDITIONS
            Text(
                text = "Sky conditions",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = kpText,
                onValueChange = { kpText = it },
                label = { Text("Kp Index (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = weatherSummary,
                onValueChange = { weatherSummary = it },
                label = { Text("Weather summary (optional)") },
                placeholder = { Text("Clear, light clouds, windy…") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = temperatureText,
                    onValueChange = { temperatureText = it },
                    label = { Text("Temperature °C (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cloudCoverText,
                    onValueChange = { cloudCoverText = it },
                    label = { Text("Cloud cover % (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // ISS COORDS
            Text(
                text = "ISS coordinates (optional)",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = issLatText,
                    onValueChange = { issLatText = it },
                    label = { Text("ISS Lat") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = issLonText,
                    onValueChange = { issLonText = it },
                    label = { Text("ISS Lon") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(Modifier.padding(vertical = 8.dp))

            // NOTES (required)
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
                    .heightIn(min = 120.dp),
                maxLines = 6,
                isError = notesError != null,
                supportingText = {
                    notesError?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )

            // PHOTO PICKER
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { photoPickerLauncher.launch("image/*") }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Photo,
                                contentDescription = "Pick photo",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Choose Photo")
                        }

                        if (photoUrl.isNotBlank()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TextButton(
                                    onClick = { photoUrl = "" }
                                ) {
                                    Text("Remove Photo")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Bottom Save button (accessible)
            Button(
                onClick = { handleSave() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Save Observation")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ---------------------------------------------------------
    // DELETE CONFIRMATION DIALOG
    // ---------------------------------------------------------
    if (showDeleteDialog && isEditing && existingEntry != null) {
        DeleteObservationDialog(
            onConfirm = {
                vm.deleteJournalEntry(existingEntry!!)
                showDeleteDialog = false
                navController.popBackStack() // back to history
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun DeleteObservationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Delete observation?")
        },
        text = {
            Text("This will permanently delete this observation from your journal.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
