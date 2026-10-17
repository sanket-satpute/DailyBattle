package com.sanket_satpute_20.dailybattle.design

import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.matrix.DBDeviceMatrix
import com.sanket_satpute_20.dailybattle.design.matrix.DeviceFrameCategory
import com.sanket_satpute_20.dailybattle.design.matrix.LayoutFrame
import com.sanket_satpute_20.dailybattle.design.matrix.ResponsivePolicy
import com.sanket_satpute_20.dailybattle.design.sizing.DBSizing
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Sprint 26.5 — Final Device Matrix Test Suite
 *
 * Validates the approved Android device matrix and required layout sizes per:
 * - `docs/ROADMAP.md` Phase 26, Sprint 26.5 (lines 1972–1978):
 *     "Validate the approved Android device matrix and the required layout sizes:
 *      360 × 800
 *      390 × 844
 *      412 × 915"
 * - `docs/05_DESIGN_SYSTEM.md` §3 (Master Device System), §4 (Safe Area System),
 *   §63 (Touch Targets), §64 (Responsive System), §94 (Core Dimensions — Source of Truth)
 * - `docs/04_SCREEN_BLUEPRINTS.md` §3 (Global Canvas), §4 (Global Layout Rules), §55 (QA Checklist)
 * - `docs/15_ANTIGRAVITY_RULES.md` §35 (Screen Size Rule), §36 (Touch Target Rule)
 * - `docs/16_DEFINITION_OF_DONE.md` §55 (Visual QA DoD: [ ] 390×844, [ ] 412×915, [ ] 360×800)
 */
class Sprint265FinalDeviceMatrixTest {

    // =========================================================================
    // 1. APPROVED MATRIX FRAMES VALIDATION
    // =========================================================================

    @Test
    fun `master design frame conforms strictly to 390 x 844 dp specification`() {
        val master = DBDeviceMatrix.MasterFrame
        assertEquals(DeviceFrameCategory.MASTER, master.category)
        assertEquals(390.dp, master.widthDp)
        assertEquals(844.dp, master.heightDp)
        assertTrue(master.isMasterFrame)
        assertFalse(master.isCompactWidth)
        assertFalse(master.isExpandedWidth)
    }

    @Test
    fun `small validation frame conforms strictly to 360 x 800 dp specification`() {
        val small = DBDeviceMatrix.SmallFrame
        assertEquals(DeviceFrameCategory.SMALL, small.category)
        assertEquals(360.dp, small.widthDp)
        assertEquals(800.dp, small.heightDp)
        assertFalse(small.isMasterFrame)
        assertTrue(small.isCompactWidth)
        assertFalse(small.isExpandedWidth)
    }

    @Test
    fun `large validation frame conforms strictly to 412 x 915 dp specification`() {
        val large = DBDeviceMatrix.LargeFrame
        assertEquals(DeviceFrameCategory.LARGE, large.category)
        assertEquals(412.dp, large.widthDp)
        assertEquals(915.dp, large.heightDp)
        assertFalse(large.isMasterFrame)
        assertFalse(large.isCompactWidth)
        assertTrue(large.isExpandedWidth)
    }

    @Test
    fun `all three approved frames are registered in approved frames matrix`() {
        val frames = DBDeviceMatrix.ApprovedFrames
        assertEquals(3, frames.size)
        assertTrue(frames.contains(DBDeviceMatrix.SmallFrame))
        assertTrue(frames.contains(DBDeviceMatrix.MasterFrame))
        assertTrue(frames.contains(DBDeviceMatrix.LargeFrame))

        assertTrue(DBDeviceMatrix.isApprovedFrame(360.dp, 800.dp))
        assertTrue(DBDeviceMatrix.isApprovedFrame(390.dp, 844.dp))
        assertTrue(DBDeviceMatrix.isApprovedFrame(412.dp, 915.dp))

        // Non-approved dimensions
        assertFalse(DBDeviceMatrix.isApprovedFrame(320.dp, 480.dp))
        assertFalse(DBDeviceMatrix.isApprovedFrame(768.dp, 1024.dp))
    }

