package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Rival Card per `05_DESIGN_SYSTEM.md` §25.
 * Used on Home, secondary to Today's Battle.
 */
@Composable
fun DBRivalCard(
    modifier: Modifier = Modifier,
    title: String = "YOUR RIVAL",
    rivalName: String,
    rivalScore: Int,
    userName: String = "You",
    userScore: Int,
    catchUpText: String,
    action: @Composable () -> Unit
) {
    DBCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = DBTypography.Caption,
                color = DBColor.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            
            // Score rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = rivalName, style = DBTypography.H3, color = DBColor.TextPrimary)
                Text(text = rivalScore.toString(), style = DBTypography.H3, color = DBColor.TextPrimary)
            }
            
            Spacer(modifier = Modifier.height(DBSpacing.XXS))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = userName, style = DBTypography.Body, color = DBColor.TextSecondary)
                Text(text = userScore.toString(), style = DBTypography.Body, color = DBColor.TextSecondary)
            }
            
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            
            Text(
                text = catchUpText,
                style = DBTypography.Caption,
                color = DBColor.TextMuted
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            
            action()
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBRivalCardPreview() {
    Column(modifier = Modifier.padding(DBSpacing.MD)) {
        DBRivalCard(
            rivalName = "Rahul",
            rivalScore = 914,
            userName = "You",
            userScore = 901,
            catchUpText = "13 points to catch",
            action = {
                DBButton(
                    text = "Beat Rahul",
                    type = DBButtonType.Secondary,
                    onClick = {}
                )
            }
        )
    }
}
