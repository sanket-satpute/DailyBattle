package com.sanket_satpute_20.dailybattle.domain.history

import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultRepository
import com.sanket_satpute_20.dailybattle.domain.identifier.UserId
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: BattleResultRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(userId: UserId): DomainResult<History> {
        return when (val result = repository.getUserBattleResults(userId)) {
            is DomainResult.Failure -> DomainResult.Failure(result.error)
            is DomainResult.Success -> {
                val officialBattles = result.value
                    .sortedByDescending { it.completedAt } // Most recent first

                if (officialBattles.isEmpty()) {
                    return DomainResult.Success(
                        History(
                            best = 0,
                            average = 0,
                            momentum = 0,
                            recentBattles = emptyList(),
                            graphData = emptyList()
                        )
                    )
                }

                val best = officialBattles.maxOf { it.totalScore }
                val average = officialBattles.map { it.totalScore }.average().toInt()
                
                // Calculate Momentum
                val zoneId = clock.zone ?: ZoneId.systemDefault()
                val today = LocalDate.now(clock)
                
                // Extract distinct calendar dates from completed official battles, sorted descending
                val qualifyingDates = officialBattles
                    .map { Instant.ofEpochMilli(it.completedAt).atZone(zoneId).toLocalDate() }
                    .distinct()
                    .sortedDescending()

                var momentum = 0
                var currentDateToCheck = today
                
                for (date in qualifyingDates) {
                    if (date == currentDateToCheck) {
                        momentum++
                        currentDateToCheck = currentDateToCheck.minusDays(1)
                    } else if (date.isBefore(currentDateToCheck)) {
                        // A gap exists, momentum breaks
                        break
                    }
                    // if date is after currentDateToCheck, it should be ignored (e.g. data from future somehow)
                }

                DomainResult.Success(
                    History(
                        best = best,
                        average = average,
                        momentum = momentum,
                        recentBattles = officialBattles.take(10), // Limit recent
                        graphData = officialBattles.sortedBy { it.completedAt } // Chronological for graph
                    )
                )
            }
        }
    }
}
