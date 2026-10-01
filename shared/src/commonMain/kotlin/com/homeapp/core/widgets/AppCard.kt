package com.homeapp.core.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import com.homeapp.core.theme.kRadiusLG
import androidx.compose.ui.unit.dp

/**
 * Standard elevated card used across the app (white surface, soft shadow,
 * 16dp radius per the design spec).
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = kRadiusLG,
    elevation: Dp = 0.dp,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val clickModifier = if (onClick != null) modifier.clip(shape).clickable(onClick = onClick) else modifier
    Card(
        modifier = clickModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
        content = content,
    )
}