package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.radius.DBRadius
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * The states a Battle Card can be in, per `05_DESIGN_SYSTEM.md` §87 Component Variant Matrix.
 */
enum class DBBattleCardState {
    /** Battle is available to play. */
    Ready,
    /** Battle has been completed. */
    Completed,
    /** Battle data is loading. */
    Loading,
    /** Battle data failed to load. */
    Error
}

/**
 * Battle Card (aka "Challenge Card") from `05_DESIGN_SYSTEM.md` §24.
 *
 * Used primarily on Home. Dominant component, approximately 50–55% viewport.
 * Structure: title, subtitle info, challenge indicators slot, action button slot.
 * Uses major card radius (16dp) per §23.
 */
@Composable
fun DBBattleCard(
    state: DBBattleCardState,
    modifier: Modifier = Modifier,
    title: String = "TODAY'S BATTLE",
    subtitle: String = "3 challenges · ~3 minutes",
    content: @Composable () -> Unit = {},
    action: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DBRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = DBColor.Surface1
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(DBSpacing.XL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = DBTypography.H2,
                color = DBColor.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.XS))
            
            Text(
                text = subtitle,
                style = DBTypography.Body,
                color = DBColor.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            
            // Challenge indicators / content slot
            content()
            
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            
            // Action button slot
            action()
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBBattleCardPreview() {
    Column(modifier = Modifier.padding(DBSpacing.MD)) {
        DBBattleCard(
            state = DBBattleCardState.Ready,
            content = {
                Row(horizontalArrangement = Arrangement.spacedBy(DBSpacing.XS)) {
                    Text("●", color = DBColor.Snap)
                    Text("●", color = DBColor.Shift)
                    Text("●", color = DBColor.Crowd)
                }
            },
            action = {
                DBButton(text = "Play Battle", onClick = {})
            }
        )
    }
}
