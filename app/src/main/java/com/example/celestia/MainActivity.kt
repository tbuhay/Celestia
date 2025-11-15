package com.example.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.celestia.ui.screens.AsteroidTrackingScreen
import com.example.celestia.ui.screens.AstronautDetailScreen
import com.example.celestia.ui.screens.LunarPhaseScreen
import com.example.celestia.ui.screens.HomeScreen
import com.example.celestia.ui.screens.IssLocationScreen
import com.example.celestia.ui.screens.KpIndexScreen
import com.example.celestia.ui.screens.LoginScreen
import com.example.celestia.ui.screens.RegisterScreen
import com.example.celestia.ui.screens.SettingsScreen
import com.example.celestia.ui.theme.CelestiaTheme
import com.example.celestia.ui.viewmodel.CelestiaViewModel
import com.example.celestia.ui.viewmodel.SettingsViewModel
import com.google.firebase.FirebaseApp
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        setContent {

            // Settings ViewModel (separate, fine)
            val settingsVM: SettingsViewModel = viewModel()
            val darkModeEnabled = settingsVM.darkModeEnabled.observeAsState(initial = true).value

            CelestiaTheme(darkTheme = darkModeEnabled) {

                val navController = rememberNavController()
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                val startDestination = if (firebaseUser != null) "home" else "login"

                // 🔥 Create CelestiaViewModel ONCE — shared across all screens
                val celestiaVM = viewModel<CelestiaViewModel>()

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {

                    // ---------------------- AUTH ----------------------
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // ---------------------- MAIN SCREENS ----------------------
                    composable("home") {
                        HomeScreen(navController, celestiaVM)
                    }
                    composable("kp_index") {
                        KpIndexScreen(navController, celestiaVM)
                    }
                    composable("iss_location") {
                        IssLocationScreen(navController, celestiaVM)
                    }
                    composable("asteroid_tracking") {
                        AsteroidTrackingScreen(navController)
                    }
                    composable("lunar_phase") {
                        LunarPhaseScreen(navController)
                    }

                    // ---------------------- ASTRONAUT DETAIL ----------------------
                    composable(
                        route = "astronautDetail/{name}",
                        arguments = listOf(navArgument("name") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val name = backStackEntry.arguments?.getString("name") ?: ""
                        AstronautDetailScreen(navController, celestiaVM, name)
                    }

                    // ---------------------- SETTINGS ----------------------
                    composable("settings") {
                        SettingsScreen(navController)
                    }
                }
            }
        }
    }
}