package com.sanket_satpute_20.dailybattle.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun HistoryRoute(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HistoryScreen(
        uiState = uiState
    )
}

@Composable
fun HistoryScreen(
    uiState: HistoryUiState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DBColor.BackgroundPrimary)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is HistoryUiState.Loading -> {
                CircularProgressIndicator(color = DBColor.BrandPrimary)
            }
            is HistoryUiState.Empty -> {
                HistoryEmptyState()
            }
            is HistoryUiState.Error -> {
                Text(
                    text = uiState.message,
                    style = DBTypography.Body,
                    color = DBColor.Error
                )
            }
            is HistoryUiState.Success -> {
                HistoryContent(uiState.data)
            }
        }
    }
}

@Composable
private fun HistoryEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "YOUR HISTORY STARTS TODAY",
            style = DBTypography.H2,
            color = DBColor.TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Complete your first Battle\nto see your progress.",
            style = DBTypography.Body,
            color = DBColor.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryContent(
    data: HistoryUiStateData
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "HISTORY",
            style = DBTypography.H1,
            color = DBColor.TextPrimary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "YOUR SCORE",
            style = DBTypography.H3,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Graph Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(DBColor.Surface1),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "[Graph Placeholder]",
                style = DBTypography.Body,
                color = DBColor.TextSecondary
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats
        StatRow(label = "BEST", value = data.best.toString())
        Spacer(modifier = Modifier.height(8.dp))
        StatRow(label = "AVERAGE", value = data.average.toString())
        Spacer(modifier = Modifier.height(8.dp))
        StatRow(label = "MOMENTUM", value = "${data.momentum} DAYS")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "RECENT BATTLES",
            style = DBTypography.H3,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recent Battles Placeholder
        Text(
            text = "[Recent Battles List Placeholder]",
            style = DBTypography.Body,
            color = DBColor.TextSecondary
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = DBTypography.Body,
            color = DBColor.TextSecondary
        )
        Text(
            text = value,
            style = DBTypography.H3,
            color = DBColor.TextPrimary
        )
    }
}
