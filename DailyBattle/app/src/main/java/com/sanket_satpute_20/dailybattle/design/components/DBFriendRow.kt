package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.radius.DBRadius
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

enum class DBFriendRowState {
    Normal,
    You,
    Rival,
    Pending
}

/**
 * Friend Row per `05_DESIGN_SYSTEM.md` §28.
 * Displays a friend or competition rank row.
 */
@Composable
fun DBFriendRow(
    name: String,
    score: Int?,
    state: DBFriendRowState,
    modifier: Modifier = Modifier
) {
    // Styling adaptations based on state
    val containerColor = when (state) {
        DBFriendRowState.You -> DBColor.SurfaceElevated // Make "YOU" pop slightly more
        else -> DBColor.Surface1
    }
    
    val nameColor = when (state) {
        DBFriendRowState.Pending -> DBColor.TextMuted
        else -> DBColor.TextPrimary
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DBRadius.Medium))
            .background(containerColor)
            .padding(horizontal = DBSpacing.MD, vertical = DBSpacing.SM),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Name Column
        Column {
            Text(
                text = name,
                style = DBTypography.BodyLarge,
                color = nameColor
            )
            if (state == DBFriendRowState.Pending) {
                Text(
                    text = "Pending...",
                    style = DBTypography.Caption,
                    color = DBColor.TextMuted
                )
            }
        }
        
        // Right side (Score + Modifier tag)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DBSpacing.MD)
        ) {
            if (score != null) {
                Text(
                    text = score.toString(),
                    style = DBTypography.H3,
                    color = DBColor.TextPrimary
                )
            }
            
            if (state == DBFriendRowState.You) {
                Text(
                    text = "YOU",
                    style = DBTypography.Caption,
                    color = DBColor.BrandPrimary,
                    modifier = Modifier
                        .background(DBColor.BrandPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = DBSpacing.XS, vertical = 2.dp)
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBFriendRowPreview() {
    Column(
        modifier = Modifier.padding(DBSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(DBSpacing.XS)
    ) {
        DBFriendRow(
            name = "Rahul",
            score = 914,
            state = DBFriendRowState.Normal
        )
        DBFriendRow(
            name = "Sanket",
            score = 901,
            state = DBFriendRowState.You
        )
        DBFriendRow(
            name = "Neha",
            score = 880,
            state = DBFriendRowState.Rival
        )
        DBFriendRow(
            name = "Alex",
            score = null,
            state = DBFriendRowState.Pending
        )
    }
}
