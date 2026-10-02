package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.sizing.DBSizing
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing

/**
 * Reusable DB Icon Button component implementing Sprint 3.4 requirements and `05_DESIGN_SYSTEM.md`
 * section 19.
 *
 * It enforces:
 * - Compact utility action usage
 * - Minimum touch target of 44x44px (DBSizing.TouchMinimum)
 */
@Composable
fun DBIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentColor: Color = DBColor.TextPrimary,
    disabledContentColor: Color = DBColor.TextDisabled,
    icon: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(
                minWidth = DBSizing.TouchMinimum,
                minHeight = DBSizing.TouchMinimum
            ),
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = contentColor,
            disabledContentColor = disabledContentColor
        )
    ) {
        icon()
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBIconButtonPreview() {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(DBSpacing.MD),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(DBSpacing.MD)
    ) {
        DBIconButton(onClick = {}) {
            androidx.compose.material3.Text("S")
        }
        
        DBIconButton(onClick = {}, enabled = false) {
            androidx.compose.material3.Text("S")
        }
    }
}
