package com.example.celestia.auth

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider

/**
 * Authentication service providing OAuth login functionality for GitHub
 * (and structured future support for additional providers).
 *
 * This class centralizes authentication logic and keeps the ViewModel/UI clean.
 */
class AuthService(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    // === GitHub Login (With Forced Logout) ===

    /**
     * Initiates GitHub OAuth login flow using Firebase.
     *
     * Implementation notes:
     * - GitHub aggressively caches previous sessions.
     * - Opening the logout page in a custom tab ensures users always see
     *   the GitHub account selector instead of auto-signing in silently.
     *
     * OAuth Flow:
     * 1. Open GitHub logout URL via Chrome Custom Tab.
     * 2. Wait ~600ms to ensure logout completes.
     * 3. Launch Firebase OAuth provider (github.com).
     *
     * @param activity The hosting Activity.
     * @param onSuccess Callback for successful authentication.
     * @param onError Callback invoked on failure with a message.
     */
    fun signInWithGithub(
        activity: Activity,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (String) -> Unit
    ) {
        // Step 1 — Force GitHub logout so the account picker appears.
        val logoutUrl = Uri.parse("https://github.com/logout")
        CustomTabsIntent.Builder().build().launchUrl(context, logoutUrl)

        // Step 2 — Delay before starting OAuth to ensure logout completes.
        Handler(Looper.getMainLooper()).postDelayed({

            val provider = OAuthProvider.newBuilder("github.com").apply {
                scopes = listOf("user:email")
                addCustomParameter("allow_signup", "true")
            }

            firebaseAuth
                .startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { result ->
                    onSuccess(result.user)
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "GitHub sign-in failed")
                }

        }, 600)
    }
}
