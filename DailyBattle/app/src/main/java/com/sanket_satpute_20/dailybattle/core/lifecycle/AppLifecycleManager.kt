package com.sanket_satpute_20.dailybattle.core.lifecycle

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Manages and coordinates application-wide lifecycle transitions between foreground and background.
 *
 * Conforms to:
 * - 07_TECHNICAL_ARCHITECTURE.md §28 (Lifecycle Handling)
 * - 15_ANTIGRAVITY_RULES.md Rule 43 (Lifecycle Rule)
 * - ROADMAP.md Sprint 18.1 (Background, foreground, screen lock, notification interruption, app switching)
 */
interface AppLifecycleManager {
    val state: StateFlow<AppLifecycleState>
    val events: SharedFlow<AppLifecycleEvent>
    val lastBackgroundAtMillis: Long?
    val lastForegroundAtMillis: Long?
    val currentReason: BackgroundReason?

    /**
     * Dispatches a backgrounding event triggered by user navigation, screen lock, notification,
     * or app switching.
     */
    fun onBackground(
        reason: BackgroundReason = BackgroundReason.UNKNOWN,
        timestamp: Long? = null,
    )

    /**
     * Dispatches a foregrounding event when returning to the application.
     */
    fun onForeground(
        timestamp: Long? = null,
    )

    /**
     * Dispatches a recreation event when an Activity is recreated following
     * configuration changes or system process restoration.
     */
    fun onRecreation(
        isProcessRecreation: Boolean,
        timestamp: Long? = null,
    )

    val listenerCount: Int get() = 0

    fun addListener(listener: AppLifecycleListener)
    fun removeListener(listener: AppLifecycleListener)
}

@Singleton
class DefaultAppLifecycleManager @Inject constructor(
    private val clock: Clock,
) : AppLifecycleManager {

    override val listenerCount: Int get() = listeners.size

    private val _state = MutableStateFlow(AppLifecycleState.FOREGROUND)
    override val state: StateFlow<AppLifecycleState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AppLifecycleEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<AppLifecycleEvent> = _events.asSharedFlow()

    private var _lastBackgroundAtMillis: Long? = null
    override val lastBackgroundAtMillis: Long? get() = _lastBackgroundAtMillis

    private var _lastForegroundAtMillis: Long? = null
    override val lastForegroundAtMillis: Long? get() = _lastForegroundAtMillis

    private var _currentReason: BackgroundReason? = null
    override val currentReason: BackgroundReason? get() = _currentReason

    private val listeners = CopyOnWriteArrayList<AppLifecycleListener>()

    override fun onBackground(reason: BackgroundReason, timestamp: Long?) {
        val now = timestamp ?: clock.millis()
        if (_state.value == AppLifecycleState.BACKGROUND && _currentReason == reason) {
            return // Idempotent when identical background event occurs
        }

        _state.value = AppLifecycleState.BACKGROUND
        _lastBackgroundAtMillis = now
        _currentReason = reason

        val event = AppLifecycleEvent.Background(now, reason)
        _events.tryEmit(event)
        for (listener in listeners) {
            listener.onLifecycleEvent(event)
        }
    }

    override fun onForeground(timestamp: Long?) {
        val now = timestamp ?: clock.millis()
        if (_state.value == AppLifecycleState.FOREGROUND) {
            return // Already in foreground
        }

        val backgroundDuration = _lastBackgroundAtMillis?.let { bgTime ->
            max(0L, now - bgTime)
        } ?: 0L

        _state.value = AppLifecycleState.FOREGROUND
        _lastForegroundAtMillis = now

        val event = AppLifecycleEvent.Foreground(now, backgroundDuration)
        _events.tryEmit(event)
        for (listener in listeners) {
            listener.onLifecycleEvent(event)
        }
    }

    override fun onRecreation(isProcessRecreation: Boolean, timestamp: Long?) {
        val now = timestamp ?: clock.millis()
        val event = AppLifecycleEvent.Recreated(now, isProcessRecreation)
        _events.tryEmit(event)
        for (listener in listeners) {
            listener.onLifecycleEvent(event)
        }
    }

    override fun addListener(listener: AppLifecycleListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun removeListener(listener: AppLifecycleListener) {
        listeners.remove(listener)
    }
}
