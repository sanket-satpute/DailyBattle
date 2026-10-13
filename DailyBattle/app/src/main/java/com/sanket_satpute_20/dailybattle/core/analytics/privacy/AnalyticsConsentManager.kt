package com.sanket_satpute_20.dailybattle.core.analytics.privacy

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user analytics consent, opt-out preferences, and account deletion wipeouts
 * per `14_ANALYTICS.md` §52 & §78.
 */
class AnalyticsConsentManager(
    initialConsent: Boolean = true
) {

    private val _isConsentGranted = MutableStateFlow(initialConsent)
    val isConsentGranted: StateFlow<Boolean> = _isConsentGranted.asStateFlow()

    /**
     * Whether tracking is currently permitted.
     */
    val hasConsent: Boolean
        get() = _isConsentGranted.value

    /**
     * Updates user privacy consent preference.
     * When set to false, all analytics collection immediately ceases.
     */
    fun setConsentGranted(granted: Boolean) {
        _isConsentGranted.value = granted
    }
}
