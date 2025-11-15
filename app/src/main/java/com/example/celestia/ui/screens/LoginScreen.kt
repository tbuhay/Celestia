package com.example.celestia.ui.screens

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
import com.example.celestia.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.celestia.ui.components.CelestiaToast
import com.example.celestia.ui.theme.CelestiaOrange
import com.example.celestia.ui.theme.CelestiaPurple
import com.example.celestia.ui.theme.DarkSurface
import com.example.celestia.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val isAuthenticated by vm.isAuthenticated.observeAsState(false)
    val errorMessage by vm.errorMessage.observeAsState()

    // Navigate on success
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Show Firebase errors as toasts
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            toastMessage = msg
            vm.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DarkSurface.copy(alpha = 0.9f),
                        CelestiaPurple.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon =
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(20.dp))

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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sign In", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = { navController.navigate("register") }) {
                        Text(
                            "Don’t have an account? Register",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    // --- Google Sign In ---
                    Button(
                        onClick = {},
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
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Icon",
                                tint = Color.Unspecified, // Keep original Google colors
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                "Sign in with Google",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // --- GitHub Sign In ---
                    Button(
                        onClick = {},
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
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_github),
                                contentDescription = "GitHub Icon",
                                tint = Color.Black, // GitHub is monochrome
                                modifier = Modifier.size(22.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                "Sign in with GitHub",
                                color = Color.Black,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Toast (bottom only)
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

            LaunchedEffect(msg) {
                delay(2500)
                toastMessage = null
            }
        }
    }
}
