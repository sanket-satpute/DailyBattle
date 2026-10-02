package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.sizing.DBSizing
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * 15px / 700 explicit typography rule for buttons defined in `05_DESIGN_SYSTEM.md` section 18.
 * This is an implementation detail of the button component and does not belong in the global
 * scale tokens.
 */
private val ButtonTextStyle = TextStyle(
    fontFamily = DBTypography.InterFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
)

/**
 * The visual types of buttons defined in `05_DESIGN_SYSTEM.md` section 19 and 87.
 */
enum class DBButtonType {
    /** Main action. Brand violet background, primary text, strong contrast. */
    Primary,
    /** Important but subordinate action. Using border/outline treatment to remain secondary. */
    Secondary,
    /** Low-emphasis action. Transparent background, secondary text. */
    Ghost
}

/**
 * Reusable DB Button component implementing Sprint 3.4 requirements and `05_DESIGN_SYSTEM.md`
 * sections 18-21.
 *
 * It enforces:
 * - 52px height (DBSizing.ButtonHeight)
 * - 14px radius (DBSizing.ButtonRadius)
 * - 15/700 typography
 * - States: Default, Pressed, Disabled, Loading, Success
 */
@Composable
fun DBButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: DBButtonType = DBButtonType.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    // Resolve colors based on type and state
    val containerColor = when {
        isSuccess -> DBColor.Success
        type == DBButtonType.Primary -> DBColor.BrandPrimary
        type == DBButtonType.Secondary -> Color.Transparent
        type == DBButtonType.Ghost -> Color.Transparent
        else -> DBColor.BrandPrimary
    }

    val contentColor = when {
        isSuccess -> DBColor.BackgroundPrimary // Dark text on success green for contrast
        type == DBButtonType.Primary -> DBColor.TextPrimary
        type == DBButtonType.Secondary -> DBColor.TextPrimary
        type == DBButtonType.Ghost -> DBColor.TextSecondary
        else -> DBColor.TextPrimary
    }
    
    val disabledContainerColor = when (type) {
        DBButtonType.Primary -> DBColor.SurfaceElevated
        else -> Color.Transparent
    }
    
    val disabledContentColor = DBColor.TextDisabled

    val border = when (type) {
        DBButtonType.Secondary -> BorderStroke(1.dp, DBColor.Border)
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = DBSizing.ButtonHeight, minWidth = DBSizing.TouchMinimum)
            .height(DBSizing.ButtonHeight),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(DBSizing.ButtonRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = DBSpacing.XL)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Invisible layout to preserve button dimensions during loading state (Rule 21)
            Row(
                modifier = Modifier.alphaIfLoading(isLoading),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null && !isSuccess) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(DBSpacing.XS))
                }
                Text(
                    text = if (isSuccess) "SUCCESS" else text.uppercase(),
                    style = ButtonTextStyle,
                    color = if (enabled) contentColor else disabledContentColor
                )
            }
            
            if (isLoading && !isSuccess) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = if (type == DBButtonType.Primary) DBColor.TextPrimary else DBColor.BrandPrimary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

private fun Modifier.alphaIfLoading(isLoading: Boolean): Modifier {
    return this.alpha(if (isLoading) 0f else 1f)
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBButtonPreview() {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.padding(DBSpacing.MD),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(DBSpacing.MD)
    ) {
        // Primary states
        DBButton(text = "Play Battle", onClick = {}, type = DBButtonType.Primary)
        DBButton(text = "Play Battle", onClick = {}, type = DBButtonType.Primary, enabled = false)
        DBButton(text = "Play Battle", onClick = {}, type = DBButtonType.Primary, isLoading = true)
        DBButton(text = "Play Battle", onClick = {}, type = DBButtonType.Primary, isSuccess = true)

        // Secondary states
        DBButton(text = "Secondary", onClick = {}, type = DBButtonType.Secondary)
        
        // Ghost states
        DBButton(text = "Ghost", onClick = {}, type = DBButtonType.Ghost)
    }
}
