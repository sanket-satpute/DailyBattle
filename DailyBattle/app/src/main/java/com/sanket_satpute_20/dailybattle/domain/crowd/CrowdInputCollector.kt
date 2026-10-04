package com.sanket_satpute_20.dailybattle.domain.crowd

import com.sanket_satpute_20.dailybattle.core.error.AppError
import com.sanket_satpute_20.dailybattle.domain.result.DomainResult

/**
 * Collects raw inputs for a Crowd Call challenge.
 *
 * Since DEC-GAME-003 is PENDING, we stub the actual question and distribution,
 * but enforce the conceptual state flow:
 * NotStarted -> Question -> PredictionResult -> Completed
 */
class CrowdInputCollector(
    private val timeProvider: () -> Long = { System.currentTimeMillis() }
) : CrowdEngine {

    private var _currentState: CrowdState = CrowdState.NotStarted
    override val currentState: CrowdState
        get() = _currentState

    private var challengeStartTime: Long? = null
    private var challengeDurationMs: Long = 0L
    private var selectedChoiceId: String? = null
    
    // Stub question
    private val stubQuestion = CrowdQuestion(
        id = "q_stub_1",
        text = "You suddenly get ₹500 tonight. What would most people choose?",
        choices = listOf(
            CrowdChoice("c1", "Movie"),
            CrowdChoice("c2", "Food + Hangout"),
            CrowdChoice("c3", "Gaming"),
            CrowdChoice("c4", "Save it")
        )
    )
    
    // Stub distribution
    private val stubDistribution = CrowdDistribution(
        percentages = mapOf(
            "c1" to 19,
            "c2" to 36,
            "c3" to 27,
            "c4" to 18
        )
    )

    override fun prepare(): DomainResult<CrowdState> {
        if (_currentState !is CrowdState.NotStarted) {
            return DomainResult.Failure(AppError.BattleState)
        }
        _currentState = CrowdState.Question(stubQuestion)
        return DomainResult.Success(_currentState)
    }

    override fun start(): DomainResult<CrowdState> {
        if (_currentState !is CrowdState.Question) {
            return DomainResult.Failure(AppError.BattleState)
        }
        challengeStartTime = timeProvider()
        return DomainResult.Success(_currentState)
    }

    override fun handleInput(event: CrowdEvent): DomainResult<CrowdState> {
        when (event) {
            CrowdEvent.Start -> {
                return start()
            }
            is CrowdEvent.SubmitPrediction -> {
                if (_currentState !is CrowdState.Question) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                selectedChoiceId = event.choiceId
                val timeTaken = if (challengeStartTime != null) event.timestampMs - challengeStartTime!! else 0L
                challengeDurationMs = timeTaken
                
                // For stub, consider 'c2' (Food + Hangout) as majority.
                val isMajority = event.choiceId == "c2"
                
                _currentState = CrowdState.PredictionResult(
                    question = stubQuestion,
                    selectedChoiceId = event.choiceId,
                    distribution = stubDistribution,
                    isMajorityChosen = isMajority
                )
            }
            CrowdEvent.Timeout -> {
                if (_currentState !is CrowdState.Question) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                selectedChoiceId = null
                challengeDurationMs = if (challengeStartTime != null) timeProvider() - challengeStartTime!! else 0L
                
                _currentState = CrowdState.PredictionResult(
                    question = stubQuestion,
                    selectedChoiceId = null,
                    distribution = stubDistribution,
                    isMajorityChosen = false
                )
            }
            CrowdEvent.Continue -> {
                if (_currentState !is CrowdState.PredictionResult) {
                    return DomainResult.Failure(AppError.BattleState)
                }
                return complete()
            }
        }
        return DomainResult.Success(_currentState)
    }

    override fun evaluate(): RawCrowdResult? {
        if (challengeStartTime == null) return null
        return RawCrowdResult(
            questionId = stubQuestion.id,
            selectedChoiceId = selectedChoiceId,
            timeToAnswerMs = challengeDurationMs
        )
    }

    override fun complete(): DomainResult<CrowdState> {
        if (_currentState is CrowdState.Completed) {
            return DomainResult.Failure(AppError.BattleState)
        }
        
        val result = evaluate() ?: return DomainResult.Failure(AppError.BattleState)
        _currentState = CrowdState.Completed(result)
        return DomainResult.Success(_currentState)
    }
}
