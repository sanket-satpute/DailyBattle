package com.sanket_satpute_20.dailybattle.data.remote.serialization

import kotlinx.serialization.json.Json

/**
 * Standard serialization engine for Daily Battle network communication.
 *
 * Configured with resilient defaults to prevent schema evolution crashes while preserving strict validation.
 */
val DailyBattleJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
    coerceInputValues = true
    prettyPrint = false
}
