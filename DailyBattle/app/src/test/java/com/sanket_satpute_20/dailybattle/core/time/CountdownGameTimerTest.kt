package com.sanket_satpute_20.dailybattle.core.time

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownGameTimerTest {

    private lateinit var timeProvider: FakeTimeProvider
    private lateinit var timer: CountdownGameTimer

    @Before
    fun setup() {
        timeProvider = FakeTimeProvider()
    }

    @Test
    fun verify_initial_state() = runTest {
        timer = CountdownGameTimer(timeProvider, backgroundScope, tickIntervalMs = 100L)
        val state = timer.state.value
        assertEquals(0L, state.remainingMillis)
        assertFalse(state.isRunning)
        assertFalse(state.isFinished)
    }

    @Test
    fun verify_start_timer() = runTest {
        timer = CountdownGameTimer(timeProvider, backgroundScope, tickIntervalMs = 100L)
        timer.start(5000L)
        
        var state = timer.state.value
        assertTrue(state.isRunning)
        assertEquals(5000L, state.remainingMillis)
        assertEquals(5000L, state.durationMillis)
        assertFalse(state.isFinished)

        // Advance 2 seconds
        timeProvider.advance(2000L)
        delay(2000L)

        state = timer.state.value
        assertTrue(state.isRunning)
        assertEquals(3000L, state.remainingMillis)
        assertFalse(state.isFinished)
    }

    @Test
    fun verify_timer_finishes() = runTest {
        timer = CountdownGameTimer(timeProvider, backgroundScope, tickIntervalMs = 100L)
        timer.start(1000L)

        timeProvider.advance(1000L)
        delay(1000L)

        val state = timer.state.value
        assertFalse(state.isRunning)
        assertTrue(state.isFinished)
        assertEquals(0L, state.remainingMillis)
    }

    @Test
    fun verify_pause_and_resume() = runTest {
        timer = CountdownGameTimer(timeProvider, backgroundScope, tickIntervalMs = 100L)
        timer.start(5000L)

        // Run for 2 seconds
        timeProvider.advance(2000L)
        delay(2000L)

        timer.pause()
        
        var state = timer.state.value
        assertFalse(state.isRunning)
        assertEquals(3000L, state.remainingMillis)

        // Time passes while paused (e.g. 5 seconds)
        timeProvider.advance(5000L)
        delay(5000L)

        // State shouldn't change
        state = timer.state.value
        assertFalse(state.isRunning)
        assertEquals(3000L, state.remainingMillis)

        // Resume and run for 1 second
        timer.resume()
        timeProvider.advance(1000L)
        delay(1000L)

        state = timer.state.value
        assertTrue(state.isRunning)
        assertEquals(2000L, state.remainingMillis)
    }

    @Test
    fun verify_restore_running_timer() = runTest {
        timer = CountdownGameTimer(timeProvider, backgroundScope, tickIntervalMs = 100L)
        // App is restored with 3 seconds remaining, was running
        timer.restore(durationMillis = 5000L, remainingMillis = 3000L, wasRunning = true)

        var state = timer.state.value
        assertTrue(state.isRunning)
        assertEquals(3000L, state.remainingMillis)

        // Run for 1 second
        timeProvider.advance(1000L)
        delay(1000L)

        state = timer.state.value
        assertEquals(2000L, state.remainingMillis)
    }

    @Test
    fun verify_restore_paused_timer() = runTest {
        timer = CountdownGameTimer(timeProvider, backgroundScope, tickIntervalMs = 100L)
        timer.restore(durationMillis = 5000L, remainingMillis = 3000L, wasRunning = false)

        var state = timer.state.value
        assertFalse(state.isRunning)
        assertEquals(3000L, state.remainingMillis)

        // Time passes
        timeProvider.advance(2000L)
        delay(2000L)

        state = timer.state.value
        assertFalse(state.isRunning)
        assertEquals(3000L, state.remainingMillis)
    }
}

class FakeTimeProvider : TimeProvider {
    private var currentTime = 0L
    
    override fun elapsedRealtime(): Long {
        return currentTime
    }
    
    fun advance(millis: Long) {
        currentTime += millis
    }
}
