package com.sanket_satpute_20.dailybattle.presentation.battle.intro

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBErrorState
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun BattleIntroScreen(
    state: BattleIntroState,
    onStartBattle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Back behavior confirmed by product pending decision: 
    // Allow Back navigation to Home. Since gameplay hasn't started, returning is safe.
    BackHandler {
        onBack()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(DBSpacing.XL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(color = DBColor.BrandPrimary)
            return@Column
        }

        if (state.isError) {
            DBErrorState(
                title = "COULDN'T START BATTLE",
                description = state.errorMessage ?: "An unexpected error occurred.",
                onRetry = onStartBattle
            )
            return@Column
        }

        // Blueprint Layout
        Text(
            text = "TODAY'S BATTLE",
            style = DBTypography.H1,
            color = DBColor.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(DBSpacing.LG))

        Text(
            text = "${state.challengesCount} challenges",
            style = DBTypography.H3,
            color = DBColor.TextPrimary
        )

        Spacer(modifier = Modifier.height(DBSpacing.XS))

        Text(
            text = "~${state.estimatedMinutes} minutes",
            style = DBTypography.Body,
            color = DBColor.TextSecondary
        )

        Spacer(modifier = Modifier.height(DBSpacing.XL))

        Row(horizontalArrangement = Arrangement.spacedBy(DBSpacing.MD)) {
            Text("●", color = DBColor.TextSecondary)
            Text("○", color = DBColor.TextSecondary)
            Text("○", color = DBColor.TextSecondary)
        }

        Spacer(modifier = Modifier.height(DBSpacing.XL))

        Text(
            text = "OFFICIAL ATTEMPT",
            style = DBTypography.Caption,
            color = DBColor.BrandPrimary
        )

        Spacer(modifier = Modifier.height(DBSpacing.MD))

        DBButton(
            text = "START BATTLE",
            onClick = onStartBattle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
