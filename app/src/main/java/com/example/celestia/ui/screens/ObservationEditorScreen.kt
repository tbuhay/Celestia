package com.example.celestia.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestia.data.model.ObservationEntry
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationEditorScreen(
    navController: NavController,
    vm: CelestiaViewModel,
    entryId: Int?
) {
    // ---------------------------------------------------------
    // Determine mode: NEW or EDIT
    // ---------------------------------------------------------
    var existingEntry by remember { mutableStateOf<ObservationEntry?>(null) }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            existingEntry = vm.getJournalEntry(entryId)
        }
    }

    val isEditMode = entryId != null

    // ---------------------------------------------------------
    // Initialize field states
    // ---------------------------------------------------------
    var title by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var kpIndex by remember { mutableStateOf("") }
    var weatherSummary by remember { mutableStateOf("") }
    var temperatureC by remember { mutableStateOf("") }
    var cloudCover by remember { mutableStateOf("") }
    var issLat by remember { mutableStateOf("") }
    var issLon by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }
    var notesError by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Populate fields when editing
    LaunchedEffect(existingEntry) {
        existingEntry?.let { e ->
            title = e.observationTitle
            locationName = e.locationName ?: ""
            latitude = e.latitude?.toString() ?: ""
            longitude = e.longitude?.toString() ?: ""
            kpIndex = e.kpIndex?.toString() ?: ""
            weatherSummary = e.weatherSummary ?: ""
            temperatureC = e.temperatureC?.toString() ?: ""
            cloudCover = e.cloudCoverPercent?.toString() ?: ""
            issLat = e.issLat?.toString() ?: ""
            issLon = e.issLon?.toString() ?: ""
            notes = e.notes
            photoUri = e.photoUrl?.let { Uri.parse(it) }
        }
    }

    // Timestamp to show (existing or new)
    val timestampToDisplay = existingEntry?.timestamp ?: remember {
        System.currentTimeMillis()
    }
    val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        .format(Date(timestampToDisplay))

    // Photo picker
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        photoUri = uri
    }

    // Delete confirmation dialog
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog && existingEntry != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Observation") },
            text = { Text("Are you sure you want to delete this observation? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteJournalEntry(existingEntry!!)
                        showDeleteDialog = false
                        navController.popBackStack()
                    }
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---------------------------------------------------------
    // MAIN UI
    // ---------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEditMode) "Edit Observation" else "New Observation")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (
                                validateAndSave(
                                    title = title,
                                    notes = notes,
                                    setTitleError = { titleError = it },
                                    setNotesError = { notesError = it },
                                    vm = vm,
                                    existingEntry = existingEntry,
                                    locationName = locationName,
                                    latitude = latitude,
                                    longitude = longitude,
                                    kpIndex = kpIndex,
                                    weatherSummary = weatherSummary,
                                    temperatureC = temperatureC,
                                    cloudCover = cloudCover,
                                    issLat = issLat,
                                    issLon = issLon,
                                    photoUri = photoUri,
                                    timestamp = timestampToDisplay
                                )
                            ) {
                                navController.popBackStack()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }

                    if (isEditMode && existingEntry != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // ----- Title (required) -----
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = it.isBlank()
                },
                label = { Text("Title (required)") },
                isError = titleError,
                modifier = Modifier.fillMaxWidth()
            )
            if (titleError) {
                Text(
                    text = "Title cannot be empty.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SectionDivider()

            // ----- Observation Time (read-only) -----
            Text(
                text = "Observation Time",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium
            )

            SectionDivider()

            // ----- Location -----
            Text(
                text = "Location",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location Name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text("Latitude (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text("Longitude (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionDivider()

            // ----- Sky Conditions -----
            Text(
                text = "Sky Conditions",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = weatherSummary,
                onValueChange = { weatherSummary = it },
                label = { Text("Weather Summary (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = temperatureC,
                    onValueChange = { temperatureC = it },
                    label = { Text("Temp °C (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = cloudCover,
                    onValueChange = { cloudCover = it },
                    label = { Text("Cloud Cover % (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionDivider()

            // ----- Kp Index -----
            Text(
                text = "Kp Index",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = kpIndex,
                onValueChange = { kpIndex = it },
                label = { Text("Kp Index (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            SectionDivider()

            // ----- ISS Coordinates -----
            Text(
                text = "ISS Coordinates",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = issLat,
                    onValueChange = { issLat = it },
                    label = { Text("ISS Lat (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = issLon,
                    onValueChange = { issLon = it },
                    label = { Text("ISS Lon (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            SectionDivider()

            // ----- Notes (required) -----
            Text(
                text = "Notes",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = {
                    notes = it
                    notesError = it.isBlank()
                },
                label = { Text("Notes (required)") },
                isError = notesError,
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            if (notesError) {
                Text(
                    text = "Notes cannot be empty.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            SectionDivider()

            // ----- Photo Picker -----
            Text(
                text = "Photo",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(8.dp))

            ElevatedCard(
                onClick = { photoPicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Thumbnail/icon area
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Photo thumbnail",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(40.dp)
                    )

                    Column {
                        Text(
                            text = if (photoUri == null) "Choose Photo" else "Change Photo",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Tap to select from gallery",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (photoUri != null) {
                TextButton(
                    onClick = { photoUri = null },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "Remove photo",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionDivider() {
    Divider(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .padding(horizontal = 8.dp)
    )
}

private fun validateAndSave(
    title: String,
    notes: String,
    setTitleError: (Boolean) -> Unit,
    setNotesError: (Boolean) -> Unit,
    vm: CelestiaViewModel,
    existingEntry: ObservationEntry?,
    locationName: String,
    latitude: String,
    longitude: String,
    kpIndex: String,
    weatherSummary: String,
    temperatureC: String,
    cloudCover: String,
    issLat: String,
    issLon: String,
    photoUri: Uri?,
    timestamp: Long
): Boolean {

    var valid = true

    if (title.isBlank()) {
        setTitleError(true)
        valid = false
    } else {
        setTitleError(false)
    }

    if (notes.isBlank()) {
        setNotesError(true)
        valid = false
    } else {
        setNotesError(false)
    }

    if (!valid) return false

    val newEntry = ObservationEntry(
        id = existingEntry?.id ?: 0,
        observationTitle = title,
        timestamp = timestamp,
        locationName = locationName.ifBlank { null },
        latitude = latitude.toDoubleOrNull(),
        longitude = longitude.toDoubleOrNull(),
        kpIndex = kpIndex.toDoubleOrNull(),
        weatherSummary = weatherSummary.ifBlank { null },
        temperatureC = temperatureC.toDoubleOrNull(),
        cloudCoverPercent = cloudCover.toIntOrNull(),
        issLat = issLat.toDoubleOrNull(),
        issLon = issLon.toDoubleOrNull(),
        notes = notes,
        photoUrl = photoUri?.toString()
    )

    vm.saveJournalEntry(newEntry)
    return true
}