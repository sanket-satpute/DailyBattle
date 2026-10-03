package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * The mode of a Battle Session.
 *
 * Per `08_DATA_AND_API.md` §17, the system must explicitly distinguish:
 * - [OFFICIAL] — the one-per-day official attempt that produces the scored result
 * - [PRACTICE] — a repeatable attempt that does not affect official scoring
 *
 * These values must not be inferred from screen navigation.
 */
enum class BattleMode {
    OFFICIAL,
    PRACTICE,
}
