package com.sanket_satpute_20.dailybattle.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography
import com.sanket_satpute_20.dailybattle.presentation.startup.StartupRoute

/**
 * Application navigation host.
 *
 * All approved screens (SCR-001 through SCR-013) are registered per Sprint 4.2.
 * Actual screen content is implemented in subsequent sprints.
 */
@Composable
fun DailyBattleNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.Startup.route,
        modifier = modifier,
    ) {
        composable(AppRoute.Startup.route) {
            StartupRoute()
        }

        composable(AppRoute.Welcome.route) {
            ScreenPlaceholder(label = "Welcome (SCR-001)")
        }

        composable(AppRoute.BattleName.route) {
            ScreenPlaceholder(label = "Battle Name (SCR-002)")
        }

        composable(AppRoute.Home.route) {
            ScreenPlaceholder(label = "Home (SCR-003)")
        }

        composable(AppRoute.Battle.route) {
            ScreenPlaceholder(label = "Battle")
        }

        composable(AppRoute.BattleIntro.route) {
            ScreenPlaceholder(label = "Battle Intro (SCR-004)")
        }

        composable(AppRoute.Snap.route) {
            ScreenPlaceholder(label = "Snap (SCR-005)")
        }

        composable(AppRoute.Shift.route) {
            ScreenPlaceholder(label = "Shift (SCR-006)")
        }

        composable(AppRoute.CrowdCall.route) {
            ScreenPlaceholder(label = "Crowd Call (SCR-007)")
        }

        composable(AppRoute.Results.route) {
            ScreenPlaceholder(label = "Results (SCR-008)")
        }

        composable(AppRoute.Rival.route) {
            ScreenPlaceholder(label = "Rival (SCR-009)")
        }

        composable(AppRoute.Friends.route) {
            ScreenPlaceholder(label = "Friends (SCR-010)")
        }

        composable(AppRoute.AddFriend.route) {
            ScreenPlaceholder(label = "Add Friend (SCR-011)")
        }

        composable(AppRoute.Profile.route) {
            ScreenPlaceholder(label = "Profile (SCR-012)")
        }

        composable(AppRoute.History.route) {
            ScreenPlaceholder(label = "History (SCR-013)")
        }

        composable(AppRoute.Me.route) {
            ScreenPlaceholder(label = "Me")
        }
    }
}

/**
 * Temporary placeholder for screens. Replaced by actual screen
 * implementations in later sprints.
 */
@Composable
private fun ScreenPlaceholder(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = DBTypography.H2,
            color = DBColor.TextSecondary
        )
    }
}


