package com.homeapp.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.unit.dp

// Fully self-contained brand icon set (no material-icons dependency).
// All vectors are tinted at use-site via Icon(color=...), so fills and
// stroke brushes are plain black and only contribute alpha coverage.
//
// The DSL helpers below are `internal` so the glyph families can be split
// across ServiceIcons.kt / UiIcons.kt while sharing one construction style:
// 24dp box, 24-unit viewport, 1.8dp round-cap strokes, 24x24 alignment.

internal fun iconBuilder(name: String): ImageVector.Builder = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
)

internal val ink: SolidColor
    get() = SolidColor(Color.Black)

internal fun circlePath(radius: Float, cx: Float, cy: Float): List<PathNode> {
    val k = 0.5522847498f * radius
    return PathBuilder().apply {
        moveTo(cx + radius, cy)
        curveTo(cx + radius, cy + k, cx + k, cy + radius, cx, cy + radius)
        curveTo(cx - k, cy + radius, cx - radius, cy + k, cx - radius, cy)
        curveTo(cx - radius, cy - k, cx - k, cy - radius, cx, cy - radius)
        curveTo(cx + k, cy - radius, cx + radius, cy - k, cx + radius, cy)
        close()
    }.nodes
}

internal fun roundedRectPath(
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    corner: Float,
): List<PathNode> = PathBuilder().apply {
    moveTo(left + corner, top)
    horizontalLineTo(right - corner)
    quadTo(right, top, right, top + corner)
    verticalLineTo(bottom - corner)
    quadTo(right, bottom, right - corner, bottom)
    horizontalLineTo(left + corner)
    quadTo(left, bottom, left, bottom - corner)
    verticalLineTo(top + corner)
    quadTo(left, top, left + corner, top)
    close()
}.nodes

/** Convenience: a straight open polyline from [points] (first point = moveTo). */
internal fun polyline(vararg points: Float, close: Boolean = false): List<PathNode> =
    PathBuilder().apply {
        moveTo(points[0], points[1])
        var i = 2
        while (i + 1 < points.size) {
            lineTo(points[i], points[i + 1])
            i += 2
        }
        if (close) close()
    }.nodes

internal fun ImageVector.Builder.addPath(
    pathData: List<PathNode>,
    fill: SolidColor? = null,
    stroke: SolidColor? = null,
    strokeWidth: Float = 1.8f,
    cap: StrokeCap = StrokeCap.Round,
    join: StrokeJoin = StrokeJoin.Round,
) {
    addPath(
        pathData = pathData,
        pathFillType = PathFillType.NonZero,
        fill = fill,
        stroke = stroke,
        strokeLineWidth = strokeWidth,
        strokeLineCap = cap,
        strokeLineJoin = join,
    )
}

/** Magnifying-glass search glyph. */
val IconSearch: ImageVector by lazy {
    iconBuilder("App.IconSearch").apply {
        addPath(circlePath(7f, 11f, 11f), stroke = ink)
        addPath(
            pathData = PathBuilder().apply { moveTo(16f, 16f); lineTo(21f, 21f) }.nodes,
            stroke = ink,
        )
    }.build()
}

/** Filled five-point star. */
val IconStar: ImageVector by lazy {
    val outer = listOf(12f to 2.5f, 21.51f to 9.41f, 17.88f to 20.59f, 6.12f to 20.59f, 2.49f to 9.41f)
    val inner = listOf(14.35f to 9.26f, 15.8f to 13.74f, 12f to 16.5f, 8.2f to 13.74f, 9.65f to 9.26f)
    iconBuilder("App.IconStar").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(outer[0].first, outer[0].second)
                outer.indices.forEach { i ->
                    lineTo(inner[i].first, inner[i].second)
                    val next = (i + 1) % outer.size
                    lineTo(outer[next].first, outer[next].second)
                }
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Outlined house. */
val IconHome: ImageVector by lazy {
    iconBuilder("App.IconHome").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3f, 11f); lineTo(12f, 3f); lineTo(21f, 11f)
                moveTo(5f, 11f); verticalLineTo(19f); quadTo(5f, 20f, 6f, 20f)
                horizontalLineTo(18f); quadTo(19f, 20f, 19f, 19f); verticalLineTo(11f)
                moveTo(10f, 20f); verticalLineTo(15f); quadTo(10f, 14f, 12f, 14f); quadTo(14f, 14f, 14f, 15f); verticalLineTo(20f)
            }.nodes,
            stroke = ink,
        )
    }.build()
}

