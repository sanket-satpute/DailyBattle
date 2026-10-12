package com.sanket_satpute_20.dailybattle.domain.authority

/**
 * Physical local storage tier where domain data is held on the Android client.
 */
enum class StorageTier {
    /** Preferences DataStore (key-value, non-relational settings). */
    DATASTORE,

    /** Room SQLite relational database (structured entities). */
    ROOM,

    /** In-memory state holder or volatile manager (ephemeral/in-flight). */
    IN_MEMORY,

    /** Not stored locally (remote only). */
    REMOTE_ONLY,
}
