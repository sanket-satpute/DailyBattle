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
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography
import com.sanket_satpute_20.dailybattle.presentation.home.HomeRoute
import com.sanket_satpute_20.dailybattle.presentation.startup.BattleNameRoute
import com.sanket_satpute_20.dailybattle.presentation.startup.StartupRoute
import com.sanket_satpute_20.dailybattle.presentation.startup.WelcomeRoute

/**
 * Application navigation host.
 *
 * Implements nested navigation (Startup, Main, Gameplay) per Sprint 4.3.
 */
@Composable
fun DailyBattleNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoute.StartupGraph.route,
        modifier = modifier,
    ) {
        // --- Startup Flow ---
        navigation(
            route = AppRoute.StartupGraph.route,
            startDestination = AppRoute.Startup.route
        ) {
            composable(AppRoute.Startup.route) {
                StartupRoute(
                    onStartupComplete = {
                        navController.navigate(AppRoute.Welcome.route) {
                            popUpTo(AppRoute.Startup.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Welcome.route) {
                WelcomeRoute(
                    onGetStarted = {
                        navController.navigate(AppRoute.BattleName.route)
                    }
                )
            }
            composable(AppRoute.BattleName.route) {
                BattleNameRoute(
                    onContinue = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.StartupGraph.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // --- Main App Shell Flow ---
        navigation(
            route = AppRoute.MainGraph.route,
            startDestination = AppRoute.Home.route
        ) {
            composable(AppRoute.Home.route) {
                HomeRoute(
                    onPlayBattle = {
                        navController.navigate(AppRoute.BattleIntro.route)
                    },
                    onBeatRival = {
                        navController.navigate(AppRoute.Rival.route)
                    },
                    onAddFriend = {
                        navController.navigate(AppRoute.AddFriend.route)
                    }
                )
            }
            composable(AppRoute.Battle.route) {
                ScreenPlaceholder(label = "Battle")
            }
            composable(AppRoute.Friends.route) {
                ScreenPlaceholder(label = "Friends (SCR-010)")
            }
            composable(AppRoute.Me.route) {
                ScreenPlaceholder(label = "Me")
            }
            composable(AppRoute.Profile.route) {
                ScreenPlaceholder(label = "Profile (SCR-012)")
            }
            composable(AppRoute.History.route) {
                ScreenPlaceholder(label = "History (SCR-013)")
            }
            composable(AppRoute.AddFriend.route) {
                ScreenPlaceholder(label = "Add Friend (SCR-011)")
            }
        }

        // --- Gameplay Flow ---
        navigation(
            route = AppRoute.GameplayGraph.route,
            startDestination = AppRoute.BattleIntro.route
        ) {
            composable(AppRoute.BattleIntro.route) {
                com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroRoute(
                    onNavigateToBattle = {
                        navController.navigate(AppRoute.Snap.route) {
                            popUpTo(AppRoute.BattleIntro.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
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


