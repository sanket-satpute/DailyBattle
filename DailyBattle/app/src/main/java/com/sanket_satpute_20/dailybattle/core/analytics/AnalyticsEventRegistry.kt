package com.sanket_satpute_20.dailybattle.core.analytics

/**
 * Formal event registry implementing `14_ANALYTICS.md` §87 & §88.
 *
 * Provides governance, traceability, and validation for all approved production events across the 11 categories.
 */
object AnalyticsEventRegistry {

    enum class EventCategory {
        APPLICATION,
        ONBOARDING,
        BATTLE,
        CHALLENGE,
        RESULTS,
        RIVAL,
        FRIENDS,
        PRACTICE,
        ERROR,
        NETWORK,
        PERFORMANCE
    }

    enum class BattleModeRequirement {
        NONE,
        OFFICIAL_ONLY,
        PRACTICE_ONLY,
        EITHER
    }

    enum class PrivacyClassification {
        PUBLIC_PRODUCT_METRIC,
        INTERNAL_OPERATIONAL,
        RESTRICTED_DATA_MINIMIZED
    }

    data class EventDefinition(
        val name: String,
        val category: EventCategory,
        val description: String,
        val requiredProperties: Set<String> = emptySet(),
        val optionalProperties: Set<String> = emptySet(),
        val modeRequirement: BattleModeRequirement = BattleModeRequirement.NONE,
        val privacyClassification: PrivacyClassification = PrivacyClassification.INTERNAL_OPERATIONAL
    )

