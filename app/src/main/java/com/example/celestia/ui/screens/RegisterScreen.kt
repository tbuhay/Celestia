package com.example.celestia.ui.screens

import android.app.Activity
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

import com.example.celestia.R
import com.example.celestia.ui.components.CelestiaToast
import com.example.celestia.ui.theme.CelestiaOrange
import com.example.celestia.ui.theme.CelestiaPurple
import com.example.celestia.ui.theme.CelestiaSkyBlue
import com.example.celestia.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Registration screen for Celestia.
 *
 * Features:
 * - Email/password sign-up
 * - Google OAuth account creation (via Firebase)
 * - GitHub OAuth account creation
 * - Input validation with toast feedback
 * - Password visibility toggle
 * - Automatic redirection to Home after successful registration
 *
 * When a user successfully registers:
 * - They are navigated to `"home"`
 * - The `"register"` screen is removed from the back stack
 *
 * @param navController Used to navigate after successful sign-up or return to login.
 * @param vm ViewModel handling Firebase authentication logic.
 */
@Composable
fun RegisterScreen(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity

    // Initialize Firebase AuthService only once
    LaunchedEffect(Unit) { vm.init(context) }

    // ---------------------------------------------------------
    // GOOGLE SIGN-IN CONFIGURATION
    // ---------------------------------------------------------
    /**
     * Constructs GoogleSignInOptions and GoogleSignInClient once and remembers them.
     */
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }

    val googleClient = remember { GoogleSignIn.getClient(context, gso) }

    var toastMessage by remember { mutableStateOf<String?>(null) }

    /**
     * Launcher that receives the result of Google OAuth and forwards
     * the ID token to the ViewModel.
     */
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                vm.signInWithGoogle(idToken)
            } else {
                toastMessage = "Google sign-in failed: no token"
            }
        } catch (e: Exception) {
            toastMessage = "Google sign-in cancelled or failed"
        }
    }

    // ---------------------------------------------------------
    // INPUT STATE
    // ---------------------------------------------------------
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isAuthenticated by vm.isAuthenticated.observeAsState(false)
    val errorMessage by vm.errorMessage.observeAsState()

    // ---------------------------------------------------------
    // AUTH EFFECTS
    // ---------------------------------------------------------

    /**
     * Navigate to Home when registration completes.
     */
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
            }
        }
    }

    /**
     * Display any authentication errors via custom toast.
     */
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            toastMessage = it
            vm.clearError()
        }
    }

    // ---------------------------------------------------------
    // UI — REGISTRATION FORM
    // ---------------------------------------------------------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CelestiaPurple.copy(alpha = 0.9f),
                        CelestiaSkyBlue.copy(alpha = 0.8f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
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
                    "Create Your Celestia Account",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                // ----------------------
                // NAME FIELD
                // ----------------------
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // ----------------------
                // EMAIL FIELD
                // ----------------------
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))

                // ----------------------
                // PASSWORD FIELD
                // ----------------------
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 chars)") },
                    singleLine = true,
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                // EMAIL/PASSWORD REGISTRATION BUTTON
                // ---------------------------------------------------------
                Button(
                    onClick = {
                        when {
                            name.isBlank() ->
                                toastMessage = "Please enter your name"
                            email.isBlank() || password.length < 6 ->
                                toastMessage = "Enter a valid email and password (min 6 chars)"
                            else ->
                                vm.register(name.trim(), email.trim(), password.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E63FF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Register",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Already have an account? Sign In")
                }

                Spacer(Modifier.height(20.dp))

                // ---------------------------------------------------------
                // GOOGLE SIGN-UP BUTTON
                // ---------------------------------------------------------
                Button(
                    onClick = {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sign up with Google", color = Color.Black)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ---------------------------------------------------------
                // GITHUB SIGN-UP BUTTON
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_github),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sign up with GitHub", color = Color.Black)
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // CUSTOM CELESTIA TOAST FEEDBACK
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
        }
    }
}
