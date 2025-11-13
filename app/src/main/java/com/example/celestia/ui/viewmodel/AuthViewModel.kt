package com.example.celestia.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> get() = _isAuthenticated

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    private val _userName = MutableLiveData<String?>()
    val userName: LiveData<String?> get() = _userName

    init {
        _isAuthenticated.value = auth.currentUser != null
        _userName.value = auth.currentUser?.displayName
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isAuthenticated.value = true
                    _userName.value = auth.currentUser?.displayName
                    Log.d("AuthVM", "Login success")
                } else {
                    _errorMessage.value =
                        task.exception?.message ?: "Login failed. Please check your credentials."
                    Log.e("AuthVM", "Login failed", task.exception)
                }
            }
    }

    fun register(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = auth.currentUser
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener {

                        user.reload().addOnCompleteListener {

                            _userName.value = user.displayName ?: name

                            _isAuthenticated.value = true
                            Log.d("AuthVM", "Registration success + displayName refreshed")
                        }
                    }

                } else {
                    _errorMessage.value =
                        task.exception?.message ?: "Registration failed. Try again later."
                    Log.e("AuthVM", "Registration failed", task.exception)
                }
            }
    }

    fun logout() {
        auth.signOut()
        _isAuthenticated.value = false
        _userName.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
