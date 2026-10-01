package com.homeapp.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

// ─────────────────────────────────────────────────────────────────────────────
// BRAND
// ─────────────────────────────────────────────────────────────────────────────
val kPrimaryRed = Color(0xFFCB3549)
val kPrimaryDark = Color(0xFF242B35)
val kAccentTeal = Color(0xFF087F72)
val kAccentPurple = Color(0xFF6C3FC5)
val kAccentGold = Color(0xFFD4A017)
val kWalletNavy = Color(0xFF0D1B4B)

// ─────────────────────────────────────────────────────────────────────────────
// NEUTRALS
// ─────────────────────────────────────────────────────────────────────────────
val kBackground = Color(0xFFF6F5F2)
val kSurface = Color(0xFFFFFFFF)
val kTextPrimary = Color(0xFF252B33)
val kTextSecondary = Color(0xFF68717C)
val kDivider = Color(0xFFE5E5E1)
val kBadgeOrange = Color(0xFFF5A623)

// ─────────────────────────────────────────────────────────────────────────────
// SEMANTIC VARIANTS
// ─────────────────────────────────────────────────────────────────────────────
val kPrimaryRedLight = Color(0xFFFDEBEE)
val kTealLight = Color(0xFFDFF7F2)
val kPurpleLight = Color(0xFFEFE7FB)
val kGoldLight = Color(0xFFFBF3DD)
val kBrownDark = Color(0xFF2D1B0E)
val kDarkSurface = Color(0xFF1C1C24)
val kDarkSurfaceVariant = Color(0xFF232330)
val kDarkBackground = Color(0xFF16161E)
val kSuccessLight = Color(0xFFF0F8F0)
val kTileGrey = Color(0xFFF5F5F5)
val kIconGrey = Color(0xFFE0E0E0)
val kSurfaceGrey = Color(0xFFF8F8F8)
val kWalletNavyLight = Color(0xFF2A3E78)
val kTealDark = Color(0xFF00564D)

// ─────────────────────────────────────────────────────────────────────────────
// SURFACE / ELEVATION TINTS
// Cards stack on [kBackground]; these give each nesting level a slightly
// different plane instead of every surface being flat #FFFFFF.
// ─────────────────────────────────────────────────────────────────────────────

/** Page-level plane. */
val kSurfacePage = Color(0xFFF6F5F2)

/** Standard card plane. */
val kSurfaceCard = Color(0xFFFFFFFF)

/** Nested plane inside a card (rows, stat blocks). */
val kSurfaceRaised = Color(0xFFFAF9F6)

/** Recessed plane inside a card (input fields, code blocks). */
val kSurfaceSunken = Color(0xFFF0EFEB)

/** Hairline used for card and divider strokes. */
val kStrokeSubtle = Color(0xFFE5E5E1)

/** Hairline used for strokes on dark surfaces. */
val kStrokeStrong = Color(0xFFD4D4DC)

// ─────────────────────────────────────────────────────────────────────────────
// SEMANTIC ACCENTS
// Success / warning / info, so status is never a raw literal at a call site.
// ─────────────────────────────────────────────────────────────────────────────
val kStatusSuccess = Color(0xFF00A88F)
val kStatusWarning = Color(0xFFF5A623)
val kStatusInfo = Color(0xFF1E7FD4)
val kStatusNeutral = Color(0xFF9AA0A6)
val kStatusDanger = Color(0xFFE5484D)

val kStatusSuccessLight = Color(0xFFE0F5F0)
val kStatusWarningLight = Color(0xFFFEF3E2)
val kStatusInfoLight = Color(0xFFE4EFFB)
val kStatusNeutralLight = Color(0xFFF0F1F3)
val kStatusDangerLight = Color(0xFFFDEBEC)

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY PALETTE
//
// Every service category owns one recognisable hue, spread around the wheel so
// adjacent categories never read as the same colour. Hues were chosen to sit
// alongside the 0Brocker red primary without competing with it.
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The three-colour treatment for one category: the saturated [accent] used for
 * glyphs, markers and selected states; the soft [container] used for icon
 * backgrounds; and [onContainer], the readable foreground on that container.
 */
