package com.sanket_satpute_20.dailybattle.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.domain.validation.InputValidator
import com.sanket_satpute_20.dailybattle.domain.validation.ValidationResult

/**
 * Specialized input for Battle Codes.
 * Incorporates Sprint 3.5 rules: auto-uppercase, max 6 characters, and canonical validation logic.
 */
@Composable
fun DBBattleCodeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    feedbackText: String? = null,
    state: DBInputState = DBInputState.Default
) {
    DBTextInput(
        value = value,
        onValueChange = { newValue ->
            // Enforce max 6 characters and normalize to uppercase immediately
            val normalized = InputValidator.normalizeBattleCode(newValue)
            if (normalized.length <= 6) {
                onValueChange(normalized)
            }
        },
        label = "Battle Code",
        modifier = modifier,
        state = state,
        feedbackText = feedbackText,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
    )
}

// ── Previews ────────────────────────────────────────────────────────────

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF080B10)
@Composable
private fun DBBattleCodeInputPreview() {
    var validCode by remember { mutableStateOf("K4X8M9") }
    var invalidCode by remember { mutableStateOf("ABC") }

    Column(
        modifier = Modifier.padding(DBSpacing.MD),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(DBSpacing.MD)
    ) {
        DBBattleCodeInput(
            value = validCode,
            onValueChange = { validCode = it },
            state = DBInputState.Valid,
            feedbackText = "Valid Battle Code"
        )
        
        DBBattleCodeInput(
            value = invalidCode,
            onValueChange = { invalidCode = it },
            state = DBInputState.Invalid,
            feedbackText = "Battle Code must be exactly 6 characters."
        )
    }
}
