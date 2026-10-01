package com.homeapp.core.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder

/**
 * Service-category glyphs.
 *
 * One glyph per [com.homeapp.data.model.ServiceCategory], drawn in the same
 * house style as [AppIcons.kt]: 24-unit viewport, 1.8dp round-cap strokes,
 * centre-aligned on a 12,12 optical centre so they sit correctly inside a
 * circular chip or a map pin.
 *
 * These replace the emoji that used to distinguish categories, which rendered
 * differently on every platform and carried no consistent colour.
 */

// ─── Cleaning ───────────────────────────────────────────────────────────────

/** Broom: handle, flared head, bristles. */
val IconBroom: ImageVector by lazy {
    iconBuilder("App.IconBroom").apply {
        addPath(polyline(12f, 3f, 12f, 12.2f), stroke = ink, strokeWidth = 2.2f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(7.4f, 12.2f)
                lineTo(16.6f, 12.2f)
                lineTo(15f, 20.6f)
                quadTo(12f, 21.9f, 9f, 20.6f)
                close()
            }.nodes,
            fill = ink,
        )
        addPath(polyline(9.8f, 15f, 9.8f, 18.8f), stroke = ink, strokeWidth = 1.1f)
        addPath(polyline(14.2f, 15f, 14.2f, 18.8f), stroke = ink, strokeWidth = 1.1f)
    }.build()
}

// ─── Plumbing ───────────────────────────────────────────────────────────────

/** Box-end wrench: shaft plus closed ring head. */
val IconWrench: ImageVector by lazy {
    iconBuilder("App.IconWrench").apply {
        addPath(polyline(4.6f, 19.4f, 14.2f, 9.8f), stroke = ink, strokeWidth = 2.4f)
        addPath(circlePath(4.2f, 17.4f, 6.6f), stroke = ink, strokeWidth = 2.2f)
        addPath(circlePath(1.3f, 17.4f, 6.6f), fill = ink)
    }.build()
}

/** Water droplet: bathroom, kitchen and water-line work. */
val IconDroplet: ImageVector by lazy {
    iconBuilder("App.IconDroplet").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 3.2f)
                quadTo(19.2f, 11.2f, 19.2f, 15.2f)
                quadTo(19.2f, 19.8f, 12f, 19.8f)
                quadTo(4.8f, 19.8f, 4.8f, 15.2f)
                quadTo(4.8f, 11.2f, 12f, 3.2f)
                close()
            }.nodes,
            stroke = ink,
        )
    }.build()
}

// ─── Electrical ─────────────────────────────────────────────────────────────

/** Lightning bolt. */
val IconBolt: ImageVector by lazy {
    iconBuilder("App.IconBolt").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(13.8f, 2.2f)
                lineTo(5.2f, 13.4f)
                lineTo(10.9f, 13.4f)
                lineTo(10.2f, 21.8f)
                lineTo(18.8f, 10.6f)
                lineTo(13.1f, 10.6f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

// ─── Painting ───────────────────────────────────────────────────────────────

/** Paint roller: roller head, arm, grip. */
val IconPaintRoller: ImageVector by lazy {
    iconBuilder("App.IconPaintRoller").apply {
        addPath(roundedRectPath(3f, 4f, 14f, 8.6f, 2.2f), fill = ink)
        addPath(polyline(14f, 6.3f, 16.8f, 6.3f, 16.8f, 10f), stroke = ink, strokeWidth = 1.6f)
        addPath(roundedRectPath(15.2f, 10f, 18.4f, 20.6f, 1.6f), fill = ink)
    }.build()
}

// ─── Repairs ────────────────────────────────────────────────────────────────

/** Claw hammer. */
val IconHammer: ImageVector by lazy {
    iconBuilder("App.IconHammer").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12.4f, 5.4f)
                lineTo(18.8f, 2.7f)
                lineTo(21.3f, 7.5f)
                lineTo(14.9f, 10.2f)
                close()
            }.nodes,
            fill = ink,
        )
        addPath(polyline(13.9f, 8.8f, 4.6f, 18.2f), stroke = ink, strokeWidth = 2.2f)
    }.build()
}

// ─── Moving ─────────────────────────────────────────────────────────────────

