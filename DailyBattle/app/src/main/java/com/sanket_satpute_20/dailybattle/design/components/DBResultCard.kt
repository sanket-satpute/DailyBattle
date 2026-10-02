package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing

/**
 * Result Card per `05_DESIGN_SYSTEM.md` §26.
 * Presents performance information.
 */
@Composable
fun DBResultCard(
    modifier: Modifier = Modifier,
    snapScore: Int,
    snapMax: Int = 300,
    shiftScore: Int,
    shiftMax: Int = 300,
    crowdCallScore: Int,
    crowdCallMax: Int = 300
) {
    DBCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(DBSpacing.MD)
        ) {
            DBStat(
                label = "SNAP",
                value = snapScore.toString(),
                maxValue = snapMax.toString()
            )
            
            DBStat(
                label = "SHIFT",
                value = shiftScore.toString(),
                maxValue = shiftMax.toString()
            )
            
            DBStat(
                label = "CROWD CALL",
                value = crowdCallScore.toString(),
                maxValue = crowdCallMax.toString()
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBResultCardPreview() {
    Column(modifier = Modifier.padding(DBSpacing.MD)) {
        DBResultCard(
            snapScore = 287,
            shiftScore = 252,
            crowdCallScore = 271
        )
    }
}
