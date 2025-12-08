package com.example.celestia.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.celestia.data.model.ObservationEntry
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationHistoryScreen(
    navController: NavController,
    vm: CelestiaViewModel
) {
    val entries = vm.allJournalEntries.observeAsState(emptyList()).value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Observation Journal") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(
                        onClick = { navController.navigate("observation_new") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Observation")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(12.dp)
        ) {

            if (entries.isEmpty()) {
                // Empty state message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No observations yet.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(entries) { entry ->
                        ObservationHistoryCard(
                            entry = entry,
                            onClick = {
                                navController.navigate("observation_detail/${entry.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ObservationHistoryCard(
    entry: ObservationEntry,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
        .format(Date(entry.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // First line: Title • timestamp
            Text(
                text = "${entry.observationTitle.ifBlank { "Untitled" }} • $dateStr",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Second line: location
            Text(
                text = entry.locationName ?: "Unknown location",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Third line: quick stats
            val stats = when {
                entry.kpIndex != null || entry.weatherSummary != null ->
                    listOfNotNull(
                        entry.kpIndex?.let { "Kp $it" },
                        entry.weatherSummary
                    ).joinToString(" • ")

                else -> "No sky data captured"
            }

            Text(
                text = stats,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notes preview
            Text(
                text = entry.notes,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
