package com.example.celestia.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.celestia.data.store.ThemeManager
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val themeManager = ThemeManager(application)

    val darkModeEnabled: LiveData<Boolean> =
        themeManager.darkModeFlow.asLiveData()

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeManager.setDarkMode(enabled)
        }
    }
}