/** Box truck. */
val IconTruck: ImageVector by lazy {
    iconBuilder("App.IconTruck").apply {
        addPath(roundedRectPath(1.6f, 6.4f, 13.4f, 17f, 1.6f), stroke = ink, strokeWidth = 1.6f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(13.4f, 9.6f)
                lineTo(17.4f, 9.6f)
                quadTo(19.4f, 9.8f, 20.2f, 11.6f)
                lineTo(22.2f, 15.2f)
                verticalLineTo(17f)
                horizontalLineTo(13.4f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(circlePath(1.8f, 6.6f, 17.8f), fill = ink)
        addPath(circlePath(1.8f, 17.6f, 17.8f), fill = ink)
    }.build()
}

/** Sealed parcel: used for packing / logistics surfaces. */
val IconBox: ImageVector by lazy {
    iconBuilder("App.IconBox").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.2f, 7.4f)
                lineTo(12f, 3.2f)
                lineTo(20.8f, 7.4f)
                lineTo(20.8f, 16.6f)
                lineTo(12f, 20.8f)
                lineTo(3.2f, 16.6f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(3.2f, 7.4f, 20.8f, 7.4f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(12f, 11.6f, 12f, 20.8f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(8.6f, 5.3f, 8.6f, 18.7f), stroke = ink, strokeWidth = 1.4f)
    }.build()
}

// ─── Carpentry ──────────────────────────────────────────────────────────────

/** Hand saw. */
val IconSaw: ImageVector by lazy {
    iconBuilder("App.IconSaw").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(2.8f, 16.4f)
                lineTo(14.6f, 5.4f)
                lineTo(21.2f, 5.4f)
                lineTo(21.2f, 11.6f)
                lineTo(9.4f, 20.4f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.4f, 17.2f)
                lineTo(6f, 19.8f)
                lineTo(5.4f, 21.2f)
                lineTo(3.4f, 19.2f)
                lineTo(1.6f, 21f)
                lineTo(0.6f, 20f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Workbench. */
val IconWorkbench: ImageVector by lazy {
    iconBuilder("App.IconWorkbench").apply {
        addPath(roundedRectPath(2.4f, 7.4f, 21.6f, 10.4f, 1.4f), fill = ink)
        addPath(polyline(5.4f, 10.4f, 5.4f, 20.6f), stroke = ink, strokeWidth = 1.8f)
        addPath(polyline(18.6f, 10.4f, 18.6f, 20.6f), stroke = ink, strokeWidth = 1.8f)
        addPath(roundedRectPath(6.6f, 13.4f, 12.4f, 15.2f, 0.9f), stroke = ink, strokeWidth = 1.2f)
    }.build()
}

// ─── AC ─────────────────────────────────────────────────────────────────────

/** Six-armed snowflake. */
val IconSnowflake: ImageVector by lazy {
    iconBuilder("App.IconSnowflake").apply {
        addPath(polyline(12f, 2.4f, 12f, 21.6f), stroke = ink, strokeWidth = 1.6f)
        addPath(polyline(2.4f, 12f, 21.6f, 12f), stroke = ink, strokeWidth = 1.6f)
        addPath(polyline(5.2f, 5.2f, 18.8f, 18.8f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(9f, 5.2f, 12f, 7.6f, 15f, 5.2f), stroke = ink, strokeWidth = 1.3f)
        addPath(polyline(9f, 18.8f, 12f, 16.4f, 15f, 18.8f), stroke = ink, strokeWidth = 1.3f)
        addPath(polyline(5.2f, 9f, 7.6f, 12f, 5.2f, 15f), stroke = ink, strokeWidth = 1.3f)
        addPath(polyline(18.8f, 9f, 16.4f, 12f, 18.8f, 15f), stroke = ink, strokeWidth = 1.3f)
    }.build()
}

// ─── Pest control ───────────────────────────────────────────────────────────

/** Beetle. */
val IconBug: ImageVector by lazy {
    iconBuilder("App.IconBug").apply {
        addPath(roundedRectPath(7.6f, 6.6f, 16.4f, 19.6f, 4.4f), fill = ink)
        addPath(circlePath(2.2f, 12f, 5.4f), fill = ink)
        addPath(polyline(10.4f, 5f, 8.6f, 2.6f), stroke = ink, strokeWidth = 1.1f)
        addPath(polyline(13.6f, 5f, 15.4f, 2.6f), stroke = ink, strokeWidth = 1.1f)
        addPath(polyline(7.6f, 10f, 3.2f, 8f), stroke = ink, strokeWidth = 1.2f)
        addPath(polyline(7.6f, 13.2f, 2.6f, 13.2f), stroke = ink, strokeWidth = 1.2f)
        addPath(polyline(7.6f, 16.4f, 3.2f, 18.4f), stroke = ink, strokeWidth = 1.2f)
        addPath(polyline(16.4f, 10f, 20.8f, 8f), stroke = ink, strokeWidth = 1.2f)
        addPath(polyline(16.4f, 13.2f, 21.4f, 13.2f), stroke = ink, strokeWidth = 1.2f)
        addPath(polyline(16.4f, 16.4f, 20.8f, 18.4f), stroke = ink, strokeWidth = 1.2f)
    }.build()
}

// ─── Gardening ──────────────────────────────────────────────────────────────

/** Leaf with stem and midrib. */
val IconLeaf: ImageVector by lazy {
    iconBuilder("App.IconLeaf").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(20.6f, 3.4f)
                quadTo(6.4f, 3.8f, 5.4f, 13.6f)
                quadTo(4.8f, 20.6f, 12f, 20.6f)
                quadTo(19.4f, 20.6f, 20.4f, 12.4f)
                quadTo(20.9f, 7.2f, 20.6f, 3.4f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3.6f, 21.2f)
                quadTo(10.5f, 15f, 20.2f, 4.6f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(8.4f, 16.2f, 8.4f, 9.4f), stroke = ink, strokeWidth = 1.1f)
        addPath(polyline(8.4f, 12.6f, 14.6f, 12.6f), stroke = ink, strokeWidth = 1.1f)
    }.build()
}

/** Parked tree, for land and garden plots. */
val IconTree: ImageVector by lazy {
    iconBuilder("App.IconTree").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 2.8f)
                lineTo(18f, 9.4f)
                horizontalLineTo(6f)
                close()
            }.nodes,
            fill = ink,
        )
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 7.2f)
                lineTo(19.2f, 15.4f)
                horizontalLineTo(4.8f)
                close()
            }.nodes,
            fill = ink,
        )
        addPath(polyline(12f, 15.4f, 12f, 21.2f), stroke = ink, strokeWidth = 2f)
    }.build()
}

