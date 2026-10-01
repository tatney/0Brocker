package com.homeapp.core.widgets

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kRadiusFull
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kTealLight

/**
 * Small pill-shaped label badge (category / status / price tag).
 */
@Composable
fun TagBadge(
    text: String,
    modifier: Modifier = Modifier,
    foreground: Color = kAccentTeal,
    background: Color = kTealLight,
    shape: Shape = kRadiusFull,
    fontSize: TextUnit = 11.sp,
    weight: FontWeight = FontWeight.Medium,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = background,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = fontSize),
            fontWeight = weight,
            color = foreground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = kSpaceSM, vertical = kSpaceXS),
        )
    }
}
