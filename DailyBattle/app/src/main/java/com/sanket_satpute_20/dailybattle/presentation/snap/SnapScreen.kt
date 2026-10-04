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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.domain.snap.SnapState

/**
 * UI State for the Snap Challenge screen.
 */
data class SnapUiState(
    val domainState: SnapState = SnapState.NotStarted,
    // Note: Timer values, progress (1/3), score values are blocked by DEC-GAME-001.
    val progressText: String = "1 / ?", // Blocked
    val timerText: String = "--:--",   // Blocked
    val scoreText: String = "Score --" // Blocked
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
    onElementTap: (String) -> Unit
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
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.size(24.dp))
        
        Text(
            text = uiState.timerText,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(24.dp))

        // GAME AREA SECTION (60-65% of viewport conceptually)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder: The actual generation of targets/distractors is blocked.
            Text(
                text = "GAME AREA\n(Pending DEC-GAME-001)",
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.size(24.dp))

        // BOTTOM SECTION
        Text(
            text = "TAP TARGET", // Exact instruction text is pending algorithm
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = uiState.scoreText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.size(16.dp))
    }
}
