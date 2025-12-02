package com.example.celestia.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    authVM: AuthViewModel = viewModel()
) {
    val userName by authVM.userName.observeAsState()
    val user = FirebaseAuth.getInstance().currentUser

    var name by remember { mutableStateOf(userName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // DISPLAY NAME
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display Name") },
                singleLine = true
            )
            Button(onClick = {
                authVM.updateDisplayName(name) { success, error ->
                    message = error ?: "Display name updated"
                }
            }) {
                Text("Update Name")
            }

            // EMAIL
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true
            )
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password (required)") },
                singleLine = true
            )
            Button(onClick = {
                authVM.updateEmail(email, currentPassword) { success, error ->
                    message = error ?: "Email updated"
                }
            }) {
                Text("Update Email")
            }

            // PASSWORD
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                singleLine = true
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true
            )
            Button(onClick = {
                authVM.updatePassword(currentPassword, newPassword) { success, error ->
                    message = error ?: "Password updated"
                }
            }) {
                Text("Update Password")
            }
            Button(onClick = {
                FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
            }) {
                Text("Send Verification Email")
            }

            FirebaseAuth.getInstance().currentUser?.reload()

            Button(onClick = {
                FirebaseAuth.getInstance().currentUser?.reload()
                Log.d("AUTH_CHECK", "Verified refreshed = ${FirebaseAuth.getInstance().currentUser?.isEmailVerified}")
            }) {
                Text("Refresh Verification")
            }

            message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
        }
        Log.d("AUTH_CHECK", "Verified = ${FirebaseAuth.getInstance().currentUser?.isEmailVerified}")
        Log.d("AUTH_CHECK", "Providers = ${
            FirebaseAuth.getInstance().currentUser?.providerData?.joinToString { it.providerId }
        }")
    }
}


