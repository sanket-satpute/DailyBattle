package com.sanket_satpute_20.dailybattle.presentation.addfriend

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBBattleCodeInput
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.components.DBButtonType
import com.sanket_satpute_20.dailybattle.design.components.DBFeedback
import com.sanket_satpute_20.dailybattle.design.components.DBFeedbackType
import com.sanket_satpute_20.dailybattle.design.components.DBTopBar
import com.sanket_satpute_20.dailybattle.design.radius.DBRadius
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

@Composable
fun AddFriendRoute(
    onNavigateBack: () -> Unit,
    viewModel: AddFriendViewModel = hiltViewModel()
) {
    val userCode by viewModel.userCode.collectAsStateWithLifecycle()
    val enteredCode by viewModel.enteredCode.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    AddFriendScreen(
        userCode = userCode,
        enteredCode = enteredCode,
        inputState = inputState,
        feedbackMessage = feedbackMessage,
        submitState = submitState,
        onCodeChanged = viewModel::onCodeChanged,
        onSubmit = viewModel::submitCode,
        onSuccessAcknowledged = viewModel::acknowledgeSuccess,
        onNavigateBack = onNavigateBack
    )
}

@Composable
fun AddFriendScreen(
    userCode: String?,
    enteredCode: String,
    inputState: com.sanket_satpute_20.dailybattle.design.components.DBInputState,
    feedbackMessage: String?,
    submitState: AddFriendSubmitState,
    onCodeChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onSuccessAcknowledged: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            DBTopBar(
                title = "ADD A FRIEND",
                onBackClick = onNavigateBack
            )
        },
        containerColor = DBColor.BackgroundPrimary
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DBSpacing.MD),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Add Friend interaction area
                DBBattleCodeInput(
                    value = enteredCode,
                    onValueChange = onCodeChanged,
                    state = inputState,
                    feedbackText = feedbackMessage,
                    enabled = submitState !is AddFriendSubmitState.Loading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(DBSpacing.MD))

                DBButton(
                    text = if (submitState is AddFriendSubmitState.Loading) "ADDING..." else "ADD FRIEND",
                    onClick = onSubmit,
                    enabled = enteredCode.length == 6 && submitState !is AddFriendSubmitState.Loading,
                    type = DBButtonType.Primary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(DBSpacing.XL))

                // User Code Display
                if (userCode != null) {
                    Text(
                        text = "YOUR BATTLE CODE",
                        style = DBTypography.Caption,
                        color = DBColor.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(DBSpacing.SM))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DBRadius.Medium))
                            .background(DBColor.Surface1)
                            .padding(DBSpacing.MD),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(DBSpacing.MD)
                    ) {
                        Text(
                            text = userCode,
                            style = DBTypography.H2,
                            color = DBColor.BrandPrimary,
                            textAlign = TextAlign.Center
                        )

                        DBButton(
                            text = "COPY",
                            onClick = {
                                clipboardManager.setText(AnnotatedString(userCode))
                            },
                            type = DBButtonType.Secondary,
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))

                // Success Feedback
                if (submitState is AddFriendSubmitState.Success) {
                    DBFeedback(
                        message = "Friend request sent successfully!",
                        type = DBFeedbackType.Success
                    )
                }
            }
        }
    }
}
