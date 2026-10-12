package com.sanket_satpute_20.dailybattle.domain.authority

/**
 * Functional domains in the Daily Battle architecture for which authoritative
 * ownership and synchronization behavior are defined.
 *
 * Source: 08_DATA_AND_API.md §60, 07_TECHNICAL_ARCHITECTURE.md §12.
 */
enum class DomainType {
    /** User device preferences (audio, haptics, notifications, theme). */
    USER_SETTINGS,

    /** Live, in-progress battle session and live challenge progression. */
    ACTIVE_BATTLE_SESSION,

    /** Official daily battle definition and challenge puzzle compositions. */
    OFFICIAL_BATTLE_DEFINITION,

    /** Completed official battle result with verified competitive scoring. */
    OFFICIAL_BATTLE_RESULT,

    /** Casual practice session execution and practice scores. */
    PRACTICE_SESSION,

    /** User profile metadata, player battle name, and aggregate records. */
    USER_PROFILE,

    /** Multi-dimensional Battle DNA player attributes (speed, memory, people, endurance). */
    BATTLE_DNA,

    /** Consecutive daily official battle completion streak / momentum. */
    MOMENTUM_STREAK,

    /** Friend list, friendship relations, and friend battle scores. */
    FRIENDS,

    /** Daily server-assigned rival and score comparison. */
    RIVAL,

    /** Historical official battle completion archive. */
    BATTLE_HISTORY,

    /** Ephemeral UI presentation state, dialog buffers, and animation states. */
    TRANSIENT_UI,
}
