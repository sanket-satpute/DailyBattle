package com.sanket_satpute_20.dailybattle.domain.snap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SnapInputCollectorTest {

    @Test
    fun `edge case - target appears`() {
        val collector = SnapInputCollector(timeProvider = { 1000L })
        collector.prepare()
        collector.start()
        
        collector.markTargetAppeared(1500L)
        collector.handleInput(SnapEvent.Tap("target_1", timestampMs = 1800L))
        
        val collectorComplete = SnapInputCollector(timeProvider = { 2000L })
        // Need to simulate time advancing for complete
        // We'll use a local variable to control time
        var currentTime = 1000L
        val timeCollector = SnapInputCollector { currentTime }
        timeCollector.prepare()
        timeCollector.start()
        
        timeCollector.markTargetAppeared(1500L)
        timeCollector.handleInput(SnapEvent.Tap("target_1", timestampMs = 1800L))
        
        currentTime = 2000L
        timeCollector.complete()
        
        val state = timeCollector.currentState as SnapState.Completed
        val result = state.result
        
        assertTrue(result.targetAppeared)
        assertEquals(1500L, result.targetAppearanceTimeMs)
        assertEquals(1, result.taps.size)
        assertEquals(1800L, result.taps[0].timestampMs)
        assertEquals(1000L, result.roundDurationMs) // 2000 - 1000
    }

    @Test
    fun `edge case - target does not appear`() {
        var currentTime = 1000L
        val collector = SnapInputCollector { currentTime }
        collector.prepare()
        collector.start()
        
        // No target spawned
        
        // Timeout reached
        currentTime = 2000L
        collector.handleInput(SnapEvent.Timeout)
        
        currentTime = 2500L
        collector.complete()
        
        val state = collector.currentState as SnapState.Completed
        val result = state.result
        
        assertFalse(result.targetAppeared)
        assertEquals(null, result.targetAppearanceTimeMs)
        assertTrue(result.taps.isEmpty())
        assertEquals(1500L, result.roundDurationMs)
    }

    @Test
    fun `edge case - very fast tap`() {
        val collector = SnapInputCollector()
        collector.prepare()
        collector.start()
        
        collector.markTargetAppeared(100L)
        // User taps exactly when target appears (superhuman / 0ms reaction)
        collector.handleInput(SnapEvent.Tap("target_1", timestampMs = 100L))
        collector.complete()
        
        val result = (collector.currentState as SnapState.Completed).result
        assertEquals(1, result.taps.size)
        assertEquals(100L, result.taps.first().timestampMs)
    }

    @Test
    fun `edge case - very slow tap`() {
        val collector = SnapInputCollector()
        collector.prepare()
        collector.start()
        
        collector.markTargetAppeared(100L)
        // User taps 10 seconds later
        collector.handleInput(SnapEvent.Tap("target_1", timestampMs = 10100L))
        collector.complete()
        
        val result = (collector.currentState as SnapState.Completed).result
        assertEquals(1, result.taps.size)
        assertEquals(10100L, result.taps.first().timestampMs)
    }

    @Test
    fun `edge case - tap before target`() {
        val collector = SnapInputCollector()
        collector.prepare()
        collector.start()
        
        // User taps before target appears (false start)
        collector.handleInput(SnapEvent.Tap("bg", timestampMs = 50L))
        
        collector.markTargetAppeared(200L)
        collector.complete()
        
        val result = (collector.currentState as SnapState.Completed).result
        assertEquals(1, result.taps.size)
        assertEquals(50L, result.taps.first().timestampMs)
    }

    @Test
    fun `edge case - multiple taps`() {
        val collector = SnapInputCollector()
        collector.prepare()
        collector.start()
        
        collector.markTargetAppeared(200L)
        
        collector.handleInput(SnapEvent.Tap("bg", timestampMs = 250L))
        collector.handleInput(SnapEvent.Tap("bg", timestampMs = 280L))
        collector.handleInput(SnapEvent.Tap("target_1", timestampMs = 300L))
        
        collector.complete()
        
        val result = (collector.currentState as SnapState.Completed).result
        assertEquals(3, result.taps.size)
        assertEquals(250L, result.taps[0].timestampMs)
        assertEquals(280L, result.taps[1].timestampMs)
        assertEquals(300L, result.taps[2].timestampMs)
    }
}
