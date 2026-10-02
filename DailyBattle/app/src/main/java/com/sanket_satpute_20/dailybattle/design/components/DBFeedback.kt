package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.radius.DBRadius
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Feedback type determines visual treatment.
 */
enum class DBFeedbackType {
    Success,
    Error,
    Warning,
    Info
}

/**
 * Inline feedback / success / validation feedback component per Sprint 3.8 scope.
 *
 * Used for contextual, inline feedback messages (success confirmations,
 * validation errors, warnings, informational notices).
 *
 * Canonical component name: `DBFeedback` (§86).
 * Accessibility: state must not rely on color alone (§66) — indicator symbol is always present.
 */
@Composable
fun DBFeedback(
    message: String,
    type: DBFeedbackType,
    modifier: Modifier = Modifier
) {
    val (containerColor, textColor, indicator) = when (type) {
        DBFeedbackType.Success -> Triple(
            DBColor.Success.copy(alpha = 0.1f),
            DBColor.Success,
            "✓"
        )
        DBFeedbackType.Error -> Triple(
            DBColor.Error.copy(alpha = 0.1f),
            DBColor.Error,
            "✕"
        )
        DBFeedbackType.Warning -> Triple(
            DBColor.Warning.copy(alpha = 0.1f),
            DBColor.Warning,
            "!"
        )
        DBFeedbackType.Info -> Triple(
            DBColor.Info.copy(alpha = 0.1f),
            DBColor.Info,
            "i"
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor, RoundedCornerShape(DBRadius.Small))
            .padding(horizontal = DBSpacing.MD, vertical = DBSpacing.SM)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = indicator,
                style = DBTypography.BodyLarge,
                color = textColor
            )
            Spacer(modifier = Modifier.width(DBSpacing.XS))
            Text(
                text = message,
                style = DBTypography.Body,
                color = textColor
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBFeedbackPreview() {
    Column(
        modifier = Modifier.padding(DBSpacing.MD),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(DBSpacing.SM)
    ) {
        DBFeedback(message = "Battle submitted successfully!", type = DBFeedbackType.Success)
        DBFeedback(message = "Invalid battle code.", type = DBFeedbackType.Error)
        DBFeedback(message = "Your connection is unstable.", type = DBFeedbackType.Warning)
        DBFeedback(message = "Battle starts in 5 minutes.", type = DBFeedbackType.Info)
    }
}
