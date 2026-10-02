package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.sizing.DBSizing
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

enum class DBInputState {
    Default,
    Valid,
    Invalid
}

/**
 * Standard text input component complying with Sprint 3.5 rules and `05_DESIGN_SYSTEM.md` section 22.
 * Enforces 52px height and handles accessibility requirements such as non-color-only validity feedback.
 */
@Composable
fun DBTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    state: DBInputState = DBInputState.Default,
    feedbackText: String? = null,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    val isError = state == DBInputState.Invalid
    val isSuccess = state == DBInputState.Valid

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = DBSizing.InputHeight),
            enabled = enabled,
            label = { Text(label, style = DBTypography.Body) },
            isError = isError,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = DBTypography.BodyLarge,
            shape = RoundedCornerShape(8.dp), // General input radius
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DBColor.TextPrimary,
                unfocusedTextColor = DBColor.TextPrimary,
                disabledTextColor = DBColor.TextDisabled,
                focusedContainerColor = DBColor.BackgroundPrimary,
                unfocusedContainerColor = DBColor.BackgroundPrimary,
                disabledContainerColor = DBColor.Surface1,
                cursorColor = DBColor.BrandPrimary,
                errorCursorColor = DBColor.Error,
                
                focusedBorderColor = DBColor.BrandPrimary,
                unfocusedBorderColor = DBColor.Border,
                disabledBorderColor = DBColor.Border,
                errorBorderColor = DBColor.Error,
                
                focusedLabelColor = DBColor.BrandPrimary,
                unfocusedLabelColor = DBColor.TextSecondary,
                disabledLabelColor = DBColor.TextDisabled,
                errorLabelColor = DBColor.Error
            ),
            trailingIcon = {
                when (state) {
                    DBInputState.Invalid -> {
                        Text(
                            text = "!",
                            color = DBColor.Error,
                            style = DBTypography.H3
                        )
                    }
                    DBInputState.Valid -> {
                        Text(
                            text = "✓",
                            color = DBColor.Success,
                            style = DBTypography.H3
                        )
                    }
                    else -> {}
                }
            }
        )
        
        // Feedback Text (Do not communicate validity only through color)
        if (feedbackText != null) {
            Spacer(modifier = Modifier.height(DBSpacing.XXS))
            val feedbackColor = when (state) {
                DBInputState.Invalid -> DBColor.Error
                DBInputState.Valid -> DBColor.Success
                else -> DBColor.TextSecondary
            }
            Text(
                text = feedbackText,
                style = DBTypography.Caption,
                color = feedbackColor,
                modifier = Modifier.padding(start = DBSpacing.XS)
            )
        }
    }
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBTextInputPreview() {
    Column(
        modifier = Modifier.padding(DBSpacing.MD),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(DBSpacing.MD)
    ) {
        DBTextInput(
            value = "",
            onValueChange = {},
            label = "Default Input"
        )
        DBTextInput(
            value = "Filled Input",
            onValueChange = {},
            label = "Focused (simulated)"
        )
        DBTextInput(
            value = "Valid Input",
            onValueChange = {},
            label = "Valid State",
            state = DBInputState.Valid,
            feedbackText = "Looks good!"
        )
        DBTextInput(
            value = "Invalid Input",
            onValueChange = {},
            label = "Error State",
            state = DBInputState.Invalid,
            feedbackText = "This field is required"
        )
        DBTextInput(
            value = "Disabled Input",
            onValueChange = {},
            label = "Disabled",
            enabled = false
        )
    }
}
