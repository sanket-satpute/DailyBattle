package com.sanket_satpute_20.dailybattle.presentation.shift

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.animation.pulse
import com.sanket_satpute_20.dailybattle.design.animation.shake
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.domain.shift.ShiftState

/**
 * UI State for the Shift Challenge screen.
 */
data class ShiftUiState(
    val domainState: ShiftState = ShiftState.NotStarted,
    // Note: Timer values, progress (2/3), score values are blocked by DEC-GAME-002.
    val progressText: String = "2 / 3",
    val timerText: String = "--:--",
    val instructionText: String = "WHAT MOVED?",
    
    // Abstract answer options for UI, pending exact implementation rules
    val answerOptions: List<String> = listOf("Option 1", "Option 2"),
    
    // Feedback states
    val isCorrect: Boolean = false,
    val isIncorrect: Boolean = false,
    val isComplete: Boolean = false
)

/**
 * Screen scaffolding for the Shift Challenge.
 * 
 * Layout follows 04_SCREEN_BLUEPRINTS.md §14 (SCR-006):
 * - Top: Title + Progress
 * - Timer
 * - Middle: Grid
 * - Instruction: "WHAT MOVED?"
 * - Bottom: Answer options (min 52-56px touch height)
 *
 * The actual grid rendering is explicitly left un-implemented
 * as the algorithm and grid size are PENDING (DEC-GAME-002).
 */
@Composable
fun ShiftScreen(
    uiState: ShiftUiState,
    onAnswerSelected: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SHIFT",
                style = MaterialTheme.typography.titleLarge,
                color = DBColor.BrandPrimary, // Shift accent conceptually 
                modifier = Modifier.weight(1f)
            )
            Text(
                text = uiState.progressText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.size(24.dp))
        
        // TIMER SECTION
        Text(
            text = uiState.timerText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(24.dp))

        val haptic = LocalHapticFeedback.current

        // Feedback hooks (Sprint 9.3 stub)
        LaunchedEffect(uiState.isCorrect) {
            if (uiState.isCorrect) {
                // Short success sound hook (blocked by actual sound assets)
                // SoundManager.playSound("success")
                
                // Light/success haptic
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            }
        }

        LaunchedEffect(uiState.isIncorrect) {
            if (uiState.isIncorrect) {
                // Short error sound hook (blocked by actual sound assets)
                // SoundManager.playSound("error")
                
                // Short error haptic
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
            }
        }

        LaunchedEffect(uiState.isComplete) {
            if (uiState.isComplete) {
                // Short completion feedback delay (as per 10_ANIMATION_HAPTICS.md)
                kotlinx.coroutines.delay(1000)
                onComplete()
            }
        }

        // GRID SECTION (Placeholder for Shift Grid)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pulse(isPulsing = uiState.isCorrect)
                .shake(isShaking = uiState.isIncorrect)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SHIFT GRID\n(Pending DEC-GAME-002)",
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.size(24.dp))

        // INSTRUCTION
        Text(
            text = uiState.instructionText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.size(16.dp))

        // ANSWER OPTIONS
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.answerOptions.forEach { option ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp) // Minimum touch height requirement (52-56px)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onAnswerSelected(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.size(16.dp))
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ShiftScreenPreview() {
    MaterialTheme {
        ShiftScreen(
            uiState = ShiftUiState(),
            onAnswerSelected = {},
            onComplete = {}
        )
    }
}
