package com.sanket_satpute_20.dailybattle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.sanket_satpute_20.dailybattle.navigation.DailyBattleNavHost
import com.sanket_satpute_20.dailybattle.ui.theme.DailyBattleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyBattleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DailyBattleNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