data class CategoryColors(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
)

/**
 * Builds a consistent container/onContainer pair from a single accent so a new
 * category only has to declare one colour and still gets a harmonious trio.
 *
 * @param tint how far the accent is blended into the surface behind it.
 */
fun categoryColorsOf(accent: Color, tint: Float = 0.12f): CategoryColors = CategoryColors(
    accent = accent,
    container = lerp(kSurfaceCard, accent, tint),
    onContainer = accent,
)

/** The 15 category hues, indexed by ordinal for O(1) lookup. */
private val CATEGORY_ACCENTS: List<Color> = listOf(
    Color(0xFFCB3549), // ALL          — brand red
    Color(0xFF00A88F), // CLEANING     — teal
    Color(0xFF1E7FD4), // PLUMBING     — azure
    Color(0xFFE0A800), // ELECTRICAL   — amber
    Color(0xFF7A4FD6), // PAINTING     — violet
    Color(0xFFE2662A), // REPAIRS      — orange
    Color(0xFF5B6ABF), // MOVING       — indigo
    Color(0xFFA97142), // CARPENTRY    — timber
    Color(0xFF12A5B0), // AC_REPAIR    — cyan
    Color(0xFF6B8E23), // PEST_CONTROL — olive
    Color(0xFF2E9E5B), // GARDENING    — green
    Color(0xFFC2379A), // INTERIOR     — magenta
    Color(0xFFD6336C), // BEAUTY       — rose
    Color(0xFF44546A), // AUTOMOTIVE   — slate
    Color(0xFF9C3FBF), // APPLIANCE    — violet-magenta
)

/**
 * Resolves the accent for a category ordinal, falling back to the brand red so
 * an unmapped value is still visually correct rather than crashing.
 */
fun categoryAccentAt(ordinal: Int): Color =
    CATEGORY_ACCENTS.getOrElse(ordinal) { CATEGORY_ACCENTS[0] }

/** The accent for a category ordinal, as a full [CategoryColors] trio. */
fun categoryColorsAt(ordinal: Int, tint: Float = 0.12f): CategoryColors =
    categoryColorsOf(categoryAccentAt(ordinal), tint)

// ─────────────────────────────────────────────────────────────────────────────
// PROVIDER STATUS PALETTE
// Replaces the raw 0xFFFF9800 / 0xFF2196F3 / 0xFF9E9E9E literals that were
// duplicated across the map canvas and the preview sheet.
// ─────────────────────────────────────────────────────────────────────────────

/** Full treatment for one provider availability state. */
data class StatusColors(
    val accent: Color,
    val container: Color,
    val onContainer: Color,
    val label: String,
)

private fun statusOf(accent: Color, label: String): StatusColors = StatusColors(
    accent = accent,
    container = lerp(kSurfaceCard, accent, 0.15f),
    onContainer = accent,
    label = label,
)

val kStatusAvailableColors = statusOf(kStatusSuccess, "Available Now")
val kStatusBusyColors = statusOf(kStatusWarning, "Busy")
val kStatusScheduledColors = statusOf(kStatusInfo, "Scheduled")
val kStatusOfflineColors = statusOf(kStatusNeutral, "Offline")

// ─────────────────────────────────────────────────────────────────────────────
// PROPERTY PALETTE
// Listing thumbnails are derived from assetType; listing-type tags get their
// own accents so "For Rent" and "For Sale" are distinguishable at a glance.
// ─────────────────────────────────────────────────────────────────────────────
val kAssetHouse = Color(0xFF1E7FD4)
val kAssetLand = Color(0xFF2E9E5B)
val kAssetCar = Color(0xFF44546A)

val kListingRent = Color(0xFF00A88F)
val kListingBuy = Color(0xFFCB3549)
