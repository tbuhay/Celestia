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

class AuthService(private val context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    // --------------------------------------------------------
    // GITHUB LOGIN (WITH LOGOUT)
    // --------------------------------------------------------
    fun signInWithGithub(
        activity: Activity,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (String) -> Unit
    ) {
        // Step 1: Force GitHub logout to ensure account selector appears
        val logoutUrl = Uri.parse("https://github.com/logout")
        CustomTabsIntent.Builder().build().launchUrl(context, logoutUrl)

        // Step 2: Delay before launching OAuth
        Handler(Looper.getMainLooper()).postDelayed({

            val provider = OAuthProvider.newBuilder("github.com")
            provider.scopes = listOf("user:email")
            provider.addCustomParameter("allow_signup", "true")

            firebaseAuth
                .startActivityForSignInWithProvider(activity, provider.build())
                .addOnSuccessListener { authResult ->
                    onSuccess(authResult.user)
                }
                .addOnFailureListener { e ->
                    onError(e.message ?: "GitHub sign-in failed")
                }

        }, 600)
    }
}