/** Hexagon "tools" outline for home-services branding. */
val IconTools: ImageVector by lazy {
    iconBuilder("App.IconTools").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 3f)
                lineTo(20.66f, 7.5f)
                verticalLineTo(16.5f)
                lineTo(12f, 21f)
                lineTo(3.34f, 16.5f)
                verticalLineTo(7.5f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.6f,
        )
    }.build()
}

/** Person silhouette (head + shoulders). */
val IconPerson: ImageVector by lazy {
    iconBuilder("App.IconPerson").apply {
        addPath(circlePath(4f, 12f, 7.5f), fill = ink)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(5f, 21f)
                quadTo(5f, 14f, 12f, 14f)
                quadTo(19f, 14f, 19f, 21f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Chat speech bubble. */
val IconChat: ImageVector by lazy {
    iconBuilder("App.IconChat").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(6f, 4f)
                horizontalLineTo(18f)
                quadTo(21f, 4f, 21f, 7f)
                verticalLineTo(14f)
                quadTo(21f, 17f, 18f, 17f)
                horizontalLineTo(10f)
                lineTo(6.6f, 20.4f)
                lineTo(7f, 17f)
                horizontalLineTo(6f)
                quadTo(3f, 17f, 3f, 14f)
                verticalLineTo(7f)
                quadTo(3f, 4f, 6f, 4f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Calendar outline with header strip. */
val IconCalendar: ImageVector by lazy {
    iconBuilder("App.IconCalendar").apply {
        addPath(roundedRectPath(3f, 6f, 21f, 20f, 3f), stroke = ink)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(8f, 3f); verticalLineTo(7f)
                moveTo(16f, 3f); verticalLineTo(7f)
                moveTo(3f, 11f); horizontalLineTo(21f)
            }.nodes,
            stroke = ink,
        )
    }.build()
}

/** Left-right arrows for transactions. */
val IconTransactions: ImageVector by lazy {
    iconBuilder("App.IconTransactions").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(4f, 8f); horizontalLineTo(17f)
                moveTo(12f, 3f); lineTo(17f, 8f); lineTo(12f, 13f)
                moveTo(20f, 16f); horizontalLineTo(7f)
                moveTo(12f, 11f); lineTo(7f, 16f); lineTo(12f, 21f)
            }.nodes,
            stroke = ink,
        )
    }.build()
}

/** Plus sign. */
val IconAdd: ImageVector by lazy {
    iconBuilder("App.IconAdd").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 4f); verticalLineTo(20f)
                moveTo(4f, 12f); horizontalLineTo(20f)
            }.nodes,
            stroke = ink,
        )
    }.build()
}

/** Location pin (filled teardrop). */
val IconLocationPin: ImageVector by lazy {
    iconBuilder("App.IconLocationPin").apply {
        addPath(circlePath(6f, 12f, 11f), fill = ink)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(6.5f, 13f); lineTo(17.5f, 13f); lineTo(12f, 23f); close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Right-pointing arrow. */
val IconArrowForward: ImageVector by lazy {
    iconBuilder("App.IconArrowForward").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(3f, 12f); horizontalLineTo(19f)
                moveTo(12f, 5f); lineTo(19f, 12f); lineTo(12f, 19f)
            }.nodes,
            stroke = ink,
        )
    }.build()
}

