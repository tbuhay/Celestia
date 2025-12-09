package com.example.celestia.ui.screens

import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Displays a themed toast message positioned at the top of the screen.
 *
 * This uses a custom toast view so the toast can appear at the top
 * on modern Android versions (default toasts ignore gravity).
 *
 * @param context Used to display the toast.
 * @param message Text to show.
 */
fun showTopToast(
    context: android.content.Context,
    message: String,
    backgroundColor: Int,
    textColor: Int
) {
    val layout = android.widget.TextView(context).apply {
        text = message
        setBackgroundColor(backgroundColor)
        setTextColor(textColor)
        setPadding(32, 16, 32, 16)
        textSize = 16f
    }

    Toast(context).apply {
        duration = Toast.LENGTH_SHORT
        view = layout
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 120)
        show()
    }
}

/**
 * Account settings screen allowing the user to:
 * - View account metadata (creation date, last active)
 * - Update display name
 * - Update password
 * - See responsive toast notifications
 *
 * @param navController Used for navigating back.
 * @param authVM Authentication logic provider.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    navController: NavController,
    authVM: AuthViewModel = viewModel()
) {
    val user = FirebaseAuth.getInstance().currentUser
    val userName by authVM.userName.observeAsState()

    var displayName by remember { mutableStateOf(userName ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val toastBg = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val toastText = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()

    /**
     * Converts Firebase timestamps into a readable string.
     */
    fun formatFirebaseTimestamp(timestamp: Long?): String {
        if (timestamp == null) return "Unknown"
        val date = Date(timestamp)
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ------------------------------------------------------
            // PROFILE HEADER CARD
            // ------------------------------------------------------
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    // LEFT: Icon + Name
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // RIGHT: Metadata
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.widthIn(min = 120.dp)
                    ) {
                        Text(
                            "Account Created",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatFirebaseTimestamp(user?.metadata?.creationTimestamp),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            "Last Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            formatFirebaseTimestamp(user?.metadata?.lastSignInTimestamp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ------------------------------------------------------
            // PROFILE SECTION
            // ------------------------------------------------------
            Text(
                "Profile",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            authVM.updateDisplayName(displayName) { success, error ->
                                if (success) {
                                    showTopToast(
                                        context,
                                        "Display name updated!",
                                        toastBg,
                                        toastText
                                    )
                                } else {
                                    showTopToast(context, error ?: "Error updating name", toastBg, toastText)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Name")
                    }
                }
            }

            // ------------------------------------------------------
            // SECURITY SECTION
            // ------------------------------------------------------
            Text(
                "Security",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            authVM.updatePassword(currentPassword, newPassword) { success, error ->
                                if (success) {
                                    showTopToast(
                                        context,
                                        "Password has been updated!",
                                        toastBg,
                                        toastText
                                    )
                                    currentPassword = ""
                                    newPassword = ""
                                } else {
                                    showTopToast(context, error ?: "Error updating password", toastBg, toastText)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Password")
                    }
                }
            }
        }
    }
}
