package com.sanket_satpute_20.dailybattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sanket_satpute_20.dailybattle.navigation.DailyBattleApp
import com.sanket_satpute_20.dailybattle.ui.theme.DailyBattleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyBattleTheme {
                DailyBattleApp()
            }
        }
    }
}

