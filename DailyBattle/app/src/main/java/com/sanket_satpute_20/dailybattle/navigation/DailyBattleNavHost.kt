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
 * Root tab destinations (Home, Battle, Friends, Me) are registered per
 * `06_NAVIGATION_AND_FLOWS.md` §2 and Sprint 4.1. Actual screen content
 * is implemented in subsequent sprints.
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

        composable(AppRoute.Home.route) {
            TabPlaceholder(label = "Home")
        }

        composable(AppRoute.Battle.route) {
            TabPlaceholder(label = "Battle")
        }

        composable(AppRoute.Friends.route) {
            TabPlaceholder(label = "Friends")
        }

        composable(AppRoute.Me.route) {
            TabPlaceholder(label = "Me")
        }
    }
}

/**
 * Temporary placeholder for root-tab screens. Replaced by actual screen
 * implementations in later sprints (Phase 5+).
 */
@Composable
private fun TabPlaceholder(label: String) {
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

