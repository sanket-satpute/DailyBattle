package com.sanket_satpute_20.dailybattle.data.sync

/**
 * Supported synchronization operation types across battle, social, and profile domains.
 *
 * Operations preserve logical ordering (§49):
 * [START_BATTLE] -> [SUBMIT_CHALLENGE_RESULT] -> [COMPLETE_BATTLE].
 */
enum class SyncOperationType {
    START_BATTLE,
    SUBMIT_CHALLENGE_RESULT,
    COMPLETE_BATTLE,
    ADD_FRIEND,
    REFRESH_PROFILE
}
