package com.example.celestia.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.components.CelestiaToast
import com.example.celestia.ui.theme.CelestiaOrange
import com.example.celestia.ui.theme.CelestiaPurple
import com.example.celestia.ui.theme.DarkSurface
import com.example.celestia.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.example.celestia.R


/**
 * Login screen for Celestia.
 *
 * Features:
 * - Email + password authentication
 * - Google Sign-In (OAuth) using Firebase
 * - GitHub Login via Firebase OAuth provider
 * - Input validation with toast feedback
 * - Automatic redirection to Home upon successful login
 * - Password visibility toggle
 *
 * This screen interacts with:
 * - [AuthViewModel] for authentication logic
 * - [NavController] for navigation to Home/Register screens
 *
 * When the user signs in successfully:
 * The navigation stack clears `"login"` so pressing Back does not return here.
 */
@Composable
fun LoginScreen(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Initialize the AuthService inside the ViewModel
    LaunchedEffect(Unit) { vm.init(context) }

    // ---------------------------------------------------------
    // GOOGLE SIGN-IN CONFIGURATION
    // ---------------------------------------------------------
    /**
     * GoogleSignInOptions + GoogleSignInClient are remembered so
     * they persist across recompositions.
     */
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    // Toast message container
    var toastMessage by remember { mutableStateOf<String?>(null) }

    /**
     * Launcher for Google OAuth flow.
     * Handles:
     * - Starting Google sign-in intent
     * - Reading returned ID token
     * - Passing token into ViewModel for Firebase login
     */
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { vm.signInWithGoogle(it) }
                ?: run { toastMessage = "Google sign-in failed: no token" }
        } catch (e: Exception) {
            toastMessage = "Google sign-in failed"
        }
    }

    // ---------------------------------------------------------
    // SCREEN STATE
    // ---------------------------------------------------------
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isAuthenticated by vm.isAuthenticated.observeAsState(false)
    val errorMessage by vm.errorMessage.observeAsState()

    /**
     * Navigate to home once authenticated.
     * Clears login screen from the back stack.
     */
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    /**
     * Display error messages from ViewModel as toast notifications.
     */
    LaunchedEffect(errorMessage) {
        errorMessage?.let { toastMessage = it; vm.clearError() }
    }

    // ---------------------------------------------------------
    // UI LAYOUT
    // ---------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        DarkSurface.copy(alpha = 0.9f),
                        CelestiaPurple.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        // Center card container
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            /**
             * Main login card
             * Contains:
             * - Title
             * - Email/password form
             * - Login button
             * - Navigation to registration
             * - Google + GitHub sign-in options
             */
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp)),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // ---------------------------------------------------------
                    // HEADER TEXT
                    // ---------------------------------------------------------
                    Text(
                        "Welcome to Celestia",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "Sign in to explore the cosmos",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // ---------------------------------------------------------
                    // EMAIL FIELD
                    // ---------------------------------------------------------
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    // ---------------------------------------------------------
                    // PASSWORD FIELD
                    // ---------------------------------------------------------
                    var passwordVisible by remember { mutableStateOf(false) }
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

                    // ---------------------------------------------------------
                    // LOGIN BUTTON
                    // ---------------------------------------------------------
                    Button(
                        onClick = {
                            when {
                                email.isBlank() || password.isBlank() ->
                                    toastMessage = "Please enter both fields"
                                else -> vm.login(email.trim(), password.trim())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E63FF)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Sign In",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("register") }) {
                        Text("Don’t have an account? Register")
                    }

                    Spacer(Modifier.height(20.dp))

                    // ---------------------------------------------------------
                    // GOOGLE LOGIN BUTTON
                    // ---------------------------------------------------------
                    Button(
                        onClick = {
                            // Always sign out before launching, avoids cached account issues
                            googleClient.signOut().addOnCompleteListener {
                                googleLauncher.launch(googleClient.signInIntent)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD9D9D9)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Sign in with Google", color = Color.Black)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ---------------------------------------------------------
                    // GITHUB LOGIN BUTTON
                    // ---------------------------------------------------------
                    Button(
                        onClick = { vm.githubLogin(activity) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD9D9D9)
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Sign in with GitHub", color = Color.Black)
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // TOAST FEEDBACK (Custom Celestia style)
        // ---------------------------------------------------------
        toastMessage?.let { msg ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 40.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                CelestiaToast(
                    message = msg,
                    backgroundColor = CelestiaOrange.copy(alpha = 0.95f)
                )
            }

            // Auto-dismiss after 2 seconds
            LaunchedEffect(msg) {
                delay(2000)
                toastMessage = null
            }
        }
    }
}