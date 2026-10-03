package com.sanket_satpute_20.dailybattle.presentation.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBButton
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography

/**
 * SCR-001 — Welcome
 *
 * Dominant job: Enter Daily Battle.
 */
@Composable
fun WelcomeRoute(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    WelcomeScreen(
        onGetStarted = onGetStarted,
        modifier = modifier
    )
}

@Composable
internal fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(DBSpacing.ScreenMargin)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DAILY BATTLE",
                style = DBTypography.Display,
                color = DBColor.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(DBSpacing.XXL))
            
            Text(
                text = "3 challenges.\n~3 minutes.\nOne score.",
                style = DBTypography.H2,
                color = DBColor.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
        
        DBButton(
            text = "Get Started",
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = DBSpacing.LG)
        )
    }
}
