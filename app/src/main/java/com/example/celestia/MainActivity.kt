package com.example.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            CelestiaTheme {
                val navController = rememberNavController()
                val isUserLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null

                NavHost(
                    navController = navController,
                    startDestination = if (isUserLoggedIn) "home" else "login"
                ) {
                    // Auth Screens
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // Main App Screens
                    composable("home") { HomeScreen(navController, viewModel()) }
                    composable("kp_index") { KpIndexScreen(navController, viewModel()) }
                    composable("iss_location") { IssLocationScreen(navController, viewModel()) }
                    composable("asteroid_tracking") { AsteroidTrackingScreen(navController) }
                    composable("lunar_phase") { LunarPhaseScreen(navController) }

                    // Settings Screen(s)
                    composable("settings") { SettingsScreen(navController) }
                }
            }
        }
    }
}