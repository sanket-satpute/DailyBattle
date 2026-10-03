package com.sanket_satpute_20.dailybattle.core.time

interface TimeProvider {
    /** 
     * Returns monotonic time in milliseconds since system boot. 
     * Not affected by system wall-clock changes.
     */
    fun elapsedRealtime(): Long
}
