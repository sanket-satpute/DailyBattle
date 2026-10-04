package com.sanket_satpute_20.dailybattle.domain.battle

/**
 * Composite model representing the complete result experience for a Battle,
 * aggregating the official result and derived comparative metrics.
 * 
 * Required for SCR-008 (Results Screen) per `04_SCREEN_BLUEPRINTS.md` §16.
 */
data class BattleResultSummary(
    // 1. Final score & Breakdown
    val result: BattleResult,
    
    // 2. Percentile is already inside [BattleResult]
    
    // 3. Improvement
    val previousDayScore: Int?, // if null, no improvement metric
    val scoreImprovement: Int?, // difference from yesterday (+ or -)
    
    // 4. Personal Best
    val isNewPersonalBest: Boolean,
    val personalBestScore: Int?,
    
    // 5. Average
    val averageScore: Int?,
    
    // 6. Rival Comparison
    val rivalComparison: RivalScoreComparison?
)
