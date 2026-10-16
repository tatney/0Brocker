package com.homeapp.core.icons

import androidx.compose.ui.graphics.vector.ImageVector

val IconShoppingCart: ImageVector by lazy {
    iconBuilder("App.ShoppingCart").apply {
        addPath(polyline(2f, 3f, 5f, 3f, 8f, 17f, 19f, 17f), stroke = ink, strokeWidth = 1.7f)
        addPath(polyline(6f, 6f, 22f, 6f, 19.8f, 13f, 7.5f, 13f), stroke = ink, strokeWidth = 1.7f)
        addPath(circlePath(1.5f, 9f, 21f), fill = ink)
        addPath(circlePath(1.5f, 18f, 21f), fill = ink)
    }.build()
}
