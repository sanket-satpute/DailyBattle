package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBInputState
import com.sanket_satpute_20.dailybattle.design.components.DBTextInput
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun BattleNameRoute(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BattleNameViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onContinue()
        }
    }

    BattleNameScreen(
        state = state,
        onNameChanged = viewModel::onNameChanged,
        onSubmit = viewModel::onSubmit,
        modifier = modifier
    )
}

@Composable
internal fun BattleNameScreen(
    state: BattleNameState,
    onNameChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DBSpacing.ScreenMargin)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Text(
                text = "YOUR BATTLE\nNAME",
                style = DBTypography.Display,
                color = DBColor.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            
            Text(
                text = "Choose the name your\nfriends will see.",
                style = DBTypography.BodyLarge,
                color = DBColor.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.XXL))
            
            val inputState = when {
                state.isError -> DBInputState.Invalid
                state.name.isNotEmpty() && state.isValid -> DBInputState.Valid
                else -> DBInputState.Default
            }
            
            DBTextInput(
                value = state.name,
                onValueChange = onNameChanged,
                label = "Battle Name",
                state = inputState,
                feedbackText = state.errorMessage,
                enabled = !state.isSubmitting,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onSubmit() })
            )
        }
        
        DBButton(
            text = "Continue",
            onClick = onSubmit,
            isLoading = state.isSubmitting,
            enabled = !state.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = DBSpacing.LG)
        )
    }
}
