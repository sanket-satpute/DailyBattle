package com.sanket_satpute_20.dailybattle.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBBottomNavDestination
import com.sanket_satpute_20.dailybattle.design.components.DBBottomNavigation

/**
 * Mapping between navigation routes and the bottom-nav design component destinations.
 */
private val tabRouteMap = mapOf(
    AppRoute.Home.route to DBBottomNavDestination.Home,
    AppRoute.Battle.route to DBBottomNavDestination.Battle,
    AppRoute.Friends.route to DBBottomNavDestination.Friends,
    AppRoute.Me.route to DBBottomNavDestination.Me,
)

private val tabDestinationRouteMap = mapOf(
    DBBottomNavDestination.Home to AppRoute.Home.route,
    DBBottomNavDestination.Battle to AppRoute.Battle.route,
    DBBottomNavDestination.Friends to AppRoute.Friends.route,
    DBBottomNavDestination.Me to AppRoute.Me.route,
)

/**
 * Root application composable that hosts the [DailyBattleNavHost] with
 * a [DBBottomNavigation] bottom bar per `06_NAVIGATION_AND_FLOWS.md` §2.
 *
 * Bottom navigation is shown only when the current destination is a root tab.
 * It is hidden during Startup/onboarding and gameplay per §1.3.
 */
@Composable
fun DailyBattleApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute by remember {
        derivedStateOf { navBackStackEntry?.destination?.route }
    }

    val currentTab by remember {
        derivedStateOf { currentRoute?.let { tabRouteMap[it] } }
    }

    val showBottomNav by remember {
        derivedStateOf { currentTab != null }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DBColor.BackgroundPrimary),
        containerColor = DBColor.BackgroundPrimary,
        bottomBar = {
            if (showBottomNav) {
                DBBottomNavigation(
                    currentDestination = currentTab ?: DBBottomNavDestination.Home,
                    onNavigate = { destination ->
                        val route = tabDestinationRouteMap[destination] ?: return@DBBottomNavigation
                        navController.navigate(route) {
                            // Pop up to the start destination to avoid building up a large
                            // back stack of tab destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when re-selecting a previously selected tab
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        DailyBattleNavHost(
            modifier = Modifier.padding(innerPadding),
            navController = navController,
        )
    }
}
