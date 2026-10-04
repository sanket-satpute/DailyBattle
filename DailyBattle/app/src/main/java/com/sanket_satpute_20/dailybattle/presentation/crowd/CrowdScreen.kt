package com.sanket_satpute_20.dailybattle.presentation.crowd

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdChoice
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdDistribution
import com.sanket_satpute_20.dailybattle.domain.crowd.CrowdState

/**
 * UI State for the Crowd Call Challenge screen.
 */
data class CrowdUiState(
    val domainState: CrowdState = CrowdState.NotStarted,
    val progressText: String = "3 / 3",
    val questionText: String = "",
    val choices: List<CrowdChoice> = emptyList(),
    val selectedChoiceId: String? = null,
    val isResultRevealed: Boolean = false,
    val distribution: CrowdDistribution? = null,
    val isCorrect: Boolean = false,
    val isComplete: Boolean = false
)

/**
 * Screen scaffolding for the Crowd Call Challenge.
 *
 * Layout follows 04_SCREEN_BLUEPRINTS.md §15 (SCR-007):
 * - Top: Title + Progress
 * - Middle: Question
 * - Bottom: Answer cards (min 52-56px touch height)
 * 
 * After selection:
 * - Shows distribution and result.
 */
@Composable
fun CrowdScreen(
    uiState: CrowdUiState,
    onAnswerSelected: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DBColor.BackgroundPrimary)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CROWD CALL",
                style = MaterialTheme.typography.titleLarge,
                color = DBColor.Crowd,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = uiState.progressText,
                style = MaterialTheme.typography.titleMedium,
                color = DBColor.TextPrimary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // QUESTION SECTION
        Text(
            text = uiState.questionText,
            style = MaterialTheme.typography.headlineMedium,
            color = DBColor.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        // ANSWERS SECTION
        if (!uiState.isResultRevealed) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.choices.forEach { choice ->
                    val isSelected = choice.id == uiState.selectedChoiceId
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp) // Minimum 52-56px height
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) DBColor.SurfaceElevated else DBColor.Surface1)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) DBColor.Crowd else DBColor.Border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = uiState.selectedChoiceId == null) {
                                onAnswerSelected(choice.id)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = choice.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) DBColor.Crowd else DBColor.TextPrimary
                        )
                    }
                }
            }
        } else {
            // RESULT DISTRIBUTION
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "THE CROWD",
                    style = MaterialTheme.typography.labelLarge,
                    color = DBColor.TextSecondary
                )
                
                uiState.choices.forEach { choice ->
                    val percentage = uiState.distribution?.percentages?.get(choice.id) ?: 0
                    val isUserPick = choice.id == uiState.selectedChoiceId
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = choice.text,
                            color = DBColor.TextPrimary,
                            modifier = Modifier.weight(0.3f)
                        )
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(DBColor.Surface2)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(percentage / 100f)
                                    .fillMaxSize()
                                    .background(if (isUserPick) DBColor.Crowd else DBColor.TextMuted)
                            )
                        }
                        Text(
                            text = "$percentage%",
                            color = DBColor.TextPrimary,
                            modifier = Modifier.weight(0.2f),
                            textAlign = TextAlign.End
                        )
                    }
                }
                
                Spacer(modifier = Modifier.size(24.dp))
                
                Text(
                    text = "YOUR PICK",
                    style = MaterialTheme.typography.labelLarge,
                    color = DBColor.TextSecondary
                )
                val userChoiceText = uiState.choices.find { it.id == uiState.selectedChoiceId }?.text ?: "None"
                Text(
                    text = userChoiceText,
                    style = MaterialTheme.typography.titleLarge,
                    color = DBColor.TextPrimary
                )
                
                Spacer(modifier = Modifier.size(8.dp))
                
                Text(
                    text = if (uiState.isCorrect) "CORRECT" else "INCORRECT",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (uiState.isCorrect) DBColor.Success else DBColor.Error
                )
            }
        }

        Spacer(modifier = Modifier.size(32.dp))

        // Complete callback
        LaunchedEffect(uiState.isComplete) {
            if (uiState.isComplete) {
                // Short completion feedback delay (as per 10_ANIMATION_HAPTICS.md)
                kotlinx.coroutines.delay(2000)
                onComplete()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CrowdScreenPreview() {
    MaterialTheme {
        CrowdScreen(
            uiState = CrowdUiState(
                questionText = "You suddenly get ₹500 tonight. What would most people choose?",
                choices = listOf(
                    CrowdChoice("1", "Movie"),
                    CrowdChoice("2", "Food + Hangout"),
                    CrowdChoice("3", "Gaming"),
                    CrowdChoice("4", "Save it")
                )
            ),
            onAnswerSelected = {},
            onComplete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CrowdScreenResultPreview() {
    MaterialTheme {
        CrowdScreen(
            uiState = CrowdUiState(
                questionText = "You suddenly get ₹500 tonight. What would most people choose?",
                choices = listOf(
                    CrowdChoice("1", "Movie"),
                    CrowdChoice("2", "Food + Hangout"),
                    CrowdChoice("3", "Gaming"),
                    CrowdChoice("4", "Save it")
                ),
                selectedChoiceId = "2",
                isResultRevealed = true,
                distribution = CrowdDistribution(
                    percentages = mapOf(
                        "1" to 19,
                        "2" to 36,
                        "3" to 27,
                        "4" to 18
                    )
                ),
                isCorrect = true
            ),
            onAnswerSelected = {},
            onComplete = {}
        )
    }
}
