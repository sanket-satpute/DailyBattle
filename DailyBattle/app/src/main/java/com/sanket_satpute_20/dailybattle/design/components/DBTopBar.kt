package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex

/**
 * Top Bar per `05_DESIGN_SYSTEM.md` §33.
 * May contain: title, back action, progress, timer, contextual action.
 * Semantics support added per Sprint 22.1 / 11_ACCESSIBILITY.md §10, §41, §42.
 */
@Composable
fun DBTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBackClick: (() -> Unit)? = null,
    progress: Float? = null,
    timer: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = DBSpacing.MD, vertical = DBSpacing.SM)
            .semantics { isTraversalGroup = true }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left area: Back button or spacing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .semantics { traversalIndex = 1f },
                contentAlignment = Alignment.CenterStart
            ) {
                if (onBackClick != null) {
                    // Back Button per §34
                    // Target touch area: >= 44x44, icon: 24px.
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Navigate back",
                                onClick = onBackClick
                            )
                            .semantics {
                                role = Role.Button
                                contentDescription = "Navigate back"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "←",
                            style = DBTypography.H2,
                            color = DBColor.TextPrimary
                        )
                    }
                }
            }

            // Center area: Title or Timer
            Box(
                modifier = Modifier
                    .weight(2f)
                    .semantics { traversalIndex = 2f },
                contentAlignment = Alignment.Center
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = DBTypography.H3,
                        color = DBColor.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics { heading() }
                    )
                } else if (timer != null) {
                    Text(
                        text = timer,
                        style = DBTypography.H2,
                        color = DBColor.TextPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.semantics {
                            contentDescription = "Time remaining: $timer"
                        }
                    )
                }
            }

            // Right area: Action or spacing
            Box(
                modifier = Modifier
                    .weight(1f)
                    .semantics { traversalIndex = 3f },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (action != null) {
                    action()
                }
            }
        }

        // Optional Progress (Gameplay Header per §35)
        if (progress != null) {
            Spacer(modifier = Modifier.height(DBSpacing.SM))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .semantics {
                        contentDescription = "Progress: ${(progress * 100).toInt()}%"
                        traversalIndex = 4f
                    },
                color = DBColor.BrandPrimary,
                trackColor = DBColor.SurfaceElevated,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBTopBarPreview_Standard() {
    DBTopBar(
        title = "Settings",
        onBackClick = {}
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBTopBarPreview_Gameplay() {
    DBTopBar(
        title = "SNAP",
        onBackClick = {},
        timer = "0:45",
        progress = 0.33f
    )
}
