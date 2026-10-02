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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.components.DBBottomNavDestination
import com.sanket_satpute_20.dailybattle.design.components.DBBottomNavigation

/**
 * Maps every MainGraph route to its owning bottom-nav tab.
 *
 * Root tabs map to themselves; child screens map to their parent tab
 * so the correct icon stays highlighted when navigating deeper
 * (e.g. Profile → Me tab, AddFriend → Friends tab).
 */
private val routeToTab: Map<String, DBBottomNavDestination> = mapOf(
    // Root tabs
    AppRoute.Home.route    to DBBottomNavDestination.Home,
    AppRoute.Battle.route  to DBBottomNavDestination.Battle,
    AppRoute.Friends.route to DBBottomNavDestination.Friends,
    AppRoute.Me.route      to DBBottomNavDestination.Me,
    // Friends children
    AppRoute.AddFriend.route to DBBottomNavDestination.Friends,
    // Me children
    AppRoute.Profile.route to DBBottomNavDestination.Me,
    AppRoute.History.route to DBBottomNavDestination.Me,
)

/**
 * Reverse map: bottom-nav destination → root tab route string.
 */
private val tabDestinationRouteMap: Map<DBBottomNavDestination, String> = mapOf(
    DBBottomNavDestination.Home    to AppRoute.Home.route,
    DBBottomNavDestination.Battle  to AppRoute.Battle.route,
    DBBottomNavDestination.Friends to AppRoute.Friends.route,
    DBBottomNavDestination.Me      to AppRoute.Me.route,
)

/**
 * Routes where bottom navigation must be hidden.
 *
 * Per `06_NAVIGATION_AND_FLOWS.md`:
 * - §1.3: Gameplay hides app navigation (Snap, Shift, Crowd Call).
 * - §13:  Official Battle navigation lock (BattleIntro through Results/Rival).
 * - §5-7: Startup/onboarding flow (Startup, Welcome, BattleName).
 */
private val hiddenBottomNavRoutes: Set<String> = setOf(
    // Startup / onboarding
    AppRoute.Startup.route,
    AppRoute.Welcome.route,
    AppRoute.BattleName.route,
    // Gameplay flow (§13 Official Battle Navigation Lock)
    AppRoute.BattleIntro.route,
    AppRoute.Snap.route,
    AppRoute.Shift.route,
    AppRoute.CrowdCall.route,
    AppRoute.Results.route,
    AppRoute.Rival.route,
)

/**
 * Root application composable that hosts the [DailyBattleNavHost] with
 * a [DBBottomNavigation] bottom bar.
 *
 * **Gameplay Navigation Boundary (Sprint 4.4):**
 * - Normal app (MainGraph) → bottom navigation visible.
 * - Gameplay (GameplayGraph) → bottom navigation hidden.
 * - Startup/Onboarding (StartupGraph) → bottom navigation hidden.
 *
 * See `06_NAVIGATION_AND_FLOWS.md` §1.3 and §13.
 */
@Composable
fun DailyBattleApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute by remember {
        derivedStateOf { navBackStackEntry?.destination?.route }
    }

    // Determine which tab to highlight based on the current route
    val currentTab by remember {
        derivedStateOf { currentRoute?.let { routeToTab[it] } }
    }

    // Bottom nav is visible when the current route is NOT in the hidden set
    // and is a known route (null route = no destination yet, hide bottom nav)
    val showBottomNav by remember {
        derivedStateOf {
            val route = currentRoute
            route != null && route !in hiddenBottomNavRoutes
        }
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
                            // Pop up to Home to avoid building up a large
                            // back stack of tab destinations
                            popUpTo(AppRoute.Home.route) {
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
