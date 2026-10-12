package com.sanket_satpute_20.dailybattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sanket_satpute_20.dailybattle.core.lifecycle.AppLifecycleManager
import com.sanket_satpute_20.dailybattle.core.lifecycle.BackgroundReason
import com.sanket_satpute_20.dailybattle.navigation.DailyBattleApp
import com.sanket_satpute_20.dailybattle.ui.theme.DailyBattleTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var lifecycleManager: AppLifecycleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyBattleTheme {
                DailyBattleApp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleManager.onForeground()
    }

    override fun onStop() {
        super.onStop()
        lifecycleManager.onBackground(BackgroundReason.USER_NAVIGATION)
    }
}

