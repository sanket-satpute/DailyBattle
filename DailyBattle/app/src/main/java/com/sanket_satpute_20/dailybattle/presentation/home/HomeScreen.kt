package com.sanket_satpute_20.dailybattle.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCard
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCardState
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.components.DBCard
import com.sanket_satpute_20.dailybattle.design.components.DBErrorState
import com.sanket_satpute_20.dailybattle.design.components.DBRivalCard
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun HomeRoute(
    onPlayBattle: () -> Unit,
    onBeatRival: () -> Unit,
    onAddFriend: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        state = state,
        onPlayBattle = onPlayBattle,
        onBeatRival = onBeatRival,
        onAddFriend = onAddFriend,
        onRetry = viewModel::loadHomeData,
        modifier = modifier
    )
}

@Composable
internal fun HomeScreen(
    state: HomeState,
    onPlayBattle: () -> Unit,
    onBeatRival: () -> Unit,
    onAddFriend: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.isError) {
        val title = state.errorMessage ?: "COULDN'T LOAD TODAY'S BATTLE"
        val description = if (state.isOffline) "You are currently offline." else "Check your connection and try again."
        DBErrorState(
            title = title,
            description = description,
            onRetry = onRetry,
            modifier = modifier
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(DBSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(DBSpacing.LG)
    ) {
        // Greeting & Momentum
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                if (state.isLoading) {
                    SkeletonBox(width = 120, height = 24)
                } else {
                    Text(
                        text = "Good evening, ${state.userName}.",
                        style = DBTypography.H3,
                        color = DBColor.TextPrimary
                    )
                }
            }

            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                if (state.isLoading) {
                    SkeletonBox(width = 60, height = 16)
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonBox(width = 80, height = 20)
                } else {
                    Text(
                        text = "Momentum",
                        style = DBTypography.Caption,
                        color = DBColor.TextSecondary
                    )
                    Text(
                        text = "🔥 ${state.momentumDays} days",
                        style = DBTypography.Body,
                        color = DBColor.TextPrimary
                    )
                }
            }
        }

        // Today's Battle Card
        if (state.isLoading) {
            DBBattleCard(
                state = DBBattleCardState.Loading,
                title = "TODAY'S BATTLE",
                subtitle = "Loading...",
                content = { SkeletonBox(width = 100, height = 20) },
                action = { SkeletonBox(width = 200, height = 52) }
            )
        } else {
            DBBattleCard(
                state = state.battleState,
                title = if (state.battleState == DBBattleCardState.Completed) "TODAY'S BATTLE ✓" else "TODAY'S BATTLE",
                subtitle = if (state.battleState == DBBattleCardState.Completed) "" else "${state.challengesCount} challenges · ~${state.estimatedMinutes} minutes",
                content = {
                    if (state.battleState == DBBattleCardState.Completed) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(DBSpacing.XS)
                        ) {
                            Text(text = state.score?.toString() ?: "", style = DBTypography.H1, color = DBColor.TextPrimary)
                            Text(text = state.percentileText ?: "", style = DBTypography.Body, color = DBColor.BrandPrimary)
                            Spacer(modifier = Modifier.height(DBSpacing.MD))
                            Text(text = "NEXT BATTLE", style = DBTypography.Caption, color = DBColor.TextSecondary)
                            Text(text = state.nextBattleTime ?: "TOMORROW", style = DBTypography.Body, color = DBColor.TextPrimary)
                        }
                    } else {
                        // Placeholder for ● ● ●
                        Row(horizontalArrangement = Arrangement.spacedBy(DBSpacing.XS)) {
                            repeat(state.challengesCount) {
                                Text("●", color = DBColor.TextSecondary) // Snap/Shift colors are hidden per SCR-003
                            }
                        }
                    }
                },
                action = {
                    if (state.battleState != DBBattleCardState.Completed) {
                        DBButton(
                            text = "PLAY BATTLE",
                            onClick = onPlayBattle,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

        // Rival Card
        if (state.isLoading) {
            DBCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(DBSpacing.MD)) {
                    SkeletonBox(width = 80, height = 16)
                    Spacer(modifier = Modifier.height(DBSpacing.MD))
                    SkeletonBox(width = 200, height = 24)
                    Spacer(modifier = Modifier.height(DBSpacing.XXS))
                    SkeletonBox(width = 200, height = 24)
                    Spacer(modifier = Modifier.height(DBSpacing.MD))
                    SkeletonBox(width = 150, height = 16)
                    Spacer(modifier = Modifier.height(DBSpacing.MD))
                    SkeletonBox(width = 200, height = 52)
                }
            }
        } else {
            if (state.hasRival) {
                DBRivalCard(
                    rivalName = state.rivalName,
                    rivalScore = state.rivalScore,
                    userName = "You",
                    userScore = state.userScore,
                    catchUpText = "${state.pointsToCatch} points to catch",
                    action = {
                        DBButton(
                            text = "BEAT ${state.rivalName.uppercase()}",
                            type = DBButtonType.Secondary,
                            onClick = onBeatRival,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            } else {
                DBCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(DBSpacing.MD),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "NO RIVAL YET",
                            style = DBTypography.Caption,
                            color = DBColor.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(DBSpacing.MD))
                        Text(
                            text = "Add a friend and start\nyour first Battle rivalry.",
                            style = DBTypography.Body,
                            color = DBColor.TextPrimary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(DBSpacing.LG))
                        DBButton(
                            text = "ADD FRIEND",
                            type = DBButtonType.Secondary,
                            onClick = onAddFriend,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SkeletonBox(width: Int, height: Int) {
    Box(
        modifier = Modifier
            .size(width.dp, height.dp)
            .background(DBColor.Surface2, RoundedCornerShape(4.dp))
    )
}
