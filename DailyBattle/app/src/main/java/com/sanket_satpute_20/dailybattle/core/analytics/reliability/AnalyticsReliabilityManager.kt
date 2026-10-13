package com.sanket_satpute_20.dailybattle.core.analytics.reliability

import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsEvent
import com.sanket_satpute_20.dailybattle.core.analytics.AnalyticsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State representing current analytics reliability diagnostics.
 */
data class AnalyticsReliabilityState(
    val isOnline: Boolean = true,
    val pendingQueueSize: Int = 0,
    val failedDeliveryCount: Int = 0,
    val droppedDuplicateCount: Int = 0,
    val deliveredCount: Int = 0,
    val lastError: String? = null
)

/**
 * Coordinates reliability handling for analytics events:
 * - Duplicate event filtering (idempotency)
 * - Missing event detection (funnel integrity)
 * - Offline event buffering (FIFO queue)
 * - Delayed event dispatch (preserved original timestamps)
 * - App-killed event recovery
 * - Provider failure retry & fallback
 */
class AnalyticsReliabilityManager(
    val deduplicator: EventDeduplicator = EventDeduplicator(),
    val queueStore: AnalyticsQueueStore = InMemoryAnalyticsQueueStore(),
    val funnelAuditor: SessionFunnelAuditor = SessionFunnelAuditor(),
    private val maxDeliveryAttempts: Int = 3
) {

    private val _reliabilityState = MutableStateFlow(AnalyticsReliabilityState())
    val reliabilityState: StateFlow<AnalyticsReliabilityState> = _reliabilityState.asStateFlow()

    /**
     * Updates connectivity status. If transitioning to online, triggers queued event delivery.
     */
    fun setOnline(online: Boolean, providers: List<AnalyticsProvider> = emptyList()) {
        val wasOffline = !_reliabilityState.value.isOnline
        _reliabilityState.update { it.copy(isOnline = online) }
        if (online && wasOffline && providers.isNotEmpty()) {
            flushQueuedEvents(providers)
        }
    }

    /**
     * Processes an incoming event through deduplication, funnel auditing, and provider delivery.
     *
     * @return `true` if event was delivered or queued; `false` if dropped as duplicate.
     */
    fun processAndDispatch(event: AnalyticsEvent, providers: List<AnalyticsProvider>): Boolean {
        // 1. Deduplication check (handle duplicate event)
        if (!deduplicator.shouldTrack(event)) {
            _reliabilityState.update { it.copy(droppedDuplicateCount = it.droppedDuplicateCount + 1) }
            return false
        }

        // 2. Missing event check (audit funnel and dispatch any synthesized recovery event)
        val syntheticRecoveryEvent = funnelAuditor.processEvent(event)
        if (syntheticRecoveryEvent != null) {
            dispatchDirectOrQueue(syntheticRecoveryEvent, providers)
        }

        // 3. Dispatch or queue
        return dispatchDirectOrQueue(event, providers)
    }

    /**
     * Flushes buffered offline/delayed events to available providers.
     *
     * @return Number of successfully flushed events.
     */
    fun flushQueuedEvents(providers: List<AnalyticsProvider>): Int {
        if (!_reliabilityState.value.isOnline) return 0

        val batch = queueStore.peekBatch(limit = 100)
        var deliveredCount = 0

        for (queued in batch) {
            var anyProviderSucceeded = false
            var lastErrorMsg: String? = null

            for (provider in providers) {
                if (!provider.isEnabled) continue
                try {
                    provider.logEvent(queued.event)
                    anyProviderSucceeded = true
                } catch (t: Throwable) {
                    lastErrorMsg = t.message ?: t.javaClass.simpleName
                }
            }

            if (anyProviderSucceeded || providers.isEmpty()) {
                queueStore.remove(queued.queueId)
                deliveredCount++
            } else {
                // Provider still unavailable; increment attempts
                val updated = queued.withFailedAttempt(System.currentTimeMillis(), lastErrorMsg ?: "Provider failed")
                if (updated.deliveryAttempts >= maxDeliveryAttempts) {
                    // Evict permanently failed event after max retries
                    queueStore.remove(queued.queueId)
                } else {
                    queueStore.update(updated)
                }
            }
        }

        _reliabilityState.update {
            it.copy(
                pendingQueueSize = queueStore.size(),
                deliveredCount = it.deliveredCount + deliveredCount
            )
        }

        return deliveredCount
    }

    /**
     * Recovers any unclosed session left when app was killed.
     */
    fun recoverInterruptedSession(providers: List<AnalyticsProvider>) {
        val recoveryEvent = funnelAuditor.recoverInterruptedSession()
        if (recoveryEvent != null) {
            dispatchDirectOrQueue(recoveryEvent, providers)
        }
    }

    private fun dispatchDirectOrQueue(event: AnalyticsEvent, providers: List<AnalyticsProvider>): Boolean {
        val isOnline = _reliabilityState.value.isOnline

        // If offline, buffer immediately
        if (!isOnline) {
            queueStore.enqueue(QueuedAnalyticsEvent(event = event))
            _reliabilityState.update { it.copy(pendingQueueSize = queueStore.size()) }
            return true
        }

        // Online: attempt delivery to providers
        var anyProviderSucceeded = false
        var lastErrorMsg: String? = null

        for (provider in providers) {
            if (!provider.isEnabled) continue
            try {
                provider.logEvent(event)
                anyProviderSucceeded = true
            } catch (t: Throwable) {
                lastErrorMsg = t.message ?: t.javaClass.simpleName
            }
        }

        if (anyProviderSucceeded || providers.none { it.isEnabled }) {
            _reliabilityState.update { it.copy(deliveredCount = it.deliveredCount + 1) }
            return true
        } else {
            // All active providers failed (provider unavailable) -> buffer for retry
            queueStore.enqueue(
                QueuedAnalyticsEvent(
                    event = event,
                    deliveryAttempts = 1,
                    lastAttemptAt = System.currentTimeMillis(),
                    lastError = lastErrorMsg
                )
            )
            _reliabilityState.update {
                it.copy(
                    pendingQueueSize = queueStore.size(),
                    failedDeliveryCount = it.failedDeliveryCount + 1,
                    lastError = lastErrorMsg
                )
            }
            return true
        }
    }
}
