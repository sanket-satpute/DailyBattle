package com.sanket_satpute_20.dailybattle.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBTopBar
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DBColor.BackgroundPrimary)
    ) {
        DBTopBar(title = "PROFILE", onBackClick = null)
        
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    Text(
                        text = "Loading profile...",
                        style = DBTypography.Body,
                        color = DBColor.TextSecondary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = uiState.message,
                        style = DBTypography.Body,
                        color = DBColor.Error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProfileUiState.Success -> {
                    ProfileContent(profile = uiState.profile)
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: com.sanket_satpute_20.dailybattle.domain.profile.Profile
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = profile.battleName,
            style = DBTypography.H1,
            color = DBColor.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "🔥 ${profile.momentum} DAY MOMENTUM",
            style = DBTypography.H3,
            color = DBColor.BrandPrimary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // BEST SCORE
        Text(
            text = "BEST SCORE",
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = profile.bestScore.toString(),
            style = DBTypography.H1,
            color = DBColor.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // AVERAGE
        Text(
            text = "AVERAGE",
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = profile.averageScore.toString(),
            style = DBTypography.H1,
            color = DBColor.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // BATTLE DNA
        Text(
            text = "BATTLE DNA",
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        DnaRow(label = "SPEED", value = profile.battleDNA.speed)
        Spacer(modifier = Modifier.height(12.dp))
        DnaRow(label = "MEMORY", value = profile.battleDNA.memory)
        Spacer(modifier = Modifier.height(12.dp))
        DnaRow(label = "PEOPLE", value = profile.battleDNA.people)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // PERSONAL RECORDS
        Text(
            text = "PERSONAL RECORDS",
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        RecordRow(label = "Best Score", value = profile.records.bestScore.toString())
        Spacer(modifier = Modifier.height(12.dp))
        RecordRow(label = "Best Momentum", value = profile.records.bestMomentum.toString())
        Spacer(modifier = Modifier.height(12.dp))
        RecordRow(label = "Battles Played", value = profile.records.battlesPlayed.toString())
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DnaRow(label: String, value: Int?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = DBTypography.H3,
            color = DBColor.TextPrimary
        )
        Text(
            text = value?.toString() ?: "--",
            style = DBTypography.H3.copy(fontWeight = FontWeight.Bold),
            color = DBColor.TextPrimary
        )
    }
}

@Composable
private fun RecordRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = DBTypography.Body,
            color = DBColor.TextSecondary
        )
        Text(
            text = value,
            style = DBTypography.Body.copy(fontWeight = FontWeight.Bold),
            color = DBColor.TextPrimary
        )
    }
}
