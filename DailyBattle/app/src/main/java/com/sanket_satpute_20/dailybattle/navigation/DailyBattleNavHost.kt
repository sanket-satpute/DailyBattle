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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.sanket_satpute_20.dailybattle.design.color.DBColor
import com.sanket_satpute_20.dailybattle.design.typography.DBTypography
import com.sanket_satpute_20.dailybattle.presentation.home.HomeRoute
import com.sanket_satpute_20.dailybattle.presentation.snap.SnapViewModel
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
                com.sanket_satpute_20.dailybattle.presentation.friend.FriendsRoute(
                    onNavigateToAddFriend = {
                        navController.navigate(AppRoute.AddFriend.route)
                    }
                )
            }
            composable(AppRoute.Me.route) {
                com.sanket_satpute_20.dailybattle.presentation.profile.ProfileRoute()
            }
            composable(AppRoute.Profile.route) {
                com.sanket_satpute_20.dailybattle.presentation.profile.ProfileRoute()
            }
            composable(AppRoute.History.route) {
                com.sanket_satpute_20.dailybattle.presentation.history.HistoryRoute()
            }
            composable(AppRoute.Settings.route) {
                com.sanket_satpute_20.dailybattle.presentation.settings.SettingsRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onAccountDeleted = {
                        navController.navigate(AppRoute.Welcome.route) {
                            popUpTo(AppRoute.MainGraph.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.AddFriend.route) {
                com.sanket_satpute_20.dailybattle.presentation.addfriend.AddFriendRoute(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // --- Gameplay Flow ---
        navigation(
            route = AppRoute.GameplayGraph.route,
            startDestination = AppRoute.BattleIntro.route
        ) {
            composable(AppRoute.BattleIntro.route) {
                com.sanket_satpute_20.dailybattle.presentation.battle.intro.BattleIntroRoute(
                    onNavigateToBattle = { targetRoute ->
                        navController.navigate(targetRoute) {
                            popUpTo(AppRoute.BattleIntro.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(AppRoute.Snap.route) {
                val snapViewModel: SnapViewModel = hiltViewModel()
                val snapUiState by snapViewModel.uiState.collectAsState()
                com.sanket_satpute_20.dailybattle.presentation.snap.SnapScreen(
                    uiState = snapUiState,
                    onElementTap = snapViewModel::onElementTapped,
                    onComplete = {
                        navController.navigate(AppRoute.Shift.route) {
                            popUpTo(AppRoute.Snap.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Shift.route) {
                com.sanket_satpute_20.dailybattle.presentation.shift.ShiftScreen(
                    uiState = com.sanket_satpute_20.dailybattle.presentation.shift.ShiftUiState(),
                    onAnswerSelected = {},
                    onComplete = {
                        navController.navigate(AppRoute.CrowdCall.route) {
                            popUpTo(AppRoute.Shift.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.CrowdCall.route) {
                com.sanket_satpute_20.dailybattle.presentation.crowd.CrowdScreen(
                    uiState = com.sanket_satpute_20.dailybattle.presentation.crowd.CrowdUiState(),
                    onAnswerSelected = {},
                    onComplete = {
                        navController.navigate(AppRoute.Results.route) {
                            popUpTo(AppRoute.CrowdCall.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(AppRoute.Results.route) {
                com.sanket_satpute_20.dailybattle.presentation.result.ResultsScreen(
                    onBeatRivalClick = {
                        navController.navigate(AppRoute.Rival.route)
                    },
                    onShareClick = {}
                )
            }
            composable(AppRoute.Rival.route) {
                com.sanket_satpute_20.dailybattle.presentation.rival.RivalRoute(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPractice = {
                        navController.navigate(AppRoute.Home.route) {
                            popUpTo(AppRoute.MainGraph.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Battle tab placeholder. Retained per `06_NAVIGATION_AND_FLOWS.md` line 149-155:
 * "The exact long-term structure of the Battle tab is not fully defined in the current product specification.
 * Therefore: STATUS: PENDING. Do not invent additional Battle-tab features."
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


