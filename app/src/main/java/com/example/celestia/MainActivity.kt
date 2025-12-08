package com.example.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.celestia.ui.screens.AccountScreen
import com.example.celestia.ui.screens.AsteroidTrackingScreen
import com.example.celestia.ui.screens.LunarPhaseScreen
import com.example.celestia.ui.screens.HomeScreen
import com.example.celestia.ui.screens.IssLocationScreen
import com.example.celestia.ui.screens.KpIndexScreen
import com.example.celestia.ui.screens.LoginScreen
import com.example.celestia.ui.screens.NotificationPreferencesScreen
import com.example.celestia.ui.screens.ObservationHistoryScreen
import com.example.celestia.ui.screens.RegisterScreen
import com.example.celestia.ui.screens.SettingsScreen
import com.example.celestia.ui.theme.CelestiaTheme
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.google.firebase.FirebaseApp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.celestia.ui.screens.ObservationEditorScreen

/**
 * **MainActivity — Host for the entire Celestia Compose application.**
 *
 * This Activity is responsible for:
 *
 * ### 🏁 App Initialization
 * - Initializing Firebase (Auth + additional Firebase services if added later)
 * - Preparing edge-to-edge layout for modern UI design
 * - Applying global theming (dark mode + font scaling)
 *
 * ### 🧭 Navigation Graph
 * - Defines all top-level screens in the app
 * - Determines start destination based on authentication state
 * - Handles navigation triggered by system notifications
 *
 * ### 📱 UI Hosting
 * This activity contains **no UI of its own**.
 * Instead, it sets a Jetpack Compose `NavHost` that renders the entire application.
 *
 * ### 🔔 Notification Routing
 * If Celestia is opened via a system notification (e.g., “Kp Alert”), a route
 * is passed through the launch intent using:
 *
 * ```
 * intent.getStringExtra("navigate_to")
 * ```
 *
 * MainActivity intercepts this and immediately navigates to the correct screen,
 * even if the app was previously closed.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Deep-link destination triggered from notifications (optional)
        val navigateTo = intent.getStringExtra("navigate_to")

        // Initialize Firebase services (Auth required for Login/Register screens)
        FirebaseApp.initializeApp(this)

        // Prepare edge-to-edge layout for immersive UI
        enableEdgeToEdge()

        setContent {

            // -----------------------------------------------------------------
            // THEME + ACCESSIBILITY CONFIGURATION
            // -----------------------------------------------------------------
            val settingsVM: SettingsViewModel = viewModel()
            val darkModeEnabled = settingsVM.darkModeEnabled.observeAsState(initial = true).value
            val textSize = settingsVM.textSize.observeAsState(initial = 1).value

            CelestiaTheme(
                darkTheme = darkModeEnabled,
                textSize = textSize
            ) {

                // -----------------------------------------------------------------
                // NAVIGATION CONTROLLER + VIEWMODELS
                // -----------------------------------------------------------------
                val navController = rememberNavController()
                val celestiaVM: CelestiaViewModel = viewModel()

                // Determine start destination based on auth state
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val startDestination = if (firebaseUser != null) "home" else "login"

                // -----------------------------------------------------------------
                // NAVIGATION FROM NOTIFICATION TAP
                // -----------------------------------------------------------------
                if (navigateTo != null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(navigateTo) {
                            popUpTo(startDestination) { inclusive = false }
                        }
                    }
                }

                // -----------------------------------------------------------------
                // NAVIGATION GRAPH — DEFINES EVERY SCREEN IN CELESTIA
                // -----------------------------------------------------------------
                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // --- Authentication ---
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // --- Dashboard + Feature Screens ---
                    composable("home") { HomeScreen(navController, celestiaVM) }
                    composable("kp_index") { KpIndexScreen(navController, celestiaVM) }
                    composable("iss_location") { IssLocationScreen(navController, celestiaVM) }
                    composable("asteroid_tracking") { AsteroidTrackingScreen(navController) }
                    composable("lunar_phase") { LunarPhaseScreen(navController) }

                    // --- Settings & Preferences ---
                    composable("settings") { SettingsScreen(navController) }
                    composable("notification_preferences") {
                        NotificationPreferencesScreen(navController)
                    }
                    composable("account") { AccountScreen(navController) }

                    // --- Observation Journal ---
                    composable("observation_history") {
                        ObservationHistoryScreen(
                            navController = navController,
                            vm = celestiaVM
                        )
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
                        val id = backStackEntry.arguments?.getInt("id") ?: 0
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
