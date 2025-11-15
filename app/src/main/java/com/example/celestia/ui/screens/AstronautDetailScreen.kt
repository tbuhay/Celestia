package com.example.celestia.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.celestia.ui.viewmodel.CelestiaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AstronautDetailScreen(
    navController: NavController,
    vm: CelestiaViewModel,
    rawName: String
) {
    // Observe the Wiki response
    val profile by vm.selectedAstronaut.observeAsState()

    // Load once per name
    LaunchedEffect(rawName) {
        vm.clearAstronaut()
        vm.loadAstronautDetails(rawName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = rawName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        if (profile == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Name: ${profile!!.title}")
                Text("Description: ${profile!!.description}")
                Text("Bio: ${profile!!.extract}")
            }
        }
    }
}

