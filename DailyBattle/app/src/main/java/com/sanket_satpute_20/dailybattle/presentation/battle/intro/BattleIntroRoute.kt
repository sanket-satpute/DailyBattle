package com.sanket_satpute_20.dailybattle.presentation.battle.intro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BattleIntroRoute(
    onNavigateToBattle: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BattleIntroViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is BattleIntroEvent.NavigateToBattle -> onNavigateToBattle(event.targetRoute)
                is BattleIntroEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    BattleIntroScreen(
        state = state,
        onStartBattle = viewModel::onStartBattle,
        onBack = viewModel::onBack
    )
}
