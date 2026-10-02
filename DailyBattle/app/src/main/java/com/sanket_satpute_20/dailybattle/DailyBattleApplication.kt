package com.sanket_satpute_20.dailybattle

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application-level Hilt composition root. Feature bindings are added only when their contracts
 * and implementations are approved.
 */
@HiltAndroidApp
class DailyBattleApplication : Application()
