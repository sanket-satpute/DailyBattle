package com.sanket_satpute_20.dailybattle.core.analytics

import com.sanket_satpute_20.dailybattle.core.analytics.privacy.AnalyticsConsentManager
import com.sanket_satpute_20.dailybattle.core.analytics.privacy.AnalyticsPrivacySanitizer
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.AnalyticsReliabilityManager
import com.sanket_satpute_20.dailybattle.core.analytics.reliability.InMemoryAnalyticsQueueStore
import com.sanket_satpute_20.dailybattle.core.config.AppEnvironment
import com.sanket_satpute_20.dailybattle.core.config.EnvironmentProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verification of Sprint 20.4 — Privacy.
 *
 * Verifies:
 * - Data minimization (REQ-AN-005, §53)
 * - No unnecessary personal data (PII, hardware IDs, location) (§46, §48, §56)
 * - No sensitive data leakage (Credit cards, passwords, PINs) (§54)
 * - No credentials/tokens in analytics (JWT, auth tokens, API secrets) (§54)
 * - Battle Code privacy (§55, REQ-AN-013)
 * - User consent controls & right-to-erasure account deletion wipeout (§52, §78)
 */
class AnalyticsPrivacyTest {

    private lateinit var inMemoryProvider: InMemoryAnalyticsProvider
    private lateinit var queueStore: InMemoryAnalyticsQueueStore
    private lateinit var consentManager: AnalyticsConsentManager
    private lateinit var tracker: DefaultAnalyticsTracker

    private class FakeEnv : EnvironmentProvider {
        override val current: AppEnvironment = AppEnvironment.Production
    }

    @Before
    fun setup() {
        inMemoryProvider = InMemoryAnalyticsProvider()
        queueStore = InMemoryAnalyticsQueueStore()
        consentManager = AnalyticsConsentManager(initialConsent = true)
        val reliabilityManager = AnalyticsReliabilityManager(queueStore = queueStore)
        tracker = DefaultAnalyticsTracker(
            environmentProvider = FakeEnv(),
            initialProviders = listOf(inMemoryProvider),
            reliabilityManager = reliabilityManager,
            consentManager = consentManager
        )
    }

    // ============================================================================
    // 1. Data Minimization
    // ============================================================================

    @Test
    fun `privacy - long property values are bounded and truncated`() {
        val longString = "A".repeat(800)
        tracker.track("error_occurred", mapOf("diagnostic_dump" to longString))

        val event = inMemoryProvider.getRecordedEvents().first()
        val dumpValue = event.properties["diagnostic_dump"] as String

        assertTrue("Value length should be capped", dumpValue.length <= 520)
        assertTrue("Value should indicate truncation", dumpValue.endsWith("...[TRUNCATED]"))
    }

    @Test
    fun `privacy - excessive property count is capped per event`() {
        val properties = (1..50).associate { "prop_$it" to "val_$it" }
        tracker.track("bulk_event", properties)

        val event = inMemoryProvider.getRecordedEvents().first()
        assertTrue(event.properties.size <= AnalyticsPrivacySanitizer.MAX_PROPERTY_COUNT + 1) // +1 for environment
    }

    // ============================================================================
    // 2. No Unnecessary Personal Data (PII, Hardware IDs, Location)
    // ============================================================================

