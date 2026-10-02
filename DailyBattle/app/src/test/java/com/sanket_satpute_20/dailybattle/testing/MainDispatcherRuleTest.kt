package com.sanket_satpute_20.dailybattle.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainDispatcherRuleTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `dispatchers main is routed to the test dispatcher`() = runTest {
        var executed = false

        launch(Dispatchers.Main) { executed = true }

        assertEquals(true, executed)
    }
}
