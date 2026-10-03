package com.sanket_satpute_20.dailybattle.core.time

import kotlinx.coroutines.flow.StateFlow

interface GameTimer {
    val state: StateFlow<TimerState>

    /** Starts the timer with the given duration. */
    fun start(durationMillis: Long)
    
    /** Pauses the timer, saving the remaining time. */
    fun pause()
    
    /** Resumes the timer from the paused remaining time. */
    fun resume()
    
    /** Stops the timer and clears state, but does not emit finished. */
    fun stop()
    
    /** Restores a previously saved state (e.g. from process recreation or background). */
    fun restore(durationMillis: Long, remainingMillis: Long, wasRunning: Boolean)
}
