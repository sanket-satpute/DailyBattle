package com.sanket_satpute_20.dailybattle.core.analytics.reliability

import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Storage contract for managing queued analytics events across offline, delayed, and recovery scenarios.
 *
 * Enforces `14_ANALYTICS.md` §77:
 * - Defined max capacity (data minimization / cost control).
 * - Chronological ordering.
 * - Idempotent removal.
 */
interface AnalyticsQueueStore {

    /**
     * Enqueues an event for reliable delivery. If max capacity is exceeded, oldest non-critical events are evicted.
     */
    fun enqueue(queuedEvent: QueuedAnalyticsEvent): Boolean

    /**
     * Retrieves up to [limit] pending events in FIFO order without removing them.
     */
    fun peekBatch(limit: Int = 50): List<QueuedAnalyticsEvent>

    /**
     * Removes an event by its [queueId] upon successful delivery.
     */
    fun remove(queueId: String): Boolean

    /**
     * Updates an event (e.g. incrementing delivery attempts after failure).
     */
    fun update(queuedEvent: QueuedAnalyticsEvent)

    /**
     * Returns the current number of pending queued events.
     */
    fun size(): Int

    /**
     * Clears all pending events from the queue store.
     */
    fun clear()

    /**
     * Returns all queued events currently in storage.
     */
    fun getAll(): List<QueuedAnalyticsEvent>
}

/**
 * Thread-safe in-memory queue store enforcing max capacity limits.
 */
class InMemoryAnalyticsQueueStore(
    private val maxCapacity: Int = 500
) : AnalyticsQueueStore {

    private val queue = ConcurrentLinkedQueue<QueuedAnalyticsEvent>()

    override fun enqueue(queuedEvent: QueuedAnalyticsEvent): Boolean {
        // Enforce max queue capacity (§77): evict oldest if at capacity
        while (queue.size >= maxCapacity) {
            queue.poll()
        }
        return queue.add(queuedEvent)
    }

    override fun peekBatch(limit: Int): List<QueuedAnalyticsEvent> {
        return queue.take(limit)
    }

    override fun remove(queueId: String): Boolean {
        return queue.removeIf { it.queueId == queueId }
    }

    override fun update(queuedEvent: QueuedAnalyticsEvent) {
        val list = queue.toList()
        queue.clear()
        for (item in list) {
            if (item.queueId == queuedEvent.queueId) {
                queue.add(queuedEvent)
            } else {
                queue.add(item)
            }
        }
    }

    override fun size(): Int = queue.size

    override fun clear() {
        queue.clear()
    }

    override fun getAll(): List<QueuedAnalyticsEvent> = queue.toList()
}
