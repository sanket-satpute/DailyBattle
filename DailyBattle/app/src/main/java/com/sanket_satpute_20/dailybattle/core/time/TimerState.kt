package com.sanket_satpute_20.dailybattle.core.time

data class TimerState(
    val durationMillis: Long,
    val remainingMillis: Long,
    val isRunning: Boolean,
    val isFinished: Boolean
) {
    companion object {
        val Initial = TimerState(
            durationMillis = 0,
            remainingMillis = 0,
            isRunning = false,
            isFinished = false
        )
    }
}
