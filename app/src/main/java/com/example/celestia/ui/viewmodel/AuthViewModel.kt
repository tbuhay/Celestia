package com.example.celestia.ui.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.celestia.auth.AuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import android.app.Activity

class AuthViewModel : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var authService: AuthService

    private val _isAuthenticated = MutableLiveData<Boolean>(firebaseAuth.currentUser != null)
    val isAuthenticated: LiveData<Boolean> get() = _isAuthenticated

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _userName = MutableLiveData<String?>(firebaseAuth.currentUser?.displayName)
    val userName: LiveData<String?> get() = _userName

    fun init(context: Context) {
        authService = AuthService(context)
    }

    // --------------------------------------------------------
    // EMAIL / PASSWORD
    // --------------------------------------------------------
    fun login(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _isAuthenticated.value = true
                _userName.value = firebaseAuth.currentUser?.displayName
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Login failed"
            }
    }

    fun register(name: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _isAuthenticated.value = true
                _userName.value = name
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Registration failed"
            }
    }

    // --------------------------------------------------------
    // GOOGLE LOGIN (ID TOKEN COMES FROM SCREEN LAUNCHER)
    // --------------------------------------------------------
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
    // GITHUB LOGIN (SERVICE)
    // --------------------------------------------------------
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

    fun clearError() {
        _errorMessage.value = null
    }

    fun logout() {
        firebaseAuth.signOut()
        _isAuthenticated.value = false
        _userName.value = null
    }
}
