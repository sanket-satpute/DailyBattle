package com.sanket_satpute_20.dailybattle.testing

import com.sanket_satpute_20.dailybattle.domain.identifier.BattleId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.BattleSessionId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeId
import com.sanket_satpute_20.dailybattle.domain.identifier.ChallengeResultId
import com.sanket_satpute_20.dailybattle.domain.identifier.FriendshipId
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId

/**
 * Deterministic, explicitly-labeled test identifiers (see `12_TESTING_AND_QA.md` section 43/44).
 * No production data, format, or generation rule is implied by these values.
 */
fun testUserId(suffix: Int = 1): UserId = UserId("test-user-$suffix")

fun testBattleId(suffix: Int = 1): BattleId = BattleId("test-battle-$suffix")

fun testBattleSessionId(suffix: Int = 1): BattleSessionId = BattleSessionId("test-battle-session-$suffix")

fun testChallengeId(suffix: Int = 1): ChallengeId = ChallengeId("test-challenge-$suffix")

fun testChallengeResultId(suffix: Int = 1): ChallengeResultId = ChallengeResultId("test-challenge-result-$suffix")

fun testBattleResultId(suffix: Int = 1): BattleResultId = BattleResultId("test-battle-result-$suffix")

fun testFriendshipId(suffix: Int = 1): FriendshipId = FriendshipId("test-friendship-$suffix")
