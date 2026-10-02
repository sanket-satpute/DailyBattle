package com.sanket_satpute_20.dailybattle.navigation

/**
 * Navigation destinations hosted by [DailyBattleNavHost].
 *
 * All approved screens (SCR-001 through SCR-013) are registered per Sprint 4.2.
 */
sealed interface AppRoute {
    val route: String

    // --- Graphs ---
    data object StartupGraph : AppRoute {
        override val route: String = "startup_graph"
    }
    
    data object MainGraph : AppRoute {
        override val route: String = "main_graph"
    }

    data object GameplayGraph : AppRoute {
        override val route: String = "gameplay_graph"
    }

    // --- Screens ---

    data object Startup : AppRoute {
        override val route: String = "startup"
    }

    data object Welcome : AppRoute {
        override val route: String = "welcome"
    }

    data object BattleName : AppRoute {
        override val route: String = "battle_name"
    }

    data object Home : AppRoute {
        override val route: String = "home"
    }

    data object Battle : AppRoute {
        override val route: String = "battle"
    }

    data object BattleIntro : AppRoute {
        override val route: String = "battle_intro"
    }

    data object Snap : AppRoute {
        override val route: String = "snap"
    }

    data object Shift : AppRoute {
        override val route: String = "shift"
    }

    data object CrowdCall : AppRoute {
        override val route: String = "crowd_call"
    }

    data object Results : AppRoute {
        override val route: String = "results"
    }

    data object Rival : AppRoute {
        override val route: String = "rival"
    }

    data object Friends : AppRoute {
        override val route: String = "friends"
    }

    data object AddFriend : AppRoute {
        override val route: String = "add_friend"
    }

    data object Profile : AppRoute {
        override val route: String = "profile"
    }

    data object History : AppRoute {
        override val route: String = "history"
    }

    // Root Tab for Me (which might encapsulate Profile, History, etc. depending on Phase 6)
    data object Me : AppRoute {
        override val route: String = "me"
    }
}