/** Notification bell. */
val IconBell: ImageVector by lazy {
    iconBuilder("App.IconBell").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(12f, 3f)
                quadTo(8f, 3f, 8f, 6.5f)
                quadTo(8f, 10f, 5f, 13f)
                horizontalLineTo(19f)
                quadTo(16f, 10f, 16f, 6.5f)
                quadTo(16f, 3f, 12f, 3f)
                close()
                moveTo(9.5f, 17f)
                quadTo(12f, 20f, 14.5f, 17f)
            }.nodes,
            stroke = ink,
        )
        addPath(circlePath(1.2f, 12f, 16.4f), fill = ink)
    }.build()
}

/** Simple credit-card glyph used for the Payments section/tab. */
val IconCreditCard: ImageVector by lazy {
    iconBuilder("App.IconCreditCard").apply {
        addPath(roundedRectPath(2f, 5f, 22f, 19f, 3f), stroke = ink)
        addPath(
            pathData = PathBuilder().apply { moveTo(2f, 9.2f); horizontalLineTo(22f) }.nodes,
            stroke = ink,
        )
        addPath(
            pathData = PathBuilder().apply {
                moveTo(7.5f, 12f)
                horizontalLineTo(10.5f)
                quadTo(11f, 12f, 11f, 12.5f)
                verticalLineTo(15f)
                quadTo(11f, 15.5f, 10.5f, 15.5f)
                horizontalLineTo(7.5f)
                quadTo(7f, 15.5f, 7f, 15f)
                verticalLineTo(12.5f)
                quadTo(7f, 12f, 7.5f, 12f)
                close()
            }.nodes,
            stroke = ink,
            strokeWidth = 1.2f,
        )
    }.build()
}

/** Wallet glyph for balance/wallet surfaces. */
val IconWallet: ImageVector by lazy {
    iconBuilder("App.IconWallet").apply {
        addPath(roundedRectPath(2f, 5f, 22f, 19f, 3f), stroke = ink)
        addPath(
            pathData = PathBuilder().apply {
                moveTo(13.5f, 5f)
                quadTo(12.5f, 12f, 13.5f, 19f)
            }.nodes,
            stroke = ink,
        )
        addPath(
            pathData = PathBuilder().apply {
                moveTo(17f, 9f)
                quadTo(18.6f, 9f, 18.6f, 10.4f)
                quadTo(18.6f, 11.8f, 17f, 11.8f)
                quadTo(15.4f, 11.8f, 15.4f, 10.4f)
                quadTo(15.4f, 9f, 17f, 9f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Crown glyph for premium plans / VIP badges. */
val IconCrown: ImageVector by lazy {
    iconBuilder("App.IconCrown").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(4f, 17f)
                lineTo(3f, 7.5f)
                lineTo(8f, 11.5f)
                lineTo(12f, 5f)
                lineTo(16f, 11.5f)
                lineTo(21f, 7.5f)
                lineTo(20f, 17f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Left chevron for back navigation. */
val IconBack: ImageVector by lazy {
    iconBuilder("App.IconBack").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(15f, 6f)
                lineTo(9f, 12f)
                lineTo(15f, 18f)
            }.nodes,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }.build()
}

/** Telephone handset glyph for call actions. */
val IconPhone: ImageVector by lazy {
    iconBuilder("App.IconPhone").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(6.5f, 4f)
                curveTo(6.7f, 8.2f, 8.4f, 12.1f, 11.1f, 14.7f)
                curveTo(13.9f, 17.4f, 17.7f, 19.2f, 21f, 19.5f)
                lineTo(21f, 16.5f)
                lineTo(17.5f, 15.3f)
                lineTo(15.8f, 17f)
                curveTo(13.4f, 15.6f, 11.2f, 13.5f, 9.6f, 11.1f)
                lineTo(11.2f, 9.4f)
                lineTo(10.1f, 5.8f)
                lineTo(7f, 4f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}

/** Bookmark / save glyph. */
val IconBookmark: ImageVector by lazy {
    iconBuilder("App.IconBookmark").apply {
        addPath(
            pathData = PathBuilder().apply {
                moveTo(6f, 4f)
                lineTo(18f, 4f)
                lineTo(18f, 20f)
                lineTo(12f, 16.5f)
                lineTo(6f, 20f)
                close()
            }.nodes,
            fill = ink,
        )
    }.build()
}
