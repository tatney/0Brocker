package com.homeapp.core.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder

/**
 * Interface and navigation glyphs.
 *
 * These replace the arrow, tick, cross and money-glyph characters that were
 * being rendered from font fallbacks, so affordances look identical on every
 * platform and inherit their colour from `Icon(tint = ...)`.
 */

// ─── Navigation / view modes ────────────────────────────────────────────────

/** Folded map: the map-view mode toggle. */
val IconMap: ImageVector by lazy {
    iconBuilder("App.IconMap").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(2.6f, 6.4f)
                lineTo(8.6f, 3.4f)
                lineTo(15.4f, 6.4f)
                lineTo(21.4f, 3.4f)
                lineTo(21.4f, 17.6f)
                lineTo(15.4f, 20.6f)
                lineTo(8.6f, 17.6f)
                lineTo(2.6f, 20.6f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(8.6f, 3.4f, 8.6f, 17.6f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(15.4f, 6.4f, 15.4f, 20.6f), stroke = ink, strokeWidth = 1.4f)
    }.build()
}

/** Bulleted list: the browse-view mode toggle. */
val IconList: ImageVector by lazy {
    iconBuilder("App.IconList").apply {
        addPath(circlePath(1.3f, 3.6f, 6.6f), fill = ink)
        addPath(circlePath(1.3f, 3.6f, 12f), fill = ink)
        addPath(circlePath(1.3f, 3.6f, 17.4f), fill = ink)
        addPath(polyline(8.4f, 6.6f, 20.8f, 6.6f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(8.4f, 12f, 20.8f, 12f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(8.4f, 17.4f, 20.8f, 17.4f), stroke = ink, strokeWidth = 1.7f)
    }.build()
}

/** Right chevron: the standard "go deeper" affordance. */
val IconChevronRight: ImageVector by lazy {
    iconBuilder("App.IconChevronRight").apply {
        addPath(polyline(9f, 4.8f, 16.4f, 12f, 9f, 19.2f), stroke = ink, strokeWidth = 2.1f)
    }.build()
}

/** Down chevron: expandable disclosures. */
val IconChevronDown: ImageVector by lazy {
    iconBuilder("App.IconChevronDown").apply {
        addPath(polyline(4.8f, 9f, 12f, 16.4f, 19.2f, 9f), stroke = ink, strokeWidth = 2.1f)
    }.build()
}

// ─── Status / affordances ───────────────────────────────────────────────────

/** Plus: add, and zoom in on the map. */
val IconPlus: ImageVector by lazy {
    iconBuilder("App.IconPlus").apply {
        addPath(polyline(12f, 5.4f, 12f, 18.6f), stroke = ink, strokeWidth = 2.2f)
        addPath(polyline(5.4f, 12f, 18.6f, 12f), stroke = ink, strokeWidth = 2.2f)
    }.build()
}

/** Minus: zoom out on the map. */
val IconMinus: ImageVector by lazy {
    iconBuilder("App.IconMinus").apply {
        addPath(polyline(5.4f, 12f, 18.6f, 12f), stroke = ink, strokeWidth = 2.2f)
    }.build()
}

/** Crosshair with corner brackets: re-centre the map on the results. */
val IconFitToScreen: ImageVector by lazy {
    iconBuilder("App.IconFitToScreen").apply {
        // Corner brackets, drawn as four short L-shaped strokes.
        addPath(polyline(3.4f, 8.6f, 3.4f, 3.4f, 8.6f, 3.4f), stroke = ink, strokeWidth = 2.1f)
        addPath(polyline(15.4f, 3.4f, 20.6f, 3.4f, 20.6f, 8.6f), stroke = ink, strokeWidth = 2.1f)
        addPath(polyline(20.6f, 15.4f, 20.6f, 20.6f, 15.4f, 20.6f), stroke = ink, strokeWidth = 2.1f)
        addPath(polyline(8.6f, 20.6f, 3.4f, 20.6f, 3.4f, 15.4f), stroke = ink, strokeWidth = 2.1f)
        // Centre marker.
        addPath(
            pathData = PathBuilder().apply {
                moveTo(15.2f, 8.8f)
                arcToRelative(6.4f, 6.4f, 0f, true, true, 0f, 0.01f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Check mark. */
val IconCheck: ImageVector by lazy {
    iconBuilder("App.IconCheck").apply {
        addPath(polyline(4.8f, 12.6f, 9.6f, 17.4f, 19.2f, 6.4f), stroke = ink, strokeWidth = 2.3f)
    }.build()
}

/** Cross: dismiss and close. */
val IconClose: ImageVector by lazy {
    iconBuilder("App.IconClose").apply {
        addPath(polyline(6.2f, 6.2f, 17.8f, 17.8f), stroke = ink, strokeWidth = 1.9f)
        addPath(polyline(17.8f, 6.2f, 6.2f, 17.8f), stroke = ink, strokeWidth = 1.9f)
    }.build()
}

/** Shield with a check: the verified badge. */
val IconShieldCheck: ImageVector by lazy {
    iconBuilder("App.IconShieldCheck").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 2.8f)
                lineTo(20.2f, 6f)
                verticalLineTo(12f)
                quadTo(20.2f, 18.2f, 12f, 21.2f)
                quadTo(3.8f, 18.2f, 3.8f, 12f)
                verticalLineTo(6f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(8.6f, 11.8f, 11.2f, 14.4f, 15.6f, 9.6f), stroke = ink, strokeWidth = 2f)
    }.build()
}

/** Sparkle: promotions and "new" markers. */
val IconSparkle: ImageVector by lazy {
    iconBuilder("App.IconSparkle").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(11f, 2.6f)
                quadTo(12.4f, 8f, 17.8f, 9.4f)
                quadTo(12.4f, 10.8f, 11f, 16.2f)
                quadTo(9.6f, 10.8f, 4.2f, 9.4f)
                quadTo(9.6f, 8f, 11f, 2.6f)
                close()
            }.nodes,
            fill = ink,
        )
        addPath(
            pathData = PathBuilder().apply {
                moveTo(18.2f, 14.6f)
                quadTo(19f, 17.4f, 21.6f, 18.2f)
                quadTo(19f, 19f, 18.2f, 21.8f)
                quadTo(17.4f, 19f, 14.8f, 18.2f)
                quadTo(17.4f, 17.4f, 18.2f, 14.6f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Two horizontal sliders: the sort / filter control. */
val IconSliders: ImageVector by lazy {
    iconBuilder("App.IconSliders").apply {
        addPath(polyline(3.6f, 7.2f, 20.4f, 7.2f), stroke = ink, strokeWidth = 1.6f)
        addPath(circlePath(1.8f, 9.4f, 7.2f), fill = ink)
        addPath(polyline(3.6f, 16.8f, 20.4f, 16.8f), stroke = ink, strokeWidth = 1.6f)
        addPath(circlePath(1.8f, 14.6f, 16.8f), fill = ink)
    }.build()
}

// ─── Payments ───────────────────────────────────────────────────────────────

/** Paper plane: send money. */
val IconSend: ImageVector by lazy {
    iconBuilder("App.IconSend").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(21.4f, 2.6f)
                lineTo(2.6f, 10.4f)
                lineTo(10.6f, 13.4f)
                lineTo(13.6f, 21.4f)
                close()
            }.nodes,
            fill = ink,
        )
        addPath(polyline(21.4f, 2.6f, 10.6f, 13.4f), stroke = ink, strokeWidth = 1.4f)
    }.build()
}

/** Arrow into a tray: money in. */
val IconDownload: ImageVector by lazy {
    iconBuilder("App.IconDownload").apply {
        addPath(polyline(12f, 3.2f, 12f, 14.6f), stroke = ink, strokeWidth = 1.9f)
        addPath(polyline(7.4f, 9.8f, 12f, 14.4f, 16.6f, 9.8f), stroke = ink, strokeWidth = 1.9f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.6f, 14.4f)
                verticalLineTo(18.2f)
                quadTo(3.6f, 20.8f, 6.6f, 20.8f)
                lineTo(17.4f, 20.8f)
                quadTo(20.4f, 20.8f, 20.4f, 18.2f)
                verticalLineTo(14.4f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.7f,
        )
    }.build()
}

/** Arrow out of a tray: money out. */
val IconUpload: ImageVector by lazy {
    iconBuilder("App.IconUpload").apply {
        addPath(polyline(12f, 14.6f, 12f, 3.2f), stroke = ink, strokeWidth = 1.9f)
        addPath(polyline(7.4f, 8f, 12f, 3.4f, 16.6f, 8f), stroke = ink, strokeWidth = 1.9f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.6f, 14.4f)
                verticalLineTo(18.2f)
                quadTo(3.6f, 20.8f, 6.6f, 20.8f)
                lineTo(17.4f, 20.8f)
                quadTo(20.4f, 20.8f, 20.4f, 18.2f)
                verticalLineTo(14.4f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.7f,
        )
    }.build()
}

/** QR frame: scan and pay. */
val IconQrScan: ImageVector by lazy {
    iconBuilder("App.IconQrScan").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.4f, 9f)
                verticalLineTo(4.4f)
                quadTo(3.4f, 3.4f, 8.4f, 3.4f)
                moveTo(3.4f, 3.4f)
                horizontalLineTo(8.4f)

                moveTo(15.6f, 3.4f)
                quadTo(20.6f, 3.4f, 20.6f, 8.4f)
                verticalLineTo(9f)
                moveTo(15.6f, 3.4f)
                horizontalLineTo(20.6f)

                moveTo(20.6f, 15.6f)
                verticalLineTo(20.6f)
                quadTo(20.6f, 20.6f, 15.6f, 20.6f)
                moveTo(20.6f, 20.6f)
                horizontalLineTo(15.6f)

                moveTo(8.4f, 20.6f)
                quadTo(3.4f, 20.6f, 3.4f, 15.6f)
                verticalLineTo(9f)
                moveTo(8.4f, 20.6f)
                horizontalLineTo(3.4f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.7f,
        )
        addPath(circlePath(1.3f, 8.4f, 8.4f), fill = ink)
        addPath(circlePath(1.3f, 15.6f, 8.4f), fill = ink)
        addPath(circlePath(1.3f, 8.4f, 15.6f), fill = ink)
    }.build()
}

/** Bar chart: statements and analytics. */
val IconChart: ImageVector by lazy {
    iconBuilder("App.IconChart").apply {
        addPath(polyline(3.4f, 20.4f, 20.6f, 20.4f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(6.4f, 20.4f, 6.4f, 13.4f), stroke = ink, strokeWidth = 2.4f)
        addPath(polyline(10.4f, 20.4f, 10.4f, 8.4f), stroke = ink, strokeWidth = 2.4f)
        addPath(polyline(14.4f, 20.4f, 14.4f, 4.4f), stroke = ink, strokeWidth = 2.4f)
        addPath(polyline(18.4f, 20.4f, 18.4f, 10.4f), stroke = ink, strokeWidth = 2.4f)
    }.build()
}

// ─── Service metadata ───────────────────────────────────────────────────────

/** Briefcase: jobs completed. */
val IconBriefcase: ImageVector by lazy {
    iconBuilder("App.IconBriefcase").apply {
        addPath(roundedRectPath(2.4f, 7f, 21.6f, 19.4f, 2.6f), stroke = ink, strokeWidth = 1.6f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(9f, 7f)
                verticalLineTo(5.6f)
                quadTo(9f, 4.8f, 10.4f, 4.8f)
                lineTo(13.6f, 4.8f)
                quadTo(15f, 4.8f, 15f, 5.6f)
                verticalLineTo(7f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(2.4f, 12.8f, 21.6f, 12.8f), stroke = ink, strokeWidth = 1.4f)
    }.build()
}

/** Speedometer: arrival time and distance stats. */
val IconGauge: ImageVector by lazy {
    iconBuilder("App.IconGauge").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.2f, 17.6f)
                quadTo(3.2f, 6.6f, 12f, 6.6f)
                quadTo(20.8f, 6.6f, 20.8f, 17.6f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.7f,
        )
        addPath(polyline(12f, 17.2f, 16.6f, 10.6f), stroke = ink, strokeWidth = 1.9f)
        addPath(circlePath(1.3f, 12f, 17.2f), fill = ink)
    }.build()
}

/** Clock: ETA and scheduling. */
val IconClock: ImageVector by lazy {
    iconBuilder("App.IconClock").apply {
        addPath(circlePath(9f, 12f, 12f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(12f, 6.8f, 12f, 12f, 15.8f, 12f), stroke = ink, strokeWidth = 1.7f)
    }.build()
}