    @Test
    fun `privacy - email addresses in property values are redacted`() {
        tracker.track(
            "user_feedback_sent",
            mapOf("message" to "Contact me at user.john@example.com for more info")
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        val message = event.properties["message"] as String

        assertFalse("Raw email must not be present", message.contains("user.john@example.com"))
        assertTrue("Email must be replaced with placeholder", message.contains("[REDACTED]"))
    }

    @Test
    fun `privacy - phone numbers in property values are redacted`() {
        tracker.track(
            "support_requested",
            mapOf("note" to "My phone is +1-555-123-4567 please call")
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        val note = event.properties["note"] as String

        assertFalse("Raw phone number must not be present", note.contains("555-123-4567"))
        assertTrue(note.contains("[REDACTED]"))
    }

    @Test
    fun `privacy - PII keys (email, phone, address, ssn) are dropped from events`() {
        tracker.track(
            "profile_updated",
            mapOf(
                "email" to "john@example.com",
                "phone" to "555-0199",
                "address" to "123 Main St",
                "ssn" to "000-11-2222",
                "theme" to "dark"
            )
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        assertFalse(event.properties.containsKey("email"))
        assertFalse(event.properties.containsKey("phone"))
        assertFalse(event.properties.containsKey("address"))
        assertFalse(event.properties.containsKey("ssn"))
        assertEquals("dark", event.properties["theme"])
    }

    @Test
    fun `privacy - hardware identifiers (imei, mac, android_id) are dropped`() {
        tracker.track(
            "device_registered",
            mapOf(
                "imei" to "354123456789012",
                "mac_address" to "00:1A:2B:3C:4D:5E",
                "android_id" to "9774d56d682e549c",
                "serial_number" to "ZX1G22",
                "screen_size" to "1080x2400"
            )
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        assertFalse(event.properties.containsKey("imei"))
        assertFalse(event.properties.containsKey("mac_address"))
        assertFalse(event.properties.containsKey("android_id"))
        assertFalse(event.properties.containsKey("serial_number"))
        assertEquals("1080x2400", event.properties["screen_size"])
    }

    @Test
    fun `privacy - exact location coordinates (lat, lng, gps) are dropped`() {
        tracker.track(
            "region_selected",
            mapOf(
                "latitude" to 37.7749,
                "longitude" to -122.4194,
                "gps" to "37.7749,-122.4194",
                "country_code" to "US"
            )
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        assertFalse(event.properties.containsKey("latitude"))
        assertFalse(event.properties.containsKey("longitude"))
        assertFalse(event.properties.containsKey("gps"))
        assertEquals("US", event.properties["country_code"])
    }

    // ============================================================================
    // 3. No Sensitive Data Leakage (Credit Cards, Passwords, PINs)
    // ============================================================================

    @Test
    fun `privacy - credit card numbers in values are redacted`() {
        tracker.track(
            "checkout_error",
            mapOf("error_details" to "Card 4111 2222 3333 4444 was declined")
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        val details = event.properties["error_details"] as String

        assertFalse("Credit card must not leak", details.contains("4111 2222 3333 4444"))
        assertTrue(details.contains("[REDACTED]"))
    }

    @Test
    fun `privacy - password and pin keys are dropped from events and user properties`() {
        tracker.track("login_attempted", mapOf("password" to "SuperSecret!", "pin" to "1234"))
        tracker.setUserProperty("password", "Secret123")
        tracker.setUserProperty("user_pin", "4321")

        val event = inMemoryProvider.getRecordedEvents().first()
        assertFalse(event.properties.containsKey("password"))
        assertFalse(event.properties.containsKey("pin"))

        assertFalse(inMemoryProvider.getUserProperties().containsKey("password"))
        assertFalse(inMemoryProvider.getUserProperties().containsKey("user_pin"))
    }

    // ============================================================================
    // 4. No Credentials / Tokens in Analytics
    // ============================================================================

    @Test
    fun `privacy - JWT and Bearer tokens in values are redacted`() {
        val fakeJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dozG4B_d_fake_signature_here"
        tracker.track("api_call_logged", mapOf("headers" to "Authorization: Bearer $fakeJwt"))

        val event = inMemoryProvider.getRecordedEvents().first()
        val headers = event.properties["headers"] as String

        assertFalse("JWT token must not leak", headers.contains(fakeJwt))
        assertTrue(headers.contains("[REDACTED]"))
    }

    @Test
    fun `privacy - auth and api key keys are dropped from events`() {
        tracker.track(
            "request_dispatched",
            mapOf(
                "auth_token" to "token_123",
                "access_token" to "access_456",
                "refresh_token" to "refresh_789",
                "api_key" to "key_abc",
                "endpoint" to "/battle/start"
            )
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        assertFalse(event.properties.containsKey("auth_token"))
        assertFalse(event.properties.containsKey("access_token"))
        assertFalse(event.properties.containsKey("refresh_token"))
        assertFalse(event.properties.containsKey("api_key"))
        assertEquals("/battle/start", event.properties["endpoint"])
    }

    // ============================================================================
    // 5. Battle Code Privacy (REQ-AN-013, §55)
    // ============================================================================

    @Test
    fun `privacy - raw battle codes are never tracked in events`() {
        tracker.track(
            "friend_interaction",
            mapOf(
                "battle_code" to "CODE99",
                "friend_code" to "ABC123",
                "invite_code" to "INVITE",
                "status" to "accepted"
            )
        )

        val event = inMemoryProvider.getRecordedEvents().first()
        assertFalse(event.properties.containsKey("battle_code"))
        assertFalse(event.properties.containsKey("friend_code"))
        assertFalse(event.properties.containsKey("invite_code"))
        assertEquals("accepted", event.properties["status"])
    }

    // ============================================================================
    // 6. User Consent Controls & Account Deletion Wipeout (§52, §78)
    // ============================================================================

    @Test
    fun `privacy - revoked consent completely halts all event tracking`() {
        tracker.track("event_with_consent")
        assertEquals(1, inMemoryProvider.getRecordedEvents().size)

        // User revokes consent / opts out
        tracker.setConsentGranted(false)
        assertFalse(tracker.isConsentGranted.value)

        // All subsequent calls must be ignored
        tracker.track("event_without_consent")
        tracker.trackBattleStarted("b-1", "s-1")
        tracker.setUserProperty("theme", "light")
        tracker.setUserId("user-should-not-track")

        assertEquals(0, inMemoryProvider.getRecordedEvents().size) // Purged on consent revoke
        assertNull(inMemoryProvider.getUserId())
        assertTrue(inMemoryProvider.getUserProperties().isEmpty())
    }

    @Test
    fun `privacy - clearUserData wipes user id, in-memory providers, and queue store (§78)`() {
        tracker.setUserId("user-persistent")
        tracker.setUserProperty("preferred_language", "en")
        tracker.track("pre_delete_event")

        // Populate offline queue
        tracker.setOnline(false)
        tracker.track("queued_event")
        assertEquals(1, queueStore.size())

        // User requests account deletion or data wipe
        tracker.clearUserData()

        assertNull(inMemoryProvider.getUserId())
        assertTrue(inMemoryProvider.getUserProperties().isEmpty())
        assertTrue(inMemoryProvider.getRecordedEvents().isEmpty())
        assertEquals(0, queueStore.size())
    }
}
