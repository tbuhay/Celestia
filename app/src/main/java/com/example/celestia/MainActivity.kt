package com.example.celestia

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.celestia.ui.screens.AccountScreen
import com.example.celestia.ui.screens.AsteroidTrackingScreen
import com.example.celestia.ui.screens.LunarPhaseScreen
import com.example.celestia.ui.screens.HomeScreen
import com.example.celestia.ui.screens.IssLocationScreen
import com.example.celestia.ui.screens.KpIndexScreen
import com.example.celestia.ui.screens.LoginScreen
import com.example.celestia.ui.screens.NotificationPreferencesScreen
import com.example.celestia.ui.screens.ObservationEditorScreen
import com.example.celestia.ui.screens.ObservationHistoryScreen
import com.example.celestia.ui.screens.RegisterScreen
import com.example.celestia.ui.screens.SettingsScreen
import com.example.celestia.ui.theme.CelestiaTheme
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.google.firebase.FirebaseApp

/**
 * Main entry point for Celestia.
 *
 * Responsibilities:
 * - Initializes Firebase
 * - Applies global theming (dark/light mode + text scaling)
 * - Sets up the navigation graph and destinations
 * - Routes users to Login or Home depending on authentication state
 * - Handles deep-link style navigation from notifications via
 *   `intent.getStringExtra("navigate_to")`
 *
 * This activity hosts all Compose UI screens.
 */
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If launched from a notification, the target route is included here.
        val navigateTo = intent.getStringExtra("navigate_to")

        // Required once for Firebase Auth + any other Firebase services.
        FirebaseApp.initializeApp(this)

        // Use full edge-to-edge layout for immersive UI.
        enableEdgeToEdge()

        setContent {

            // ---------------------------------------------------------
            // THEME + ACCESSIBILITY (text scaling)
            // ---------------------------------------------------------
            val settingsVM: SettingsViewModel = viewModel()
            val darkModeEnabled = settingsVM.darkModeEnabled.observeAsState(initial = true).value
            val textSize = settingsVM.textSize.observeAsState(initial = 1).value

            CelestiaTheme(
                darkTheme = darkModeEnabled,
                textSize = textSize
            ) {

                // ---------------------------------------------------------
                // NAVIGATION SETUP
                // ---------------------------------------------------------
                val navController = rememberNavController()
                val celestiaVM: CelestiaViewModel = viewModel()

                // Determine start screen based on whether user is signed in.
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val startDestination = if (firebaseUser != null) "home" else "login"

                // ---------------------------------------------------------
                // NOTIFICATION NAVIGATION HANDLER
                //
                // If a user taps a system notification (e.g., "Kp Alert"),
                // Celestia opens directly to the correct screen.
                // ---------------------------------------------------------
                if (navigateTo != null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(navigateTo) {
                            popUpTo(startDestination) { inclusive = false }
                        }
                    }
                }

                // ---------------------------------------------------------
                // NAVIGATION GRAPH
                // ---------------------------------------------------------
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // Authentication
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // Dashboard & Screens
                    composable("home") { HomeScreen(navController, celestiaVM) }
                    composable("kp_index") { KpIndexScreen(navController, celestiaVM) }
                    composable("iss_location") { IssLocationScreen(navController, celestiaVM) }
                    composable("asteroid_tracking") { AsteroidTrackingScreen(navController) }
                    composable("lunar_phase") { LunarPhaseScreen(navController) }

                    // Settings
                    composable("settings") { SettingsScreen(navController) }
                    composable("notification_preferences") {
                        NotificationPreferencesScreen(navController)
                    }
                    composable("account") { AccountScreen(navController) }
                    composable("observation_history") {
                        ObservationHistoryScreen(navController, celestiaVM)
                    }

                    composable("observation_new") {
                        ObservationEditorScreen(
                            navController = navController,
                            vm = celestiaVM,
                            entryId = null
                        )
                    }

                    composable(
                        route = "observation_detail/{id}",
                        arguments = listOf(
                            navArgument("id") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getInt("id")
                        ObservationEditorScreen(
                            navController = navController,
                            vm = celestiaVM,
                            entryId = id
                        )
                    }
                }
            }
        }
    }
}
