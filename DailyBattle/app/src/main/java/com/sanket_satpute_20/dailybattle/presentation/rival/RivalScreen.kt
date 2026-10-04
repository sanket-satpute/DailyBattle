package com.sanket_satpute_20.dailybattle.presentation.rival

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.R
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography
import com.sanket_satpute_20.dailybattle.domain.rival.MatchOutcome
import com.sanket_satpute_20.dailybattle.domain.rival.RivalMatchHistoryItem
import com.sanket_satpute_20.dailybattle.domain.rival.ScoreDirection

@Composable
fun RivalRoute(
    onNavigateBack: () -> Unit,
    onNavigateToPractice: () -> Unit,
    viewModel: RivalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RivalScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onBeatRival = onNavigateToPractice,
        onRetry = { viewModel.loadRivalData() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RivalScreen(
    state: RivalUiState,
    onNavigateBack: () -> Unit,
    onBeatRival: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "YOUR RIVAL",
                        style = DBTypography.H2,
                        color = DBColor.TextPrimary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DBColor.BackgroundPrimary
                )
            )
        },
        containerColor = DBColor.BackgroundPrimary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            when (state) {
                is RivalUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = DBColor.BrandPrimary
                    )
                }
                is RivalUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Something went wrong",
                            style = DBTypography.Body,
                            color = DBColor.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        DBButton(
                            text = "Retry",
                            onClick = onRetry,
                            type = DBButtonType.Secondary
                        )
                    }
                }
                is RivalUiState.NoRival -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No active rival",
                            style = DBTypography.H1,
                            color = DBColor.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Play battles to find a rival.",
                            style = DBTypography.Body,
                            color = DBColor.TextSecondary
                        )
                    }
                }
                is RivalUiState.Success -> {
                    RivalSuccessContent(
                        state = state,
                        onBeatRival = onBeatRival
                    )
                }
            }
        }
    }
}

@Composable
private fun RivalSuccessContent(
    state: RivalUiState.Success,
    onBeatRival: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = state.rivalName,
            style = DBTypography.H1,
            color = DBColor.TextPrimary
        )
        Text(
            text = state.rivalScore.toString(),
            style = DBTypography.Display,
            color = DBColor.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "VS",
            style = DBTypography.H2,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = state.userName,
            style = DBTypography.H1,
            color = DBColor.TextPrimary
        )
        Text(
            text = state.userScore.toString(),
            style = DBTypography.Display,
            color = DBColor.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        val gapText = when (state.direction) {
            ScoreDirection.AHEAD -> "${state.gap} POINTS AHEAD"
            ScoreDirection.BEHIND -> "${state.gap} POINTS TO CATCH"
            ScoreDirection.TIED -> "TIED SCORE"
        }
        
        Text(
            text = gapText,
            style = DBTypography.Label,
            color = if (state.direction == ScoreDirection.BEHIND) DBColor.Error else DBColor.Success
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        DBButton(
            text = "BEAT ${state.rivalName.uppercase()}",
            onClick = onBeatRival,
            type = DBButtonType.Primary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (state.history.isNotEmpty()) {
            Text(
                text = "MATCH HISTORY",
                style = DBTypography.Label,
                color = DBColor.TextSecondary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.history) { item ->
                    HistoryItemRow(item = item, userName = state.userName, rivalName = state.rivalName)
                }
            }
        }
    }
}

@Composable
private fun HistoryItemRow(
    item: RivalMatchHistoryItem,
    userName: String,
    rivalName: String
) {
    val backgroundColor = DBColor.Surface1
    val outcomeColor = when (item.outcome) {
        MatchOutcome.WIN -> DBColor.Success
        MatchOutcome.LOSS -> DBColor.Error
        MatchOutcome.TIE -> DBColor.TextSecondary
    }
    val outcomeText = when (item.outcome) {
        MatchOutcome.WIN -> "WIN"
        MatchOutcome.LOSS -> "LOSS"
        MatchOutcome.TIE -> "TIE"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$userName ${item.userScore} - ${item.rivalScore} $rivalName",
                style = DBTypography.Body,
                color = DBColor.TextPrimary
            )
        }
        Text(
            text = outcomeText,
            style = DBTypography.Label,
            color = outcomeColor
        )
    }
}
