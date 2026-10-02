package com.sanket_satpute_20.dailybattle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sanket_satpute_20.dailybattle.presentation.startup.StartupRoute

/**
 * Application navigation host. Only the startup destination is registered; later sprints add the
 * approved primary destinations without changing this initialization boundary.
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
    }
}
