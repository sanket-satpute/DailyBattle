package com.sanket_satpute_20.dailybattle.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppRouteTest {
    @Test
    fun `startup route is a stable non-blank identifier`() {
        assertEquals("startup", AppRoute.Startup.route)
    }
}
