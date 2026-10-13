package com.sanket_satpute_20.dailybattle.core.analytics.reliability

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent
import java.util.UUID

/**
 * Model representing an analytics event queued for reliable offline/delayed delivery or retry.
 *
 * Preserves the original [AnalyticsEvent.timestamp] so delayed events reflect their true occurrence time (§76).
 */
data class QueuedAnalyticsEvent(
    val queueId: String = UUID.randomUUID().toString(),
    val event: AnalyticsEvent,
    val enqueuedAt: Long = System.currentTimeMillis(),
    val deliveryAttempts: Int = 0,
    val lastAttemptAt: Long? = null,
    val lastError: String? = null
) {
    fun withFailedAttempt(now: Long, error: String): QueuedAnalyticsEvent {
        return copy(
            deliveryAttempts = deliveryAttempts + 1,
            lastAttemptAt = now,
            lastError = error
        )
    }
}
