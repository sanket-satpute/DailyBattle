package com.sanket_satpute_20.dailybattle.core.lifecycle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class AppLifecycleRecreationTest {

    private lateinit var lifecycleManager: DefaultAppLifecycleManager
    private val clock = Clock.fixed(Instant.ofEpochMilli(500_000L), ZoneId.of("UTC"))

    @Before
    fun setUp() {
        lifecycleManager = DefaultAppLifecycleManager(clock)
    }

    @Test
    fun onRecreation_screenRecreation_dispatchesEventToListener() {
        var receivedEvent: AppLifecycleEvent? = null
        lifecycleManager.addListener { event ->
            receivedEvent = event
        }

        lifecycleManager.onRecreation(isProcessRecreation = false)

        assertTrue(receivedEvent is AppLifecycleEvent.Recreated)
        val recreated = receivedEvent as AppLifecycleEvent.Recreated
        assertEquals(500_000L, recreated.timestamp)
        assertFalse(recreated.isProcessRecreation)
    }

    @Test
    fun onRecreation_processRecreation_dispatchesEventToListener() {
        var receivedEvent: AppLifecycleEvent? = null
        lifecycleManager.addListener { event ->
            receivedEvent = event
        }

        lifecycleManager.onRecreation(isProcessRecreation = true)

        assertTrue(receivedEvent is AppLifecycleEvent.Recreated)
        val recreated = receivedEvent as AppLifecycleEvent.Recreated
        assertEquals(500_000L, recreated.timestamp)
        assertTrue(recreated.isProcessRecreation)
    }
}
