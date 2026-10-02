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
 * Empty state per `05_DESIGN_SYSTEM.md` §50.
 *
 * Structure: WHAT IS MISSING? / WHY DOES IT MATTER? / WHAT CAN I DO?
 * Never show a blank screen (§50).
 * Canonical component name: `DBEmptyState` (§86).
 */
@Composable
fun DBEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
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
            // WHAT IS MISSING?
            Text(
                text = title,
                style = DBTypography.H3,
                color = DBColor.TextPrimary
            )

            Spacer(modifier = Modifier.height(DBSpacing.XS))

            // WHY DOES IT MATTER?
            Text(
                text = description,
                style = DBTypography.Body,
                color = DBColor.TextSecondary
            )

            // WHAT CAN I DO?
            if (action != null) {
                Spacer(modifier = Modifier.height(DBSpacing.MD))
                action()
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBEmptyStatePreview() {
    DBEmptyState(
        title = "NO RIVALS YET",
        description = "Add a friend and start\nyour first Battle rivalry.",
        action = {
            DBButton(
                text = "Add Friend",
                type = DBButtonType.Secondary,
                onClick = {}
            )
        }
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBEmptyStatePreview_NoAction() {
    DBEmptyState(
        title = "NO HISTORY",
        description = "Complete a battle to see\nyour history here."
    )
}
