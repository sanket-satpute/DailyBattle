package com.sanket_satpute_20.dailybattle.navigation

/**
 * Navigation destinations hosted by [DailyBattleNavHost].
 *
 * Root tabs (Home, Battle, Friends, Me) follow `06_NAVIGATION_AND_FLOWS.md` §2.
 */
sealed interface AppRoute {
    val route: String

    data object Startup : AppRoute {
        override val route: String = "startup"
    }

    data object Home : AppRoute {
        override val route: String = "home"
    }

    data object Battle : AppRoute {
        override val route: String = "battle"
    }

    data object Friends : AppRoute {
        override val route: String = "friends"
    }

    data object Me : AppRoute {
        override val route: String = "me"
    }
}
