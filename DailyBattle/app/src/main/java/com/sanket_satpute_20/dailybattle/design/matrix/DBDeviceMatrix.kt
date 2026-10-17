package com.sanket_satpute_20.dailybattle.design.matrix

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sanket_satpute_20.dailybattle.design.sizing.DBSizing
import com.sanket_satpute_20.dailybattle.design.spacing.DBSpacing

/**
 * Approved Device Frame Categories per:
 * - `05_DESIGN_SYSTEM.md` §3 (Master Device System)
 * - `04_SCREEN_BLUEPRINTS.md` §3 (Global Canvas)
 * - `15_ANTIGRAVITY_RULES.md` §35 (Screen Size Rule)
 */
enum class DeviceFrameCategory(val label: String) {
    /** 360 × 800 dp: Small validation frame. */
    SMALL("Small Validation Frame (360×800)"),

    /** 390 × 844 dp: Master design frame (authoritative source of truth). */
    MASTER("Master Design Frame (390×844)"),

    /** 412 × 915 dp: Large validation frame. */
    LARGE("Large Validation Frame (412×915)")
}

/**
 * Responsive Adaptation Policy per `05_DESIGN_SYSTEM.md` §64 (Responsive System).
 *
 * Rules:
 * - 360px: Do NOT drastically shrink typography. Instead: reduce horizontal gaps,
 *   reduce card padding, allow text wrapping, stack content where necessary (§64.1).
 * - 390px: Master design baseline (§64.2).
 * - 412px: Use increased breathing room, preserve hierarchy, preserve component sizes (§64.3).
 */
data class ResponsivePolicy(
    val category: DeviceFrameCategory,
    val reduceHorizontalGaps: Boolean,
    val reduceCardPadding: Boolean,
    val allowTextWrapping: Boolean,
    val stackContentWhereNecessary: Boolean,
    val increasedBreathingRoom: Boolean,
    val preservedHierarchy: Boolean,
    val preservedComponentSizes: Boolean,
    val recommendedHorizontalGap: Dp,
    val recommendedCardPadding: Dp
)

/**
 * Encapsulation of an approved layout frame in the Daily Battle Device Matrix.
 *
 * Sourced from:
 * - `05_DESIGN_SYSTEM.md` §3, §4, §63, §94
 * - `04_SCREEN_BLUEPRINTS.md` §3, §4, §55
 */
data class LayoutFrame(
    val category: DeviceFrameCategory,
    val widthDp: Dp,
    val heightDp: Dp,
    val defaultMargin: Dp = DBSizing.ScreenMargin, // 20 dp
    val heroMargin: Dp = DBSizing.HeroMargin,       // 24 dp
    val minTouchTarget: Dp = DBSizing.TouchMinimum,  // 44 dp
    val preferredTouchTarget: Dp = DBSizing.TouchPreferred, // 48 dp
    val primaryControlHeight: Dp = DBSizing.ButtonHeight,   // 52 dp
    val bottomNavMinHeight: Dp = 72.dp,
    val bottomNavMaxHeight: Dp = 80.dp
) {
    /** Available content width between default screen margins: width - 2 * 20dp. */
    val availableContentWidth: Dp
        get() = widthDp - (defaultMargin * 2)

    /** Available hero content width between hero margins: width - 2 * 24dp. */
    val availableHeroWidth: Dp
        get() = widthDp - (heroMargin * 2)

    /** Aspect ratio (width / height). */
    val aspectRatio: Float
        get() = widthDp.value / heightDp.value

    /** Indicates whether this frame requires compact horizontal adaptation (§64.1). */
    val isCompactWidth: Boolean
        get() = widthDp <= 360.dp

    /** Indicates whether this frame utilizes expanded breathing room (§64.3). */
    val isExpandedWidth: Boolean
        get() = widthDp >= 412.dp

    /** Whether the frame conforms to the master specification (390 × 844). */
    val isMasterFrame: Boolean
        get() = category == DeviceFrameCategory.MASTER
}

