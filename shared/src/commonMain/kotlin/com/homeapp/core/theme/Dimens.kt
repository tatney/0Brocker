package com.homeapp.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────────────────────
// PRIMITIVE SCALES
// These stay exactly as they were: they are referenced directly by ~200 legacy
// call sites, and removing them would create churn with zero design benefit.
// New code should prefer the semantic layer further down this file.
// ─────────────────────────────────────────────────────────────────────────────

// Spacing scale
val kSpaceXS = 4.dp
val kSpaceSM = 8.dp
val kSpaceMD = 16.dp
val kSpaceLG = 24.dp
val kSpaceXL = 32.dp

// Border radius
val kRadiusSM = RoundedCornerShape(8.dp)
val kRadiusMD = RoundedCornerShape(12.dp)
val kRadiusLG = RoundedCornerShape(16.dp)
val kRadiusXL = RoundedCornerShape(24.dp)
val kRadiusFull = RoundedCornerShape(100.dp)

// Numeric dp helpers for LinearGradient/Brush-free layouts
val kRadiusSMSize = 8.dp
val kRadiusMDSize = 12.dp
val kRadiusLGSize = 16.dp
val kRadiusXLSize = 24.dp

// ─────────────────────────────────────────────────────────────────────────────
// SEMANTIC LAYER
// Semantic names describe *intent* rather than magnitude, so screens can be
// restyled by editing this file alone. Anything that has a fixed physical
// meaning (touch targets, icon sizes) lives here too so it cannot drift.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Spacing by purpose. Screens should reference these, never raw dp values, so
 * that gutters and section rhythm stay identical across every module.
 */
object AppSpacing {
    /** Inside a chip, badge, or tight inline control. */
    val tight: Dp = 4.dp

    /** Gap between sibling items in a list or a group of controls. */
    val item: Dp = 8.dp

    /** Gap between chips in a rail. */
    val chip: Dp = 8.dp

    /** Gap between distinct cards in a list. */
    val card: Dp = 12.dp

    /** Padding inside a card. */
    val cardInner: Dp = 16.dp

    /** Horizontal screen gutter. All screen content aligns to this. */
    val gutter: Dp = 16.dp

    /** Vertical gap between two top-level screen sections. */
    val section: Dp = 24.dp

    /** Padding inside a hero / gradient banner. */
    val hero: Dp = 24.dp

    /** Top inset applied below a top bar so content clears it. */
    val belowBar: Dp = 8.dp

    /** Extra breathing room at the end of a scrollable screen. */
    val screenEnd: Dp = 32.dp
}

/** Corner radii by surface role. */
object AppRadii {
    /** Chips, tags, pills — fully rounded. */
    val chip = kRadiusFull

    /** Standard content card. */
    val card = RoundedCornerShape(16.dp)

    /** Hero banner / large feature tile. */
    val hero = RoundedCornerShape(24.dp)

    /** Compact card used for list rows. */
    val tile = RoundedCornerShape(18.dp)

    /** Small controls: inline badges, icon containers. */
    val control = RoundedCornerShape(12.dp)

    /** Top-rounded sheet surface. */
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    val controlSize: Dp = 12.dp
    val cardSize: Dp = 16.dp
    val heroSize: Dp = 24.dp
}

/** Elevation by layer. One ramp so shadows never disagree between screens. */
object AppElevation {
    val none: Dp = 0.dp
    /** Resting content card. */
    val card: Dp = 2.dp
    /** Interactive card / raised control. */
    val raised: Dp = 4.dp
    /** Floating overlay: search bar, marker, sheet. */
    val overlay: Dp = 8.dp
}

/** Icon glyph sizes. Keeps optical weight consistent at every call site. */
object AppIconSize {
    val xs: Dp = 14.dp
    val sm: Dp = 18.dp
    val md: Dp = 22.dp
    val lg: Dp = 28.dp
    val xl: Dp = 40.dp

    /** Glyph size inside an [AppSize.avatar]. */
    val inAvatarSm: Dp = 18.dp
    val inAvatarMd: Dp = 26.dp
    val inAvatarLg: Dp = 34.dp
}

/** Fixed component dimensions that must not vary between screens. */
object AppSize {
    /** Minimum interactive target (Material accessibility minimum). */
    val touchTarget: Dp = 48.dp

    /** Height of a primary / secondary button. */
    val button: Dp = 52.dp

    /** Height of a top app bar. */
    val topBar: Dp = 56.dp

    /** Circular icon affordance used for service categories. */
    val categoryCircle: Dp = 56.dp

    val avatarSm: Dp = 40.dp
    val avatarMd: Dp = 56.dp
    val avatarLg: Dp = 72.dp
}
