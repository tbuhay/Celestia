package com.example.celestia.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

/**
 * Tracks whether the Celestia app is currently in the foreground.
 *
 * This is useful for:
 * - Suppressing notifications while the user is actively using the app
 * - Preventing background refreshes during active sessions
 * - Detecting transitions between foreground and background states
 *
 * Usage:
 * Call [init] once in your Application class to start monitoring lifecycle changes.
 */
object AppLifecycleTracker : DefaultLifecycleObserver {

    /**
     * `true` when the app is visible and in the foreground,
     * `false` when moved to the background.
     *
     * This value is updated automatically by lifecycle callbacks.
     */
    var isAppInForeground: Boolean = false
        private set

    /**
     * Called when the app enters the foreground.
     * Updates [isAppInForeground] to `true`.
     */
    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
    }

    /**
     * Called when the app moves to the background.
     * Updates [isAppInForeground] to `false`.
     */
    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
    }

    /**
     * Initializes the lifecycle tracker by registering it with
     * the app-wide [ProcessLifecycleOwner].
     *
     * Must be called from the Application class's `onCreate()`.
     */
    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
}