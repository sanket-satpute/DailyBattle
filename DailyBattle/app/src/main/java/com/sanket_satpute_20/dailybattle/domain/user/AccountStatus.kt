package com.sanket_satpute_20.dailybattle.domain.user

/**
 * Lifecycle state of a user account.
 *
 * Per `08_DATA_AND_API.md` §6, the `accountStatus` field is an enum whose exact
 * values are PENDING (authority = backend). The conceptual values below cover the
 * minimum lifecycle required by the MVP product flow:
 *
 * - [ACTIVE]      — normal operational state
 * - [ONBOARDING]  — user has not yet completed identity setup (Battle Name)
 * - [SUSPENDED]   — account has been suspended by the backend
 * - [DELETED]     — account deletion has been completed
 *
 * Additional states (e.g. banned, locked) may be added when the backend
 * specification is approved. Values must not be invented without approval.
 */
enum class AccountStatus {
    ONBOARDING,
    ACTIVE,
    SUSPENDED,
    DELETED,
}