    // =========================================================================
    // 2. SAFE AREA AND MARGIN CONSTRAINTS
    // =========================================================================

    @Test
    fun `all frames respect 20dp default page margin and 24dp hero margin`() {
        for (frame in DBDeviceMatrix.ApprovedFrames) {
            assertEquals("Default margin must be 20dp per §4.1 & §94", 20.dp, frame.defaultMargin)
            assertEquals("Hero margin must be 24dp per §4.1 & §94", 24.dp, frame.heroMargin)
            assertEquals("Default margin must mirror DBSizing.ScreenMargin", DBSizing.ScreenMargin, frame.defaultMargin)
            assertEquals("Hero margin must mirror DBSizing.HeroMargin", DBSizing.HeroMargin, frame.heroMargin)
        }
    }

    @Test
    fun `available content width calculates accurately without negative overflow`() {
        // Small: 360 - 2 * 20 = 320 dp
        assertEquals(320.dp, DBDeviceMatrix.SmallFrame.availableContentWidth)
        // Master: 390 - 2 * 20 = 350 dp
        assertEquals(350.dp, DBDeviceMatrix.MasterFrame.availableContentWidth)
        // Large: 412 - 2 * 20 = 372 dp
        assertEquals(372.dp, DBDeviceMatrix.LargeFrame.availableContentWidth)

        // Hero available widths
        assertEquals(312.dp, DBDeviceMatrix.SmallFrame.availableHeroWidth) // 360 - 48
        assertEquals(342.dp, DBDeviceMatrix.MasterFrame.availableHeroWidth) // 390 - 48
        assertEquals(364.dp, DBDeviceMatrix.LargeFrame.availableHeroWidth) // 412 - 48
    }

    // =========================================================================
    // 3. INTERACTIVE TOUCH TARGET & CONTROL HEIGHT CONSTRAINTS
    // =========================================================================

    @Test
    fun `interactive controls meet minimum 44dp and preferred 48dp across all frames`() {
        for (frame in DBDeviceMatrix.ApprovedFrames) {
            assertTrue("Touch target minimum must be >= 44dp per §36", frame.minTouchTarget >= 44.dp)
            assertTrue("Preferred touch target must be >= 48dp per §36", frame.preferredTouchTarget >= 48.dp)
            assertEquals("Primary control height must be 52dp per §94", 52.dp, frame.primaryControlHeight)
        }
    }

    @Test
    fun `bottom navigation height reservation complies with 72 to 80 dp specification`() {
        for (frame in DBDeviceMatrix.ApprovedFrames) {
            assertEquals(72.dp, frame.bottomNavMinHeight)
            assertEquals(80.dp, frame.bottomNavMaxHeight)
            assertTrue("Min bottom nav height must be <= max", frame.bottomNavMinHeight <= frame.bottomNavMaxHeight)
        }
    }

    // =========================================================================
    // 4. RESPONSIVE ADAPTATION POLICIES (§64)
    // =========================================================================

    @Test
    fun `small 360px frame responsive policy enforces gap and padding reduction with content stacking`() {
        val policy = DBDeviceMatrix.resolveResponsivePolicy(DeviceFrameCategory.SMALL)

        assertTrue("Small 360px frame must reduce horizontal gaps per §64.1", policy.reduceHorizontalGaps)
        assertTrue("Small 360px frame must reduce card padding per §64.1", policy.reduceCardPadding)
        assertTrue("Small 360px frame must allow text wrapping per §64.1", policy.allowTextWrapping)
        assertTrue("Small 360px frame must stack content where necessary per §64.1", policy.stackContentWhereNecessary)
        assertFalse("Small 360px frame does not increase breathing room", policy.increasedBreathingRoom)

        assertEquals("Recommended horizontal gap for 360px is 8dp (XS)", DBSpacing.XS, policy.recommendedHorizontalGap)
        assertEquals("Recommended card padding for 360px is 12dp (SM)", DBSpacing.SM, policy.recommendedCardPadding)
    }

