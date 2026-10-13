package com.sanket_satpute_20.dailybattle.core.analytics.reliability

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent
import com.sanket_satpute_20.dailybattle.core.analytics.CoreAnalyticsEvents

/**
 * Tracks session progression and detects missing events in the funnel per `14_ANALYTICS.md` §18, §66-§67.
 *
 * If a session starts but terminates abruptly (app killed, crash, or new session started before previous finished),
 * synthesizes a terminal [CoreAnalyticsEvents.battleAbandoned] event to close the funnel cleanly.
 */
class SessionFunnelAuditor {

    data class ActiveSession(
        val battleId: String,
        val sessionId: String,
        val mode: String,
        val startedAt: Long,
        var lastChallengeIndex: Int = 0
    )

    @Volatile
    private var activeSession: ActiveSession? = null

    /**
     * Inspects an outgoing event and checks for session transitions or funnel interruptions.
     *
     * @return A synthesized recovery event if a missing terminal event was detected, or `null`.
     */
    fun processEvent(event: AnalyticsEvent): AnalyticsEvent? {
        var syntheticEvent: AnalyticsEvent? = null

        when (event.name) {
            CoreAnalyticsEvents.EVENT_BATTLE_STARTED -> {
                val current = activeSession
                val newSessionId = event.properties[CoreAnalyticsEvents.PARAM_SESSION_ID]?.toString() ?: ""
                val newBattleId = event.properties[CoreAnalyticsEvents.PARAM_BATTLE_ID]?.toString() ?: ""
                val mode = event.properties[CoreAnalyticsEvents.PARAM_BATTLE_MODE]?.toString()
                    ?: AnalyticsEvent.VALUE_MODE_OFFICIAL

                // If previous session was active and not closed, it was abandoned before completion!
                if (current != null && current.sessionId != newSessionId) {
                    syntheticEvent = CoreAnalyticsEvents.battleAbandoned(
                        battleId = current.battleId,
                        sessionId = current.sessionId,
                        mode = current.mode,
                        reason = "abandoned_uncompleted_session"
                    )
                }

                activeSession = ActiveSession(
                    battleId = newBattleId,
                    sessionId = newSessionId,
                    mode = mode,
                    startedAt = event.timestamp
                )
            }

            CoreAnalyticsEvents.EVENT_CHALLENGE_STARTED -> {
                val index = (event.properties[CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX] as? Number)?.toInt() ?: 1
                activeSession?.lastChallengeIndex = index
            }

            CoreAnalyticsEvents.EVENT_CHALLENGE_COMPLETED -> {
                val index = (event.properties[CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX] as? Number)?.toInt() ?: 1
                activeSession?.lastChallengeIndex = index
            }

            CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED,
            CoreAnalyticsEvents.EVENT_BATTLE_ABANDONED -> {
                val eventSessionId = event.properties[CoreAnalyticsEvents.PARAM_SESSION_ID]?.toString()
                if (activeSession?.sessionId == eventSessionId) {
                    activeSession = null
                }
            }
        }

        return syntheticEvent
    }

    /**
     * Called during app startup/recovery to check if an active session was left uncompleted when the app was killed.
     */
    fun recoverInterruptedSession(reason: String = "interrupted_app_exit"): AnalyticsEvent? {
        val current = activeSession ?: return null
        activeSession = null
        return CoreAnalyticsEvents.battleAbandoned(
            battleId = current.battleId,
            sessionId = current.sessionId,
            mode = current.mode,
            reason = reason
        )
    }

    /**
     * Manually restore active session context (e.g. from saved state after process death).
     */
    fun restoreActiveSession(session: ActiveSession) {
        this.activeSession = session
    }

    /**
     * Returns the currently active session, if any.
     */
    fun getActiveSession(): ActiveSession? = activeSession

    /**
     * Resets the auditor state.
     */
    fun clear() {
        activeSession = null
    }
}
