package com.sanket_satpute_20.dailybattle.core.time

import android.os.SystemClock
import javax.inject.Inject

class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun elapsedRealtime(): Long {
        return SystemClock.elapsedRealtime()
    }
}
