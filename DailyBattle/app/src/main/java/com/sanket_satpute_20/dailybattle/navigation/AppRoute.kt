package com.sanket_satpute_20.dailybattle.navigation

/**
 * Navigation destinations hosted by [DailyBattleNavHost]. Only the startup destination is approved;
 * primary product destinations (Home/Battle/Friends/Me) belong to the Phase 4 app-shell sprint.
 */
sealed interface AppRoute {
    val route: String

    data object Startup : AppRoute {
        override val route: String = "startup"
    }
}
