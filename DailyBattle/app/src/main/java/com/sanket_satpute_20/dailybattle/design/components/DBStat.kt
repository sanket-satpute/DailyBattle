package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * Stat Component per `05_DESIGN_SYSTEM.md` §27.
 * "A stat must have context. Never create unexplained numeric blocks."
 */
@Composable
fun DBStat(
    label: String,
    value: String,
    maxValue: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(DBSpacing.XXS))
        
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = DBColor.TextPrimary)) {
                    append(value)
                }
                if (maxValue != null) {
                    withStyle(style = SpanStyle(color = DBColor.TextMuted)) {
                        append(" / $maxValue")
                    }
                }
            },
            style = DBTypography.H2
        )
    }
}