// ─── Interior design ────────────────────────────────────────────────────────

/** Two-seat sofa. */
val IconSofa: ImageVector by lazy {
    iconBuilder("App.IconSofa").apply {
        addPath(roundedRectPath(4.2f, 7.4f, 19.8f, 17.6f, 2.4f), stroke = ink, strokeWidth = 1.6f)
        addPath(roundedRectPath(1.8f, 11.4f, 5.4f, 17.8f, 1.8f), fill = ink)
        addPath(roundedRectPath(18.6f, 11.4f, 22.2f, 17.8f, 1.8f), fill = ink)
        addPath(polyline(5.4f, 13.4f, 18.6f, 13.4f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(6f, 17.8f, 6f, 20.4f), stroke = ink, strokeWidth = 1.4f)
        addPath(polyline(18f, 17.8f, 18f, 20.4f), stroke = ink, strokeWidth = 1.4f)
    }.build()
}

/** Picture frame. */
val IconFrame: ImageVector by lazy {
    iconBuilder("App.IconFrame").apply {
        addPath(roundedRectPath(3f, 3.6f, 21f, 20.4f, 2.4f), stroke = ink, strokeWidth = 1.6f)
        addPath(circlePath(1.6f, 8.4f, 8.6f), fill = ink)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(4.6f, 18.4f)
                lineTo(10f, 12.6f)
                lineTo(14.2f, 16.8f)
                lineTo(17.4f, 13.6f)
                lineTo(19.4f, 15.8f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

// ─── Beauty ─────────────────────────────────────────────────────────────────

/** Scissors. */
val IconScissors: ImageVector by lazy {
    iconBuilder("App.IconScissors").apply {
        addPath(circlePath(2.6f, 6.6f, 18.2f), stroke = ink, strokeWidth = 1.7f)
        addPath(circlePath(2.6f, 6.6f, 5.4f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(8.6f, 17f, 20.2f, 4.6f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(8.6f, 7f, 20.2f, 19.4f), stroke = ink, strokeWidth = 1.7f)
        addPath(circlePath(0.9f, 7.5f, 12f), fill = ink)
    }.build()
}

/** Salon / grooming brush. */
val IconSalon: ImageVector by lazy {
    iconBuilder("App.IconSalon").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(4.4f, 20.6f)
                verticalLineTo(13.6f)
                quadTo(19.4f, 12.6f, 17.4f, 6.2f)
                quadTo(16f, 2.6f, 12.6f, 2.6f)
                quadTo(8.6f, 2.6f, 7.4f, 6.4f)
                quadTo(5.6f, 12.8f, 11.4f, 13.6f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(7.4f, 9f, 12.6f, 9f), stroke = ink, strokeWidth = 1.2f)
        addPath(polyline(9f, 5.4f, 11.4f, 5.4f), stroke = ink, strokeWidth = 1.2f)
    }.build()
}

// ─── Automotive ─────────────────────────────────────────────────────────────

/** Car. */
val IconCar: ImageVector by lazy {
    iconBuilder("App.IconCar").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(2.6f, 16.6f)
                lineTo(4.4f, 11.2f)
                quadTo(5f, 9.4f, 6.8f, 9.2f)
                lineTo(17.2f, 9.2f)
                quadTo(19f, 9.4f, 19.6f, 11.2f)
                lineTo(21.4f, 16.6f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
        addPath(polyline(6.6f, 12.8f, 17.4f, 12.8f), stroke = ink, strokeWidth = 1.4f)
        addPath(circlePath(1.9f, 7f, 16.8f), fill = ink)
        addPath(circlePath(1.9f, 17f, 16.8f), fill = ink)
    }.build()
}

/** Steering wheel. */
val IconSteeringWheel: ImageVector by lazy {
    iconBuilder("App.IconSteeringWheel").apply {
        addPath(circlePath(9.4f, 12f, 12f), stroke = ink, strokeWidth = 1.7f)
        addPath(circlePath(3.2f, 12f, 12f), stroke = ink, strokeWidth = 1.5f)
        addPath(polyline(5.4f, 8.6f, 12f, 12f, 18.6f, 8.6f), stroke = ink, strokeWidth = 1.5f)
        addPath(polyline(12f, 12f, 12f, 15.2f), stroke = ink, strokeWidth = 1.5f)
    }.build()
}

// ─── Appliance ──────────────────────────────────────────────────────────────

/** Mains plug. */
val IconPlug: ImageVector by lazy {
    iconBuilder("App.IconPlug").apply {
        addPath(roundedRectPath(6.4f, 7.4f, 17.6f, 14.4f, 2.6f), stroke = ink, strokeWidth = 1.6f)
        addPath(polyline(9.6f, 7.4f, 9.6f, 3.2f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(14.4f, 7.4f, 14.4f, 3.2f), stroke = ink, strokeWidth = 1.7f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 14.4f)
                verticalLineTo(18.2f)
                quadTo(12f, 21.6f, 16.2f, 20.6f)
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
    }.build()
}

/** Washing machine. */
val IconWashingMachine: ImageVector by lazy {
    iconBuilder("App.IconWashingMachine").apply {
        addPath(roundedRectPath(4f, 2.6f, 20f, 21.4f, 2.4f), stroke = ink, strokeWidth = 1.6f)
        addPath(polyline(4f, 8.2f, 20f, 8.2f), stroke = ink, strokeWidth = 1.4f)
        addPath(circlePath(6.2f, 5.4f, 7.2f), stroke = ink, strokeWidth = 1.2f)
        addPath(circlePath(4.2f, 12f, 15.2f), stroke = ink, strokeWidth = 1.6f)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(13.4f, 12.8f)
                quadTo(16f, 12.8f, 15.2f, 15.4f)
                quadTo(14.4f, 17.6f, 12f, 16.6f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

// ─── Property assets ────────────────────────────────────────────────────────

/** Office / commercial block. */
val IconBuilding: ImageVector by lazy {
    iconBuilder("App.IconBuilding").apply {
        addPath(roundedRectPath(4f, 3.4f, 20f, 20.6f, 2f), stroke = ink, strokeWidth = 1.6f)
        addPath(polyline(4f, 8.4f, 20f, 8.4f), stroke = ink, strokeWidth = 1.3f)
        addPath(polyline(4f, 13f, 20f, 13f), stroke = ink, strokeWidth = 1.3f)
        addPath(polyline(9.3f, 3.4f, 9.3f, 20.6f), stroke = ink, strokeWidth = 1.3f)
        addPath(polyline(14.7f, 3.4f, 14.7f, 20.6f), stroke = ink, strokeWidth = 1.3f)
    }.build()
}
