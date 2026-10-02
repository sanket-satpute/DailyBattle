package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Loading state per `05_DESIGN_SYSTEM.md` §49.
 *
 * Uses a small contextual loading indicator — not a giant spinner (§49).
 * Canonical component name: `DBLoadingState` (§86).
 */
@Composable
fun DBLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = message ?: "Loading" },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp), // Small contextual indicator per §49
                color = DBColor.BrandPrimary,
                strokeWidth = 3.dp
            )

            if (message != null) {
                androidx.compose.foundation.layout.Spacer(
                    modifier = Modifier.padding(top = DBSpacing.MD)
                )
                Text(
                    text = message,
                    style = DBTypography.Body,
                    color = DBColor.TextSecondary
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBLoadingStatePreview() {
    DBLoadingState(message = "Loading battle…")
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBLoadingStatePreview_NoMessage() {
    DBLoadingState()
}
