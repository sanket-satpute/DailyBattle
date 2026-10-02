package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.radius.DBRadius
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Standard card surface from `05_DESIGN_SYSTEM.md` section 23.
 *
 * Uses Surface1 background with 16px (major card) radius.
 * Cards should exist because they improve information grouping —
 * do not turn every piece of content into a card (§23).
 */
@Composable
fun DBCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DBRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = DBColor.Surface1
        ),
        border = BorderStroke(1.dp, DBColor.Border)
    ) {
        Column(
            modifier = Modifier.padding(DBSpacing.MD),
            content = content
        )
    }
}

/**
 * Elevated card surface from `05_DESIGN_SYSTEM.md` section 23.
 *
 * Uses SurfaceElevated background with 16px (major card) radius.
 * For elevated interactive surfaces and secondary cards (§6.2).
 */
@Composable
fun DBElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DBRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = DBColor.SurfaceElevated
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(DBSpacing.MD),
            content = content
        )
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBCardPreview() {
    Column(modifier = Modifier.padding(DBSpacing.MD)) {
        DBCard {
            Text("Standard Card", style = DBTypography.H3, color = DBColor.TextPrimary)
            Text("Content inside a standard card", style = DBTypography.Body, color = DBColor.TextSecondary)
        }
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(DBSpacing.MD))
        DBElevatedCard {
            Text("Elevated Card", style = DBTypography.H3, color = DBColor.TextPrimary)
            Text("Content inside an elevated card", style = DBTypography.Body, color = DBColor.TextSecondary)
        }
    }
}