    @Test
    fun `master 390px frame responsive policy maintains canonical design baseline`() {
        val policy = DBDeviceMatrix.resolveResponsivePolicy(DeviceFrameCategory.MASTER)

        assertFalse("Master 390px frame does not reduce horizontal gaps", policy.reduceHorizontalGaps)
        assertFalse("Master 390px frame does not reduce card padding", policy.reduceCardPadding)
        assertTrue("Master 390px frame allows text wrapping", policy.allowTextWrapping)
        assertFalse("Master 390px frame does not force content stacking", policy.stackContentWhereNecessary)
        assertTrue("Master 390px frame preserves hierarchy", policy.preservedHierarchy)
        assertTrue("Master 390px frame preserves component sizes", policy.preservedComponentSizes)

        assertEquals("Recommended horizontal gap for 390px is 16dp (MD)", DBSpacing.MD, policy.recommendedHorizontalGap)
        assertEquals("Recommended card padding for 390px is 16dp (MD)", DBSpacing.MD, policy.recommendedCardPadding)
    }

    @Test
    fun `large 412px frame responsive policy enables increased breathing room while preserving component sizes`() {
        val policy = DBDeviceMatrix.resolveResponsivePolicy(DeviceFrameCategory.LARGE)

        assertFalse("Large 412px frame does not reduce horizontal gaps", policy.reduceHorizontalGaps)
        assertFalse("Large 412px frame does not reduce card padding", policy.reduceCardPadding)
        assertTrue("Large 412px frame uses increased breathing room per §64.3", policy.increasedBreathingRoom)
        assertTrue("Large 412px frame preserves hierarchy per §64.3", policy.preservedHierarchy)
        assertTrue("Large 412px frame preserves component sizes without naive scaling per §64.3", policy.preservedComponentSizes)

        assertEquals("Recommended horizontal gap for 412px is 20dp (LG)", DBSpacing.LG, policy.recommendedHorizontalGap)
        assertEquals("Recommended card padding for 412px is 20dp (LG)", DBSpacing.LG, policy.recommendedCardPadding)
    }

    // =========================================================================
    // 5. FRAME RESOLUTION & ASPECT RATIO INVARIANTS
    // =========================================================================

    @Test
    fun `frame resolution maps arbitrary dimensions to nearest approved frame`() {
        // Narrow devices <= 375dp -> Small frame
        assertEquals(DBDeviceMatrix.SmallFrame, DBDeviceMatrix.resolveClosestFrame(360.dp, 800.dp))
        assertEquals(DBDeviceMatrix.SmallFrame, DBDeviceMatrix.resolveClosestFrame(375.dp, 667.dp))

        // Mid-sized devices between 376dp and 400dp -> Master frame
        assertEquals(DBDeviceMatrix.MasterFrame, DBDeviceMatrix.resolveClosestFrame(390.dp, 844.dp))
        assertEquals(DBDeviceMatrix.MasterFrame, DBDeviceMatrix.resolveClosestFrame(393.dp, 852.dp))

        // Wide devices >= 401dp -> Large frame
        assertEquals(DBDeviceMatrix.LargeFrame, DBDeviceMatrix.resolveClosestFrame(412.dp, 915.dp))
        assertEquals(DBDeviceMatrix.LargeFrame, DBDeviceMatrix.resolveClosestFrame(430.dp, 932.dp))
    }

    @Test
    fun `aspect ratios of all approved frames conform to modern smartphone viewport ratios`() {
        for (frame in DBDeviceMatrix.ApprovedFrames) {
            val ratio = frame.aspectRatio
            // 360/800 = 0.45 (9:20), 390/844 = 0.462 (9:19.5), 412/915 = 0.450 (9:20)
            assertTrue("Aspect ratio must be between 0.40 and 0.55 for standard smartphone viewports", ratio in 0.40f..0.55f)
        }
    }
}
