package com.example.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.celestia.ui.screens.AsteroidTrackingScreen
import com.example.celestia.ui.screens.LunarPhaseScreen
import com.example.celestia.ui.screens.HomeScreen
import com.example.celestia.ui.screens.IssLocationScreen
import com.example.celestia.ui.screens.KpIndexScreen
import com.example.celestia.ui.screens.LoginScreen
import com.example.celestia.ui.screens.RegisterScreen
import com.example.celestia.ui.screens.SettingsScreen
import com.example.celestia.ui.theme.CelestiaTheme
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {

            // ---------------------------------------------------------
            // THEME from DataStore (SettingsViewModel)
            // ---------------------------------------------------------
            val settingsVM: SettingsViewModel = viewModel()
            val darkModeEnabled = settingsVM.darkModeEnabled.observeAsState(initial = true).value

            CelestiaTheme(darkTheme = darkModeEnabled) {

                val navController = rememberNavController()
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val startDestination = if (firebaseUser != null) "home" else "login"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {

                    // ---------------------- AUTH ----------------------
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // ---------------------- MAIN SCREENS ----------------------
                    composable("home") { HomeScreen(navController, viewModel()) }
                    composable("kp_index") { KpIndexScreen(navController, viewModel()) }
                    composable("iss_location") { IssLocationScreen(navController, viewModel()) }
                    composable("asteroid_tracking") { AsteroidTrackingScreen(navController) }
                    composable("lunar_phase") { LunarPhaseScreen(navController) }

                    // ---------------------- SETTINGS ----------------------
                    composable("settings") { SettingsScreen(navController) }
                }
            }
        }
    }
}