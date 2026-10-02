package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

enum class DBBottomNavDestination(val label: String) {
    Home("Home"),
    Battle("Battle"),
    Friends("Friends"),
    Me("Me")
}

/**
 * Bottom Navigation per `05_DESIGN_SYSTEM.md` §32.
 * Height: 72–80px. Background: #080B10.
 * Four destinations: Home, Battle, Friends, Me.
 */
@Composable
fun DBBottomNavigation(
    modifier: Modifier = Modifier,
    currentDestination: DBBottomNavDestination,
    onNavigate: (DBBottomNavDestination) -> Unit
) {
    // Top border color
    val borderColor = DBColor.Border

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp) // Per spec: 72-80px
            .background(DBColor.BackgroundPrimary)
            .drawBehind {
                // Subtle top border
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        DBBottomNavDestination.entries.forEach { destination ->
            val isSelected = currentDestination == destination
            val isBattle = destination == DBBottomNavDestination.Battle

            DBBottomNavItem(
                destination = destination,
                isSelected = isSelected,
                isEmphasized = isBattle,
                onClick = { onNavigate(destination) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DBBottomNavItem(
    destination: DBBottomNavDestination,
    isSelected: Boolean,
    isEmphasized: Boolean, // Special treatment for Battle tab per §32 & §49
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val textColor = when {
        isSelected -> DBColor.BrandPrimary
        else -> DBColor.TextMuted
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple for cleaner custom feel
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Placeholder for an Icon - since icons aren't fully resolved yet,
            // we will use text symbols or just the label. The spec doesn't explicitly 
            // mandate icon vs text, but labels are required.
            // Using a simple indicator dot for "emphasized" active state if needed,
            // or we just use color.
            
            Text(
                text = destination.label,
                style = if (isSelected) DBTypography.Caption else DBTypography.Caption,
                color = textColor,
                // If it's Battle, maybe we bold it or just keep it standard but colored
                fontWeight = if (isEmphasized || isSelected) androidx.compose.ui.text.font.FontWeight.Bold else null
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .fillMaxWidth(0.3f)
                        .background(DBColor.BrandPrimary)
                )
            }
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBBottomNavigationPreview() {
    Column {
        DBBottomNavigation(
            currentDestination = DBBottomNavDestination.Home,
            onNavigate = {}
        )
        Spacer(modifier = Modifier.height(DBSpacing.MD))
        DBBottomNavigation(
            currentDestination = DBBottomNavDestination.Battle,
            onNavigate = {}
        )
    }
}
