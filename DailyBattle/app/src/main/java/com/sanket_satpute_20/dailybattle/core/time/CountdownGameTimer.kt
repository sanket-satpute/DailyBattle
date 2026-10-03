package com.sanket_satpute_20.dailybattle.core.time

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Implementation of GameTimer using a TimeProvider for accurate elapsed time tracking
 * that survives wall-clock changes, and handles pausing, resuming, and restoring.
 */
class CountdownGameTimer(
    private val timeProvider: TimeProvider,
    private val scope: CoroutineScope,
    private val tickIntervalMs: Long = 100L
) : GameTimer {

    private val _state = MutableStateFlow(TimerState.Initial)
    override val state: StateFlow<TimerState> = _state.asStateFlow()

    private var targetRealtime: Long = 0L
    private var timerJob: Job? = null

    override fun start(durationMillis: Long) {
        val now = timeProvider.elapsedRealtime()
        targetRealtime = now + durationMillis
        
        _state.update {
            it.copy(
                durationMillis = durationMillis,
                remainingMillis = durationMillis,
                isRunning = true,
                isFinished = false
            )
        }
        
        startTicking()
    }

    override fun pause() {
        if (!_state.value.isRunning || _state.value.isFinished) return
        
        timerJob?.cancel()
        timerJob = null
        
        // Final update before pausing
        val remaining = calculateRemaining(timeProvider.elapsedRealtime(), targetRealtime)
        
        _state.update {
            it.copy(
                remainingMillis = remaining,
                isRunning = false
            )
        }
    }

    override fun resume() {
        if (_state.value.isRunning || _state.value.isFinished || _state.value.remainingMillis <= 0) return
        
        val now = timeProvider.elapsedRealtime()
        targetRealtime = now + _state.value.remainingMillis
        
        _state.update {
            it.copy(isRunning = true)
        }
        
        startTicking()
    }

    override fun stop() {
        timerJob?.cancel()
        timerJob = null
        
        _state.update {
            it.copy(
                isRunning = false,
                isFinished = false
            )
        }
    }

    override fun restore(durationMillis: Long, remainingMillis: Long, wasRunning: Boolean) {
        timerJob?.cancel()
        timerJob = null
        
        if (remainingMillis <= 0) {
            _state.update {
                it.copy(
                    durationMillis = durationMillis,
                    remainingMillis = 0,
                    isRunning = false,
                    isFinished = true
                )
            }
            return
        }

        if (wasRunning) {
            val now = timeProvider.elapsedRealtime()
            targetRealtime = now + remainingMillis
            _state.update {
                it.copy(
                    durationMillis = durationMillis,
                    remainingMillis = remainingMillis,
                    isRunning = true,
                    isFinished = false
                )
            }
            startTicking()
        } else {
            _state.update {
                it.copy(
                    durationMillis = durationMillis,
                    remainingMillis = remainingMillis,
                    isRunning = false,
                    isFinished = false
                )
            }
        }
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val now = timeProvider.elapsedRealtime()
                val remaining = calculateRemaining(now, targetRealtime)
                
                if (remaining <= 0) {
                    _state.update {
                        it.copy(
                            remainingMillis = 0,
                            isRunning = false,
                            isFinished = true
                        )
                    }
                    break
                } else {
                    _state.update {
                        it.copy(
                            remainingMillis = remaining
                        )
                    }
                }
                delay(tickIntervalMs)
            }
        }
    }

    private fun calculateRemaining(now: Long, target: Long): Long {
        return max(0L, target - now)
    }
}
