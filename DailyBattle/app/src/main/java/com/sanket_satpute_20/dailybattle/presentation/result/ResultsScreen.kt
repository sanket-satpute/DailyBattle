package com.sanket_satpute_20.dailybattle.presentation.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.components.DBErrorState
import com.sanket_satpute_20.dailybattle.design.components.DBLoadingState
import com.sanket_satpute_20.dailybattle.design.components.DBResultCard
import com.sanket_satpute_20.dailybattle.design.components.DBRivalCard
import com.sanket_satpute_20.dailybattle.design.components.DBTopBar
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography
import com.sanket_satpute_20.dailybattle.domain.battle.BattleResultSummary

@Composable
fun ResultsScreen(
    viewModel: ResultsViewModel = hiltViewModel(),
    onBeatRivalClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DBColor.BackgroundPrimary)
    ) {
        DBTopBar(
            title = "RESULTS",
            onBackClick = null // No back button on results as per blueprint
        )
        
        when (val state = uiState) {
            is ResultsUiState.Loading -> {
                DBLoadingState(modifier = Modifier.weight(1f))
            }
            is ResultsUiState.Error -> {
                DBErrorState(
                    modifier = Modifier.weight(1f),
                    title = "COULDN'T LOAD RESULTS",
                    description = state.message,
                    onRetry = null
                )
            }
            is ResultsUiState.Success -> {
                ResultsContent(
                    summary = state.summary,
                    onBeatRivalClick = onBeatRivalClick,
                    onShareClick = onShareClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ResultsContent(
    summary: BattleResultSummary,
    onBeatRivalClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(DBSpacing.MD),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Title
        Text(
            text = "TODAY'S RESULT",
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(DBSpacing.MD))

        // 2. Score
        Text(
            text = "${summary.result.totalScore}",
            style = DBTypography.H1,
            color = DBColor.BrandPrimary
        )
        Text(
            text = "/ 1000",
            style = DBTypography.H3,
            color = DBColor.TextMuted
        )

        Spacer(modifier = Modifier.height(DBSpacing.MD))

        // 3. Percentile
        val formattedPercentile = (summary.result.percentile * 100).toInt()
        Text(
            text = "TOP $formattedPercentile%",
            style = DBTypography.H3,
            color = DBColor.TextPrimary
        )

        Spacer(modifier = Modifier.height(DBSpacing.SM))

        // 4. Improvement
        summary.scoreImprovement?.let { improvement ->
            val improvementText = if (improvement >= 0) "+$improvement" else "$improvement"
            Text(
                text = "$improvementText YESTERDAY",
                style = DBTypography.Body,
                color = if (improvement >= 0) DBColor.Success else DBColor.Error
            )
        }

        Spacer(modifier = Modifier.height(DBSpacing.LG))

        // 5. Breakdown Card
        DBResultCard(
            snapScore = summary.result.snapScore,
            shiftScore = summary.result.shiftScore,
            crowdCallScore = summary.result.crowdCallScore
        )

        Spacer(modifier = Modifier.height(DBSpacing.LG))

        // 6. Personal Best and Average
        if (summary.isNewPersonalBest) {
            Text(
                text = "NEW PERSONAL BEST",
                style = DBTypography.Caption,
                color = DBColor.BrandPrimary
            )
            Spacer(modifier = Modifier.height(DBSpacing.XS))
        }

        summary.personalBestScore?.let { pb ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "PERSONAL BEST", style = DBTypography.Body, color = DBColor.TextSecondary)
                Text(text = "$pb", style = DBTypography.Body, color = DBColor.TextPrimary)
            }
            Spacer(modifier = Modifier.height(DBSpacing.XS))
        }

        summary.averageScore?.let { avg ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "AVERAGE", style = DBTypography.Body, color = DBColor.TextSecondary)
                Text(text = "$avg", style = DBTypography.Body, color = DBColor.TextPrimary)
            }
        }

        Spacer(modifier = Modifier.height(DBSpacing.LG))

        // 7. Rival Comparison
        summary.rivalComparison?.let { rival ->
            DBRivalCard(
                rivalName = rival.rivalName,
                rivalScore = rival.rivalScore,
                userScore = rival.userScore,
                catchUpText = if (rival.scoreGap > 0) "${rival.scoreGap} points to catch" else "You are leading!",
                action = {
                    DBButton(
                        text = "BEAT ${rival.rivalName.uppercase()}",
                        type = DBButtonType.Secondary,
                        onClick = onBeatRivalClick
                    )
                }
            )
            Spacer(modifier = Modifier.height(DBSpacing.MD))
        }

        // 8. Share Action & Tomorrow's Battle
        DBButton(
            text = "SHARE RESULT",
            type = DBButtonType.Primary,
            onClick = onShareClick
        )
        
        Spacer(modifier = Modifier.height(DBSpacing.LG))

        Text(
            text = "Tomorrow's Battle awaits.",
            style = DBTypography.Caption,
            color = DBColor.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(DBSpacing.XL))
    }
}