/**
 * Authoritative Device Matrix for Daily Battle.
 *
 * Defines and validates the approved Android device matrix and required layout sizes:
 * - Master Design Frame: 390 × 844
 * - Small Validation Frame: 360 × 800
 * - Large Validation Frame: 412 × 915
 *
 * Per `docs/ROADMAP.md` Phase 26, Sprint 26.5 (lines 1972–1978).
 */
object DBDeviceMatrix {

    /** Master Frame: 390 × 844 dp (§3.1, §94). */
    val MasterFrame = LayoutFrame(
        category = DeviceFrameCategory.MASTER,
        widthDp = 390.dp,
        heightDp = 844.dp
    )

    /** Small Validation Frame: 360 × 800 dp (§3.2, §64.1). */
    val SmallFrame = LayoutFrame(
        category = DeviceFrameCategory.SMALL,
        widthDp = 360.dp,
        heightDp = 800.dp
    )

    /** Large Validation Frame: 412 × 915 dp (§3.2, §64.3). */
    val LargeFrame = LayoutFrame(
        category = DeviceFrameCategory.LARGE,
        widthDp = 412.dp,
        heightDp = 915.dp
    )

    /** All approved device frames in the release validation matrix. */
    val ApprovedFrames: List<LayoutFrame> = listOf(
        SmallFrame,
        MasterFrame,
        LargeFrame
    )

    /**
     * Resolves the corresponding [ResponsivePolicy] for a given device frame category
     * per `05_DESIGN_SYSTEM.md` §64.
     */
    fun resolveResponsivePolicy(category: DeviceFrameCategory): ResponsivePolicy {
        return when (category) {
            DeviceFrameCategory.SMALL -> ResponsivePolicy(
                category = DeviceFrameCategory.SMALL,
                reduceHorizontalGaps = true,
                reduceCardPadding = true,
                allowTextWrapping = true,
                stackContentWhereNecessary = true,
                increasedBreathingRoom = false,
                preservedHierarchy = true,
                preservedComponentSizes = false,
                recommendedHorizontalGap = DBSpacing.XS, // 8 dp
                recommendedCardPadding = DBSpacing.SM     // 12 dp
            )
            DeviceFrameCategory.MASTER -> ResponsivePolicy(
                category = DeviceFrameCategory.MASTER,
                reduceHorizontalGaps = false,
                reduceCardPadding = false,
                allowTextWrapping = true,
                stackContentWhereNecessary = false,
                increasedBreathingRoom = false,
                preservedHierarchy = true,
                preservedComponentSizes = true,
                recommendedHorizontalGap = DBSpacing.MD, // 16 dp
                recommendedCardPadding = DBSpacing.MD    // 16 dp
            )
            DeviceFrameCategory.LARGE -> ResponsivePolicy(
                category = DeviceFrameCategory.LARGE,
                reduceHorizontalGaps = false,
                reduceCardPadding = false,
                allowTextWrapping = true,
                stackContentWhereNecessary = false,
                increasedBreathingRoom = true,
                preservedHierarchy = true,
                preservedComponentSizes = true,
                recommendedHorizontalGap = DBSpacing.LG, // 20 dp
                recommendedCardPadding = DBSpacing.LG    // 20 dp
            )
        }
    }

    /**
     * Resolves the closest approved layout frame for any arbitrary viewport dimensions.
     */
    fun resolveClosestFrame(widthDp: Dp, heightDp: Dp): LayoutFrame {
        val widthVal = widthDp.value
        return when {
            widthVal <= 375f -> SmallFrame
            widthVal >= 401f -> LargeFrame
            else -> MasterFrame
        }
    }

    /**
     * Verifies whether the supplied width and height match one of the three approved frames.
     */
    fun isApprovedFrame(widthDp: Dp, heightDp: Dp): Boolean {
        return ApprovedFrames.any { it.widthDp == widthDp && it.heightDp == heightDp }
    }
}
