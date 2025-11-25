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
import com.example.celestia.ui.screens.AsteroidTrackingScreen
import com.example.celestia.ui.screens.LunarPhaseScreen
import com.example.celestia.ui.screens.HomeScreen
import com.example.celestia.ui.screens.IssLocationScreen
import com.example.celestia.ui.screens.KpIndexScreen
import com.example.celestia.ui.screens.LoginScreen
import com.example.celestia.ui.screens.NotificationPreferencesScreen
import com.example.celestia.ui.screens.RegisterScreen
import com.example.celestia.ui.screens.SettingsScreen
import com.example.celestia.ui.theme.CelestiaTheme
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val navigateTo = intent.getStringExtra("navigate_to")
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {

            val settingsVM: SettingsViewModel = viewModel()
            val darkModeEnabled = settingsVM.darkModeEnabled.observeAsState(initial = true).value

            CelestiaTheme(darkTheme = darkModeEnabled) {

                val navController = rememberNavController()
                val celestiaVM: CelestiaViewModel = viewModel()

                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val startDestination = if (firebaseUser != null) "home" else "login"

                if (navigateTo != null) {
                    LaunchedEffect(Unit) {
                        navController.navigate(navigateTo) {
                            popUpTo(startDestination) { inclusive = false }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    composable("home") { HomeScreen(navController, celestiaVM) }
                    composable("kp_index") { KpIndexScreen(navController, celestiaVM) }
                    composable("iss_location") { IssLocationScreen(navController, celestiaVM) }
                    composable("asteroid_tracking") { AsteroidTrackingScreen(navController) }
                    composable("lunar_phase") { LunarPhaseScreen(navController) }

                    composable("settings") { SettingsScreen(navController) }
                    composable("notification_preferences") {
                        NotificationPreferencesScreen(navController)
                    }
                }
            }
        }
    }
}