package com.example.celestia.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.celestia.auth.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import android.app.Activity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.userProfileChangeRequest

/**
 * ViewModel responsible for all authentication logic in Celestia.
 *
 * Handles:
 * - Email/password login & registration
 * - Google OAuth login
 * - GitHub OAuth login (via [AuthService])
 * - Logout
 * - Error forwarding to UI
 * - Display name tracking
 *
 * All state is exposed via LiveData to allow clean reactive UI updates.
 */
class AuthViewModel : ViewModel() {

    /** Firebase authentication entry point. */
    private val firebaseAuth = FirebaseAuth.getInstance()

    /** Wrapper handling GitHub OAuth logic. Initialized via [init]. */
    private lateinit var authService: AuthService

    /** Tracks whether the user is currently authenticated. */
    private val _isAuthenticated = MutableLiveData<Boolean>(firebaseAuth.currentUser != null)
    val isAuthenticated: LiveData<Boolean> get() = _isAuthenticated

    /** Holds error messages to be presented by the UI. */
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    /** User’s Firebase display name (if available). */
    private val _userName = MutableLiveData<String?>(firebaseAuth.currentUser?.displayName)
    val userName: LiveData<String?> get() = _userName

    /**
     * Initializes the authentication service (required for GitHub login).
     *
     * Must be called once from Register/Login screens.
     */
    fun init(context: Context) {
        authService = AuthService(context)
    }

    // --------------------------------------------------------
    // EMAIL / PASSWORD AUTH
    // --------------------------------------------------------

    /**
     * Attempts to log in using email + password.
     *
     * On success:
     * - User is reloaded to ensure profile accuracy
     * - `_isAuthenticated` and `_userName` are updated
     *
     * On failure:
     * - `_errorMessage` is set for UI display
     */
    fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.reload()?.addOnSuccessListener {
                    _isAuthenticated.value = true
                    _userName.value = firebaseAuth.currentUser?.displayName
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Login failed"
            }
    }

    /**
     * Registers a new user via email/password and sets their display name.
     *
     * Steps:
     * 1. Create user with Firebase
     * 2. Apply a profile update with the provided `name`
     * 3. Reload the user object to ensure displayName updates
     * 4. Notify UI of authentication success
     */
    fun register(name: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user

                // Update displayName for better UI personalization
                val updates = userProfileChangeRequest {
                    displayName = name
                }

                user?.updateProfile(updates)?.addOnCompleteListener {
                    user.reload()
                    _isAuthenticated.value = true
                    _userName.value = user.displayName ?: name
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Registration failed"
            }
    }

    // --------------------------------------------------------
    // GOOGLE AUTH
    // --------------------------------------------------------

    /**
     * Logs in the user using a Google ID token from the Google Sign-In API.
     *
     * Called after a successful `rememberLauncherForActivityResult` result.
     *
     * On success:
     * - User is authenticated
     * - Display name is updated
     */
    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                _isAuthenticated.value = true
                _userName.value = authResult.user?.displayName
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Google login failed"
            }
    }

    // --------------------------------------------------------
    // GITHUB AUTH (VIA AuthService)
    // --------------------------------------------------------

    /**
     * Initiates GitHub OAuth login using the custom [AuthService].
     *
     * The service handles:
     * - Starting the OAuth flow
     * - Receiving the callback
     * - Converting the GitHub result into a Firebase credential
     */
    fun githubLogin(activity: Activity) {
        authService.signInWithGithub(
            activity = activity,
            onSuccess = { user ->
                _isAuthenticated.value = true
                _userName.value = user?.displayName
            },
            onError = { msg ->
                _errorMessage.value = msg
            }
        )
    }

    /**
     * Clears the current error message after it has been consumed by the UI.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Signs out the current user and resets relevant authentication state.
     */
    fun logout() {
        firebaseAuth.signOut()
        _isAuthenticated.value = false
        _userName.value = null
    }

    // --------------------------------------------------------
    // ACCOUNT UPDATES (Email, Password, Name)
    // --------------------------------------------------------

    fun updateDisplayName(newName: String, onResult: (Boolean, String?) -> Unit) {
        val user = firebaseAuth.currentUser ?: return onResult(false, "Not logged in")

        val updates = userProfileChangeRequest {
            displayName = newName
        }

        user.updateProfile(updates)
            .addOnSuccessListener {
                _userName.value = newName
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun updateEmail(newEmail: String, password: String, onResult: (Boolean, String?) -> Unit) {
        val user = firebaseAuth.currentUser ?: return onResult(false, "Not logged in")

        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updateEmail(newEmail)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun updatePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        val user = firebaseAuth.currentUser ?: return onResult(false, "Not logged in")

        val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPassword)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { e -> onResult(false, e.message) }
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}
