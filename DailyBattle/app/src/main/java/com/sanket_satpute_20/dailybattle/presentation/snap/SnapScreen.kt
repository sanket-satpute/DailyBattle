package com.sanket_satpute_20.dailybattle.presentation.snap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.animation.pulse
import com.sanket_satpute_20.dailybattle.design.animation.shake
import com.sanket_satpute_20.dailybattle.domain.snap.SnapState

/**
 * UI State for the Snap Challenge screen.
 */
data class SnapUiState(
    val domainState: SnapState = SnapState.NotStarted,
    // Note: Timer values, progress (1/3), score values are blocked by DEC-GAME-001.
    val progressText: String = "1 / ?", // Blocked
    val timerText: String = "--:--",   // Blocked
    val scoreText: String = "Score --", // Blocked
    
    // Feedback states
    val isCorrect: Boolean = false,
    val isIncorrect: Boolean = false,
    val isComplete: Boolean = false
)

/**
 * Screen scaffolding for the Snap Challenge.
 * 
 * Layout follows 04_SCREEN_BLUEPRINTS.md §13 (SCR-005):
 * - Top: Title + Progress + Timer
 * - Middle (60-65%): Game Area
 * - Bottom: Instruction + Current Score
 *
 * The actual target/distractor rendering is explicitly left un-implemented
 * as the algorithm and shapes are PENDING (DEC-GAME-001).
 */
@Composable
fun SnapScreen(
    uiState: SnapUiState,
    onElementTap: (String) -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // TOP SECTION
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SNAP",
                style = MaterialTheme.typography.titleLarge,
                color = DBColor.BrandPrimary, // Snap accent conceptually 
                modifier = Modifier.weight(1f)
            )
            Text(
                text = uiState.progressText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics {
                    contentDescription = "Progress: ${uiState.progressText}"
                }
            )
        }
        
        Spacer(modifier = Modifier.size(24.dp))
        
        Text(
            text = uiState.timerText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Time remaining: ${uiState.timerText}"
                },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(24.dp))

        val haptic = LocalHapticFeedback.current

        // Trigger haptics and sound hooks based on state changes
        LaunchedEffect(uiState.isCorrect) {
            if (uiState.isCorrect) {
                // Short success sound hook (blocked by actual sound assets)
                // SoundManager.playSound("success")
                
                // Light/success haptic
                // Fallback to text handle move if platform doesn't map confirm well in standard set
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
                // Short completion feedback delay (as per 10_ANIMATION_HAPTICS.md 20. Snap Completion)
                kotlinx.coroutines.delay(1000)
                onComplete()
            }
        }

        // GAME AREA SECTION (60-65% of viewport conceptually)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pulse(isPulsing = uiState.isCorrect)
                .shake(isShaking = uiState.isIncorrect)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "GAME AREA\n(Pending DEC-GAME-001)",
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (uiState.isCorrect) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DBColor.Success.copy(alpha = 0.2f))
                            .border(1.dp, DBColor.Success, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics { contentDescription = "Correct ✓" }
                    ) {
                        Text(
                            text = "CORRECT ✓",
                            style = MaterialTheme.typography.titleMedium,
                            color = DBColor.Success,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (uiState.isIncorrect) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DBColor.Error.copy(alpha = 0.2f))
                            .border(1.dp, DBColor.Error, RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .semantics { contentDescription = "Incorrect ✕" }
                    ) {
                        Text(
                            text = "INCORRECT ✕",
                            style = MaterialTheme.typography.titleMedium,
                            color = DBColor.Error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(24.dp))

        // BOTTOM SECTION
        Text(
            text = "TAP TARGET", // Exact instruction text is pending algorithm
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Instruction: TAP TARGET"
                },
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = uiState.scoreText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Current score: ${uiState.scoreText}"
                },
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.size(16.dp))
    }
}
