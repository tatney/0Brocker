package com.homeapp.core.icons

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder

val IconMobilePayment: ImageVector by lazy {
    iconBuilder("App.MobilePayment").apply {
        addPath(polyline(6f, 2f, 18f, 2f, 18f, 22f, 6f, 22f, close = true), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(10f, 5f, 14f, 5f), stroke = ink, strokeWidth = 1.5f)
        addPath(circlePath(1f, 12f, 19f), fill = ink)
    }.build()
}

val IconBroadband: ImageVector by lazy {
    iconBuilder("App.Broadband").apply {
        addPath(PathBuilder().apply { moveTo(3f, 8f); curveTo(8f, 2f, 16f, 2f, 21f, 8f); moveTo(6f, 11f); curveTo(9f, 7f, 15f, 7f, 18f, 11f); moveTo(10f, 13f); curveTo(11f, 12f, 13f, 12f, 14f, 13f) }.nodes, stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(3f, 16f, 21f, 16f, 21f, 21f, 3f, 21f, close = true), stroke = ink, strokeWidth = 1.7f)
        addPath(circlePath(.8f, 7f, 18.5f), fill = ink)
        addPath(circlePath(.8f, 11f, 18.5f), fill = ink)
    }.build()
}

val IconTelevision: ImageVector by lazy {
    iconBuilder("App.Television").apply {
        addPath(polyline(2f, 4f, 22f, 4f, 22f, 18f, 2f, 18f, close = true), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(8f, 22f, 16f, 22f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(12f, 18f, 12f, 22f), stroke = ink, strokeWidth = 1.7f)
    }.build()
}

val IconEducation: ImageVector by lazy {
    iconBuilder("App.Education").apply {
        addPath(polyline(1f, 8f, 12f, 3f, 23f, 8f, 12f, 13f, close = true), fill = ink)
        addPath(polyline(5f, 12f, 5f, 18f, 12f, 21f, 19f, 18f, 19f, 12f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(23f, 8f, 23f, 16f), stroke = ink, strokeWidth = 1.5f)
    }.build()
}
