package com.sanket_satpute_20.dailybattle.presentation.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.components.DBEmptyState
import com.sanket_satpute_20.dailybattle.design.components.DBErrorState
import com.sanket_satpute_20.dailybattle.design.components.DBFriendRow
import com.sanket_satpute_20.dailybattle.design.components.DBFriendRowState
import com.sanket_satpute_20.dailybattle.design.components.DBLoadingState
import com.sanket_satpute_20.dailybattle.design.components.DBTopBar
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography
import com.sanket_satpute_20.dailybattle.domain.friend.FriendStatus

@Composable
fun FriendsRoute(
    onNavigateToAddFriend: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    FriendsScreen(
        state = state,
        onAddFriendClick = onNavigateToAddFriend,
        onRetry = { viewModel.loadFriends() }
    )
}

@Composable
fun FriendsScreen(
    state: FriendsUiState,
    onAddFriendClick: () -> Unit,
    onRetry: () -> Unit
) {
    Scaffold(
        topBar = {
            DBTopBar(
                title = "FRIENDS",
                onBackClick = null // Top-level tab behavior typically doesn't have a back button, or it might? Usually Home/Friends are main tabs.
            )
        },
        containerColor = DBColor.BackgroundPrimary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state) {
                is FriendsUiState.Loading -> {
                    DBLoadingState(modifier = Modifier.fillMaxSize())
                }
                is FriendsUiState.Error -> {
                    DBErrorState(
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is FriendsUiState.Success -> {
                    val friends = state.friends
                    if (friends.size <= 1) { // Only the user is in the list
                        DBEmptyState(
                            title = "NO RIVALS YET",
                            description = "Add a friend and start\nyour first Battle rivalry.",
                            action = {
                                DBButton(
                                    text = "ADD FRIEND",
                                    onClick = onAddFriendClick,
                                    type = DBButtonType.Secondary
                                )
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        FriendsListContent(
                            friends = friends,
                            onAddFriendClick = onAddFriendClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendsListContent(
    friends: List<FriendUiModel>,
    onAddFriendClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(DBSpacing.MD),
        verticalArrangement = Arrangement.spacedBy(DBSpacing.MD)
    ) {
        item {
            DBButton(
                text = "+ ADD FRIEND",
                onClick = onAddFriendClick,
                type = DBButtonType.Secondary,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(DBSpacing.MD))
            Text(
                text = "TODAY",
                style = DBTypography.H3,
                color = DBColor.TextSecondary,
                modifier = Modifier.padding(bottom = DBSpacing.XS)
            )
        }

        items(friends, key = { it.id }) { friend ->
            val rowState = when {
                friend.isUser -> DBFriendRowState.You
                friend.isRival -> DBFriendRowState.Rival
                friend.status == FriendStatus.PENDING -> DBFriendRowState.Pending
                else -> DBFriendRowState.Normal
            }

            DBFriendRow(
                name = friend.name,
                score = friend.score,
                state = rowState
            )
        }
    }
}
