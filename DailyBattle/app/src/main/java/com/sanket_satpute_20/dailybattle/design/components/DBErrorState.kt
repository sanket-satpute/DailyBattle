package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Error state per `05_DESIGN_SYSTEM.md` §51.
 *
 * Standard structure:
 *   COULDN'T LOAD [CONTENT]
 *   Check your connection and try again.
 *   [ RETRY ]
 *
 * Do not automatically throw the user back to onboarding (§51).
 * Canonical component name: `DBErrorState` (§86).
 */
@Composable
fun DBErrorState(
    title: String = "COULDN'T LOAD CONTENT",
    description: String = "Check your connection\nand try again.",
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics { contentDescription = "$title. $description" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(horizontal = DBSpacing.XL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = DBTypography.H3,
                color = DBColor.TextPrimary
            )

            Spacer(modifier = Modifier.height(DBSpacing.XS))

            Text(
                text = description,
                style = DBTypography.Body,
                color = DBColor.TextSecondary
            )

            if (onRetry != null) {
                Spacer(modifier = Modifier.height(DBSpacing.MD))
                DBButton(
                    text = "Retry",
                    type = DBButtonType.Secondary,
                    onClick = onRetry
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBErrorStatePreview() {
    DBErrorState(
        title = "COULDN'T LOAD BATTLE",
        onRetry = {}
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBErrorStatePreview_NoRetry() {
    DBErrorState(
        title = "SOMETHING WENT WRONG",
        description = "Please try again later."
    )
}
