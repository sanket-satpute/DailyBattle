package com.sanket_satpute_20.dailybattle.core.analytics

import com.sanket_satpute_20.dailybattle.core.analytics.privacy.AnalyticsConsentManager
import com.sanket_satpute_20.dailybattle.core.analytics.privacy.AnalyticsPrivacySanitizer
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.AnalyticsReliabilityManager
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.AnalyticsReliabilityState
import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Default implementation of [AnalyticsTracker] that dispatches to registered [AnalyticsProvider]s
 * through [AnalyticsReliabilityManager] and [AnalyticsPrivacySanitizer].
 *
 * Implements core architectural guardrails:
 * - Observational only (REQ-AN-001): Never alters product/domain/gameplay state.
 * - Resilience (REQ-AN-003): Catches all exceptions from providers; provider failure never crashes
 *   the app, blocks gameplay, or throws to the caller.
 * - Privacy & Data Minimization (REQ-AN-005, REQ-AN-013, Sprint 20.4): Deeply sanitizes sensitive keys,
 *   redacts PII/credentials/tokens, enforces Battle Code privacy, and supports consent opt-out and account deletion wipe.
 * - Environment Attribution (REQ-AN-014): Automatically enriches events with the runtime environment
 *   so test/dev data does not contaminate production.
 * - Event Reliability (Sprint 20.3): Handles duplicates, missing events, offline queues, delayed
 *   delivery, and provider failures via [reliabilityManager].
 */
class DefaultAnalyticsTracker(
    private val environmentProvider: EnvironmentProvider,
    initialProviders: List<AnalyticsProvider> = emptyList(),
    val reliabilityManager: AnalyticsReliabilityManager = AnalyticsReliabilityManager(),
    val consentManager: AnalyticsConsentManager = AnalyticsConsentManager()
) : AnalyticsTracker {

    private val providers = CopyOnWriteArrayList<AnalyticsProvider>(initialProviders)

    val reliabilityState: StateFlow<AnalyticsReliabilityState> = reliabilityManager.reliabilityState
    val isConsentGranted: StateFlow<Boolean> = consentManager.isConsentGranted

    /**
     * Registers a new provider. If a provider with the same name already exists, it is replaced.
     */
    fun registerProvider(provider: AnalyticsProvider) {
        providers.removeIf { it.name == provider.name }
        providers.add(provider)
    }

    /**
     * Unregisters a provider by its name.
     */
    fun unregisterProvider(name: String) {
        providers.removeIf { it.name == name }
    }

    /**
     * Returns an unmodifiable snapshot of registered providers.
     */
    fun getProviders(): List<AnalyticsProvider> = providers.toList()

    /**
     * Updates user privacy consent preference (§52). If revoked, immediately purges pending queues.
     */
    fun setConsentGranted(granted: Boolean) {
        consentManager.setConsentGranted(granted)
        if (!granted) {
            clearUserData()
        }
    }

    /**
     * Wipes all user-associated data and pending queues on account deletion or privacy reset (§78).
     */
    fun clearUserData() {
        // 1. Wipe User ID across providers
        setUserId(null)

        // 2. Clear reliability queues and deduplicator
        reliabilityManager.queueStore.clear()
        reliabilityManager.deduplicator.clear()
        reliabilityManager.funnelAuditor.clear()

        // 3. Wipe memory provider stores if registered
        for (provider in providers) {
            (provider as? InMemoryAnalyticsProvider)?.clear()
        }
    }

    /**
     * Updates network/connectivity state in reliability manager and flushes queued events when online.
     */
    fun setOnline(online: Boolean) {
        reliabilityManager.setOnline(online, providers.toList())
    }

    /**
     * Flushes buffered offline or retry events to registered providers.
     */
    fun flushQueuedEvents(): Int {
        if (!consentManager.hasConsent) return 0
        return reliabilityManager.flushQueuedEvents(providers.toList())
    }

    /**
     * Recovers any session that was interrupted without a completion or abandon event (e.g. app killed).
     */
    fun recoverInterruptedSession() {
        if (!consentManager.hasConsent) return
        reliabilityManager.recoverInterruptedSession(providers.toList())
    }

    override fun track(event: AnalyticsEvent) {
        // Privacy Gate (§52): Stop tracking if user opted out
        if (!consentManager.hasConsent) return

        val preparedEvent = prepareEvent(event)

        try {
            reliabilityManager.processAndDispatch(preparedEvent, providers.toList())
        } catch (_: Throwable) {
            // REQ-AN-003: Analytics failure must not block gameplay or crash the application.
        }
    }

    override fun track(eventName: String, properties: Map<String, Any?>) {
        if (!consentManager.hasConsent) return
        if (!AnalyticsEvent.isValidEventName(eventName)) {
            // Guard: invalid names are safely dropped or sanitized rather than crashing
            return
        }
        val event = AnalyticsEvent(name = eventName, properties = properties)
        track(event)
    }

    override fun setUserProperty(name: String, value: String?) {
        if (!consentManager.hasConsent) return

        val sanitizedProperty = AnalyticsPrivacySanitizer.sanitizeUserProperty(name, value) ?: return
        val (sanitizedKey, sanitizedVal) = sanitizedProperty

        for (provider in providers) {
            if (!provider.isEnabled) continue
            try {
                provider.setUserProperty(sanitizedKey, sanitizedVal)
            } catch (_: Throwable) {
                // Safe error suppression
            }
        }
    }

    override fun setUserId(userId: String?) {
        if (!consentManager.hasConsent && userId != null) return

        val sanitizedId = AnalyticsPrivacySanitizer.sanitizeValue(userId) as? String

        for (provider in providers) {
            if (!provider.isEnabled) continue
            try {
                provider.setUserId(sanitizedId)
            } catch (_: Throwable) {
                // Safe error suppression
            }
        }
    }

    private fun prepareEvent(event: AnalyticsEvent): AnalyticsEvent {
        // 1. Deep privacy sanitization & data minimization (Sprint 20.4)
        val deeplySanitized = AnalyticsPrivacySanitizer.sanitize(event)
        val sanitizedProperties = deeplySanitized.properties.toMutableMap()

        // 2. Ambient environment attribution (REQ-AN-014)
        if (!sanitizedProperties.containsKey(AnalyticsEvent.PARAM_ENVIRONMENT)) {
            val envString = when (environmentProvider.current) {
                AppEnvironment.Development -> "development"
                AppEnvironment.Testing -> "testing"
                AppEnvironment.Production -> "production"
            }
            sanitizedProperties[AnalyticsEvent.PARAM_ENVIRONMENT] = envString
        }

        return deeplySanitized.copy(properties = sanitizedProperties)
    }
}
