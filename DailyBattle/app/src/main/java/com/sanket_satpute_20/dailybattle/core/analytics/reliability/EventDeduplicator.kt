package com.sanket_satpute_20.dailybattle.core.analytics.reliability

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent
import com.sanket_satpute_20.dailybattle.core.analytics.CoreAnalyticsEvents
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles duplicate analytics events and enforces idempotency per `14_ANALYTICS.md` §44
 * and `ROADMAP.md` Sprint 20.3.
 *
 * Prevents multiple identical logical events (e.g. rapid taps on completion buttons,
 * repeated navigation triggers) from generating duplicate product metrics.
 */
class EventDeduplicator(
    private val windowDurationMs: Long = 5_000L,
    private val maxCacheSize: Int = 1_000
) {

    // Maps deduplication key -> timestamp when recorded
    private val recentEventKeys = ConcurrentHashMap<String, Long>()

    companion object {
        const val PARAM_DEDUPLICATION_ID = "deduplication_id"

        /**
         * Computes the canonical deduplication key for an event.
         */
        fun computeDeduplicationKey(event: AnalyticsEvent): String {
            // 1. Explicit deduplication_id if supplied
            val explicitId = event.properties[PARAM_DEDUPLICATION_ID]?.toString()
            if (!explicitId.isNullOrBlank()) {
                return "${event.name}:$explicitId"
            }

            val sessionId = event.properties[CoreAnalyticsEvents.PARAM_SESSION_ID]?.toString()
            val challengeIndex = event.properties[CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX]?.toString()
            val challengeType = event.properties[CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE]?.toString()
            val battleId = event.properties[CoreAnalyticsEvents.PARAM_BATTLE_ID]?.toString()

            // 2. Specialized deduplication by event category
            return when (event.name) {
                CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED -> {
                    if (sessionId != null) "battle_completed:$sessionId"
                    else "battle_completed:$battleId:${event.properties[CoreAnalyticsEvents.PARAM_TOTAL_SCORE]}"
                }
                CoreAnalyticsEvents.EVENT_CHALLENGE_COMPLETED -> {
                    if (sessionId != null && challengeIndex != null) {
                        "challenge_completed:$sessionId:$challengeIndex"
                    } else if (sessionId != null && challengeType != null) {
                        "challenge_completed:$sessionId:$challengeType"
                    } else {
                        defaultEventSignature(event)
                    }
                }
                CoreAnalyticsEvents.EVENT_CHALLENGE_STARTED -> {
                    if (sessionId != null && challengeIndex != null) {
                        "challenge_started:$sessionId:$challengeIndex"
                    } else {
                        defaultEventSignature(event)
                    }
                }
                CoreAnalyticsEvents.EVENT_BATTLE_STARTED -> {
                    if (sessionId != null) "battle_started:$sessionId"
                    else "battle_started:$battleId"
                }
                CoreAnalyticsEvents.EVENT_PRACTICE_COMPLETED -> {
                    if (sessionId != null) "practice_completed:$sessionId"
                    else defaultEventSignature(event)
                }
                CoreAnalyticsEvents.EVENT_FRIEND_ADDED -> {
                    defaultEventSignature(event)
                }
                else -> defaultEventSignature(event)
            }
        }

        private fun defaultEventSignature(event: AnalyticsEvent): String {
            val sortedProps = event.properties.entries
                .filter { it.key != AnalyticsEvent.PARAM_TIMESTAMP }
                .sortedBy { it.key }
                .joinToString(",") { "${it.key}=${it.value}" }
            return "${event.name}:$sortedProps"
        }
    }

    /**
     * Determines whether an event is unique (should be accepted) or duplicate (should be dropped).
     *
     * @param event The event to evaluate.
     * @param currentTimeMs The timestamp of evaluation.
     * @return `true` if this event is new and should be tracked; `false` if it is a duplicate within [windowDurationMs].
     */
    fun shouldTrack(event: AnalyticsEvent, currentTimeMs: Long = System.currentTimeMillis()): Boolean {
        pruneExpired(currentTimeMs)

        val key = computeDeduplicationKey(event)
        val lastSeen = recentEventKeys[key]

        if (lastSeen != null && (currentTimeMs - lastSeen) < windowDurationMs) {
            // Duplicate detected within window
            return false
        }

        // Enforce cache bounds to prevent unbounded memory growth
        if (recentEventKeys.size >= maxCacheSize) {
            val oldestKey = recentEventKeys.minByOrNull { it.value }?.key
            if (oldestKey != null) recentEventKeys.remove(oldestKey)
        }

        recentEventKeys[key] = currentTimeMs
        return true
    }

    /**
     * Removes expired entries older than [windowDurationMs].
     */
    fun pruneExpired(currentTimeMs: Long = System.currentTimeMillis()) {
        val cutoff = currentTimeMs - windowDurationMs
        recentEventKeys.entries.removeIf { it.value < cutoff }
    }

    /**
     * Clears all recorded deduplication keys.
     */
    fun clear() {
        recentEventKeys.clear()
    }
}