    private val registry: Map<String, EventDefinition> = buildMap {
        fun register(def: EventDefinition) {
            put(def.name, def)
        }

        // 1. Application Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_APP_OPENED,
                category = EventCategory.APPLICATION,
                description = "User opens the application or brings it into foreground",
                optionalProperties = setOf(AnalyticsEvent.PARAM_APP_VERSION, AnalyticsEvent.PARAM_ENVIRONMENT),
                privacyClassification = PrivacyClassification.PUBLIC_PRODUCT_METRIC
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_APP_BACKGROUNDED,
                category = EventCategory.APPLICATION,
                description = "Application enters background state",
                optionalProperties = setOf(AnalyticsEvent.PARAM_ENVIRONMENT)
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_APP_FOREGROUNDED,
                category = EventCategory.APPLICATION,
                description = "Application returns to foreground from background state",
                optionalProperties = setOf(AnalyticsEvent.PARAM_ENVIRONMENT)
            )
        )

        // 2. Onboarding Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_WELCOME_VIEWED,
                category = EventCategory.ONBOARDING,
                description = "User views the onboarding welcome screen"
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_NAME_STARTED,
                category = EventCategory.ONBOARDING,
                description = "User initiates battle name selection during onboarding"
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_NAME_COMPLETED,
                category = EventCategory.ONBOARDING,
                description = "User confirms battle name selection during onboarding"
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_ONBOARDING_COMPLETED,
                category = EventCategory.ONBOARDING,
                description = "User completes the initial onboarding flow",
                privacyClassification = PrivacyClassification.PUBLIC_PRODUCT_METRIC
            )
        )

        // 3. Battle Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_VIEWED,
                category = EventCategory.BATTLE,
                description = "User views today's battle card on Home",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_BATTLE_ID, CoreAnalyticsEvents.PARAM_BATTLE_MODE),
                modeRequirement = BattleModeRequirement.EITHER
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_STARTED,
                category = EventCategory.BATTLE,
                description = "Battle session started",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_BATTLE_ID,
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                modeRequirement = BattleModeRequirement.EITHER,
                privacyClassification = PrivacyClassification.PUBLIC_PRODUCT_METRIC
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_COMPLETED,
                category = EventCategory.BATTLE,
                description = "Battle session finished with official or practice result",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_BATTLE_ID,
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_TOTAL_SCORE,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_DURATION_MS),
                modeRequirement = BattleModeRequirement.EITHER,
                privacyClassification = PrivacyClassification.PUBLIC_PRODUCT_METRIC
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_ABANDONED,
                category = EventCategory.BATTLE,
                description = "Battle abandoned before all challenges complete",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_BATTLE_ID,
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_REASON),
                modeRequirement = BattleModeRequirement.EITHER
            )
        )

        // 4. Challenge Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_CHALLENGE_STARTED,
                category = EventCategory.CHALLENGE,
                description = "Challenge gameplay started within a session",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                modeRequirement = BattleModeRequirement.EITHER
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_CHALLENGE_COMPLETED,
                category = EventCategory.CHALLENGE,
                description = "Challenge gameplay completed with score",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX,
                    CoreAnalyticsEvents.PARAM_SCORE,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_DURATION_MS),
                modeRequirement = BattleModeRequirement.EITHER
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_CHALLENGE_ABANDONED,
                category = EventCategory.CHALLENGE,
                description = "Challenge gameplay abandoned before completion",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_REASON),
                modeRequirement = BattleModeRequirement.EITHER
            )
        )

        // 5. Results Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_RESULTS_VIEWED,
                category = EventCategory.RESULTS,
                description = "Results screen displayed to user",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_BATTLE_ID,
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_TOTAL_SCORE
                ),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_PERCENTILE),
                privacyClassification = PrivacyClassification.PUBLIC_PRODUCT_METRIC
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_SCORE_REVEALED,
                category = EventCategory.RESULTS,
                description = "Challenge score animation revealed on Results screen",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_SCORE
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_RIVAL_COMPARISON_VIEWED,
                category = EventCategory.RESULTS,
                description = "Rival comparison module revealed on Results screen",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_USER_SCORE,
                    CoreAnalyticsEvents.PARAM_RIVAL_SCORE,
                    CoreAnalyticsEvents.PARAM_SCORE_GAP
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_SHARE_STARTED,
                category = EventCategory.RESULTS,
                description = "Share card export/intent initiated",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_BATTLE_ID,
                    CoreAnalyticsEvents.PARAM_TOTAL_SCORE
                )
            )
        )

        // 6. Rival Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_RIVAL_VIEWED,
                category = EventCategory.RIVAL,
                description = "Rival card viewed on Home screen",
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_RIVAL_ID, CoreAnalyticsEvents.PARAM_SCORE_GAP)
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_RIVAL_BATTLE_STARTED,
                category = EventCategory.RIVAL,
                description = "Battle started directly from rival context",
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_RIVAL_ID)
            )
        )

        // 7. Friends Events (Strict data minimization: zero raw battle codes or private contact data)
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_FRIENDS_VIEWED,
                category = EventCategory.FRIENDS,
                description = "Friends hub viewed by user",
                privacyClassification = PrivacyClassification.RESTRICTED_DATA_MINIMIZED
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_ADD_FRIEND_STARTED,
                category = EventCategory.FRIENDS,
                description = "Add friend / Battle code entry sheet opened",
                privacyClassification = PrivacyClassification.RESTRICTED_DATA_MINIMIZED
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_FRIEND_ADDED,
                category = EventCategory.FRIENDS,
                description = "Friend successfully added (without raw code)",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_STATUS),
                privacyClassification = PrivacyClassification.RESTRICTED_DATA_MINIMIZED
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_FRIEND_REQUEST_FAILED,
                category = EventCategory.FRIENDS,
                description = "Friend addition attempt failed",
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_ERROR_CODE),
                privacyClassification = PrivacyClassification.RESTRICTED_DATA_MINIMIZED
            )
        )

        // 8. Practice Events (Mode is ALWAYS practice)
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_PRACTICE_VIEWED,
                category = EventCategory.PRACTICE,
                description = "Practice mode hub viewed",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_BATTLE_MODE),
                modeRequirement = BattleModeRequirement.PRACTICE_ONLY
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_PRACTICE_STARTED,
                category = EventCategory.PRACTICE,
                description = "Practice session started",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                modeRequirement = BattleModeRequirement.PRACTICE_ONLY
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_PRACTICE_CHALLENGE_STARTED,
                category = EventCategory.PRACTICE,
                description = "Practice challenge gameplay started",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                modeRequirement = BattleModeRequirement.PRACTICE_ONLY
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_PRACTICE_CHALLENGE_COMPLETED,
                category = EventCategory.PRACTICE,
                description = "Practice challenge completed with score",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_CHALLENGE_INDEX,
                    CoreAnalyticsEvents.PARAM_SCORE,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                modeRequirement = BattleModeRequirement.PRACTICE_ONLY
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_PRACTICE_COMPLETED,
                category = EventCategory.PRACTICE,
                description = "Practice session completed",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SESSION_ID,
                    CoreAnalyticsEvents.PARAM_TOTAL_SCORE,
                    CoreAnalyticsEvents.PARAM_BATTLE_MODE
                ),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_DURATION_MS),
                modeRequirement = BattleModeRequirement.PRACTICE_ONLY
            )
        )

        // 9. Error Events (Sanitized against secrets/credentials)
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_START_FAILED,
                category = EventCategory.ERROR,
                description = "Failure preventing battle session launch",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_RETRYABLE),
                optionalProperties = setOf(
                    CoreAnalyticsEvents.PARAM_ERROR_CATEGORY,
                    CoreAnalyticsEvents.PARAM_ERROR_CODE
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_CHALLENGE_LOAD_FAILED,
                category = EventCategory.ERROR,
                description = "Failure loading challenge assets or content",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_RETRYABLE),
                optionalProperties = setOf(
                    CoreAnalyticsEvents.PARAM_CHALLENGE_TYPE,
                    CoreAnalyticsEvents.PARAM_ERROR_CODE
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_SUBMISSION_FAILED,
                category = EventCategory.ERROR,
                description = "Failure submitting battle result to server/storage",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_RETRYABLE),
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_ERROR_CODE)
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_FRIEND_ACTION_FAILED,
                category = EventCategory.ERROR,
                description = "Failure performing friend-related operation",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_RETRYABLE),
                optionalProperties = setOf(
                    CoreAnalyticsEvents.PARAM_OPERATION,
                    CoreAnalyticsEvents.PARAM_ERROR_CODE
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_DATA_LOAD_FAILED,
                category = EventCategory.ERROR,
                description = "Failure loading local or remote cached data",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_RETRYABLE),
                optionalProperties = setOf(
                    CoreAnalyticsEvents.PARAM_OPERATION,
                    CoreAnalyticsEvents.PARAM_ERROR_CODE
                )
            )
        )

        // 10. Network Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_NETWORK_UNAVAILABLE,
                category = EventCategory.NETWORK,
                description = "Network connectivity lost or unavailable during operation"
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_REQUEST_TIMEOUT,
                category = EventCategory.NETWORK,
                description = "Network request timed out",
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_ENDPOINT)
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_REQUEST_FAILED,
                category = EventCategory.NETWORK,
                description = "HTTP request returned error status code",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_RETRYABLE),
                optionalProperties = setOf(
                    CoreAnalyticsEvents.PARAM_ENDPOINT,
                    CoreAnalyticsEvents.PARAM_ERROR_CODE
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_REQUEST_RECOVERED,
                category = EventCategory.NETWORK,
                description = "Network connectivity and request recovered after failure",
                optionalProperties = setOf(CoreAnalyticsEvents.PARAM_ENDPOINT)
            )
        )

        // 11. Performance Events
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_APP_STARTUP,
                category = EventCategory.PERFORMANCE,
                description = "Total cold startup duration until interactive",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_DURATION_MS)
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_SCREEN_LOAD,
                category = EventCategory.PERFORMANCE,
                description = "Time to render screen from navigation start",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_SCREEN_NAME,
                    CoreAnalyticsEvents.PARAM_DURATION_MS
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_BATTLE_LOAD,
                category = EventCategory.PERFORMANCE,
                description = "Duration to initialize battle gameplay engine and state",
                requiredProperties = setOf(CoreAnalyticsEvents.PARAM_DURATION_MS)
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_CHALLENGE_TRANSITION,
                category = EventCategory.PERFORMANCE,
                description = "Duration of transition between consecutive challenges",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_FROM_INDEX,
                    CoreAnalyticsEvents.PARAM_TO_INDEX,
                    CoreAnalyticsEvents.PARAM_DURATION_MS
                )
            )
        )
        register(
            EventDefinition(
                name = CoreAnalyticsEvents.EVENT_API_LATENCY,
                category = EventCategory.PERFORMANCE,
                description = "Round-trip network latency for critical endpoints",
                requiredProperties = setOf(
                    CoreAnalyticsEvents.PARAM_ENDPOINT,
                    CoreAnalyticsEvents.PARAM_DURATION_MS
                )
            )
        )
    }

    /**
     * Checks if the given event name is registered in the official registry.
     */
    fun isRegistered(eventName: String): Boolean = registry.containsKey(eventName)

    /**
     * Retrieves the event definition for the given event name, or null if unregistered.
     */
    fun getDefinition(eventName: String): EventDefinition? = registry[eventName]

    /**
     * Returns an unmodifiable list of all registered event definitions.
     */
    fun getAllRegisteredEvents(): List<EventDefinition> = registry.values.toList()

    /**
     * Returns all registered events in a specific category.
     */
    fun getEventsByCategory(category: EventCategory): List<EventDefinition> =
        registry.values.filter { it.category == category }

    /**
     * Validates an event against registry requirements:
     * - Event must be registered
     * - Must contain all required properties
     * - Must satisfy mode requirement (e.g. practice events must have battle_mode = "practice")
     * - Must not contain forbidden sensitive keys
     */
    fun validateEvent(event: AnalyticsEvent): EventValidationResult {
        val def = registry[event.name]
            ?: return EventValidationResult.Unregistered(event.name)

        val missing = def.requiredProperties.filter { !event.properties.containsKey(it) }
        if (missing.isNotEmpty()) {
            return EventValidationResult.MissingRequiredProperties(event.name, missing.toSet())
        }

        // Mode validation (REQ-AN-002)
        when (def.modeRequirement) {
            BattleModeRequirement.PRACTICE_ONLY -> {
                val mode = event.properties[AnalyticsEvent.PARAM_BATTLE_MODE]
                if (mode != AnalyticsEvent.VALUE_MODE_PRACTICE) {
                    return EventValidationResult.InvalidMode(
                        event.name,
                        expected = AnalyticsEvent.VALUE_MODE_PRACTICE,
                        actual = mode?.toString()
                    )
                }
            }
            BattleModeRequirement.OFFICIAL_ONLY -> {
                val mode = event.properties[AnalyticsEvent.PARAM_BATTLE_MODE]
                if (mode != AnalyticsEvent.VALUE_MODE_OFFICIAL) {
                    return EventValidationResult.InvalidMode(
                        event.name,
                        expected = AnalyticsEvent.VALUE_MODE_OFFICIAL,
                        actual = mode?.toString()
                    )
                }
            }
            BattleModeRequirement.EITHER -> {
                val mode = event.properties[AnalyticsEvent.PARAM_BATTLE_MODE]
                if (mode != AnalyticsEvent.VALUE_MODE_OFFICIAL && mode != AnalyticsEvent.VALUE_MODE_PRACTICE) {
                    return EventValidationResult.InvalidMode(
                        event.name,
                        expected = "official or practice",
                        actual = mode?.toString()
                    )
                }
            }
            BattleModeRequirement.NONE -> {
                // No mode requirement
            }
        }

        // Privacy check (REQ-AN-005, REQ-AN-013)
        val sensitiveKeysFound = event.properties.keys.filter {
            it.lowercase() in AnalyticsEvent.FORBIDDEN_SENSITIVE_KEYS
        }
        if (sensitiveKeysFound.isNotEmpty()) {
            return EventValidationResult.SensitiveKeysFound(event.name, sensitiveKeysFound.toSet())
        }

        return EventValidationResult.Valid
    }
}

/**
 * Result of validating an event against [AnalyticsEventRegistry].
 */
sealed interface EventValidationResult {
    object Valid : EventValidationResult
    data class Unregistered(val eventName: String) : EventValidationResult
    data class MissingRequiredProperties(val eventName: String, val missing: Set<String>) : EventValidationResult
    data class InvalidMode(val eventName: String, val expected: String, val actual: String?) : EventValidationResult
    data class SensitiveKeysFound(val eventName: String, val sensitiveKeys: Set<String>) : EventValidationResult
}
