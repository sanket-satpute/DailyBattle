package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.presentation.state.ScreenState

/** Hilt-wired entry point for the startup destination. */
@Composable
fun StartupRoute(
    modifier: Modifier = Modifier,
    viewModel: AppStartupViewModel = hiltViewModel(),
    onStartupComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(state) {
        if (state is ScreenState.Success) {
            onStartupComplete()
        }
    }

    StartupScreen(state = state, modifier = modifier)
}

/**
 * Renders the application's initial state. [ScreenState.Success] intentionally renders no content;
 * the approved primary destination replaces it once Phase 4 defines the app shell.
 */
@Composable
fun StartupScreen(state: ScreenState<Unit>, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is ScreenState.Loading -> CircularProgressIndicator()
            is ScreenState.Success -> Unit
            is ScreenState.Empty -> Unit
            is ScreenState.Error -> Unit
        }
    }
}
