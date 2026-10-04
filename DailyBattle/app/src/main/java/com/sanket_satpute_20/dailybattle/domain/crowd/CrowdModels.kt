package com.sanket_satpute_20.dailybattle.domain.crowd

/**
 * A selectable choice in a Crowd Call challenge.
 */
data class CrowdChoice(
    val id: String,
    val text: String
)

/**
 * The core question presented during a Crowd Call challenge.
 */
data class CrowdQuestion(
    val id: String,
    val text: String,
    val choices: List<CrowdChoice>
)

/**
 * The actual distribution of choices for a given question.
 * The percentages map choice ID to an integer percentage (e.g. 0-100).
 */
data class CrowdDistribution(
    val percentages: Map<String, Int>
)
