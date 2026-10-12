package com.sanket_satpute_20.dailybattle.core.lifecycle

import com.sanket_satpute_20.dailybattle.domain.battle.BattleSession
import com.sanket_satpute_20.dailybattle.domain.battle.BattleSessionRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes lifecycle transitions and coordinates persistence preservation for active battle sessions.
 *
 * Conforms to:
 * - 07_TECHNICAL_ARCHITECTURE.md §28 (Lifecycle Handling)
 * - 09_OFFLINE_AND_ERROR_HANDLING.md §18, §19, §20 (App Backgrounding & Gameplay State Recovery)
 * - 15_ANTIGRAVITY_RULES.md Rule 43 (Lifecycle Rule) & Rule 44 (Persistence Rule)
 */
interface LifecycleBattlePreserver {
    val activeSession: StateFlow<BattleSession?>

    /**
     * Registers the currently active session being played in memory.
     */
    fun attachActiveSession(session: BattleSession)

    /**
     * Clears the active session reference when battle is finalized or exited.
     */
    fun detachActiveSession()

    /**
     * Forces immediate persistence flush of the active session.
     */
    fun persistActiveSession()

    /**
     * Restores the authoritative session state from persistent storage.
     */
    fun restoreSession(userId: UserId, battleId: BattleId): BattleSession?
}

@Singleton
class DefaultLifecycleBattlePreserver @Inject constructor(
    private val sessionRepository: BattleSessionRepository,
    private val lifecycleManager: AppLifecycleManager,
) : LifecycleBattlePreserver, AppLifecycleListener {

    private val _activeSession = MutableStateFlow<BattleSession?>(null)
    override val activeSession: StateFlow<BattleSession?> = _activeSession.asStateFlow()

    init {
        lifecycleManager.addListener(this)
    }

    override fun attachActiveSession(session: BattleSession) {
        _activeSession.value = session
    }

    override fun detachActiveSession() {
        _activeSession.value = null
    }

    override fun persistActiveSession() {
        val current = _activeSession.value ?: return
        sessionRepository.saveSession(current)
    }

    override fun restoreSession(userId: UserId, battleId: BattleId): BattleSession? {
        val persisted = sessionRepository.getOfficialSession(userId, battleId)
        if (persisted != null) {
            _activeSession.value = persisted
        }
        return persisted
    }

    override fun onLifecycleEvent(event: AppLifecycleEvent) {
        when (event) {
            is AppLifecycleEvent.Background -> {
                // When app goes into background (screen lock, notification, home, app switcher):
                // Flush and persist active session to ensure it survives process death or backgrounding (§44)
                persistActiveSession()
            }
            is AppLifecycleEvent.Foreground -> {
                // When returning to foreground:
                // If active session is attached, ensure state is synchronized with storage
                val current = _activeSession.value
                if (current != null) {
                    val persisted = sessionRepository.getOfficialSession(current.userId, current.battleId)
                    if (persisted != null) {
                        _activeSession.value = persisted
                    }
                }
            }
        }
    }
}
