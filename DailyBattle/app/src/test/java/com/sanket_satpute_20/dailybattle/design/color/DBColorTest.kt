package com.sanket_satpute_20.dailybattle.design.color

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pins every token to its locked hex value from `05_DESIGN_SYSTEM.md` sections 91/92. */
class DBColorTest {
    @Test
    fun `background and surface tokens match the locked source of truth`() {
        assertEquals(Color(0xFF080B10), DBColor.BackgroundPrimary)
        assertEquals(Color(0xFF11161F), DBColor.Surface1)
        assertEquals(Color(0xFF171D28), DBColor.Surface2)
        assertEquals(Color(0xFF1C2330), DBColor.SurfaceElevated)
    }

    @Test
    fun `text tokens match the locked source of truth`() {
        assertEquals(Color(0xFFF5F7FA), DBColor.TextPrimary)
        assertEquals(Color(0xFFA2AAB8), DBColor.TextSecondary)
        assertEquals(Color(0xFF6F7887), DBColor.TextMuted)
        assertEquals(Color(0xFF4D5563), DBColor.TextDisabled)
    }

    @Test
    fun `brand and semantic tokens match the locked source of truth`() {
        assertEquals(Color(0xFF7C5CFC), DBColor.BrandPrimary)
        assertEquals(Color(0xFF39D98A), DBColor.Success)
        assertEquals(Color(0xFFFFB84D), DBColor.Warning)
        assertEquals(Color(0xFFFF5D73), DBColor.Error)
        assertEquals(Color(0xFF4EA8FF), DBColor.Info)
    }

    @Test
    fun `challenge accent tokens match the locked source of truth`() {
        assertEquals(Color(0xFF4EA8FF), DBColor.Snap)
        assertEquals(Color(0xFFFFB84D), DBColor.Shift)
        assertEquals(Color(0xFF39D98A), DBColor.Crowd)
    }

    @Test
    fun `border token matches the locked source of truth`() {
        assertEquals(Color(0xFF252D39), DBColor.Border)
    }
}
