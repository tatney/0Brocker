package com.homeapp.core.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.icons.IconStar
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kSpaceXS

/**
 * Gold star rating. Pass [showValue] to render the value as text (e.g. "4.8"),
 * otherwise a row of filled stars.
 */
@Composable
fun StarRating(
    rating: Double,
    modifier: Modifier = Modifier,
    showValue: Boolean = true,
    starColor: Color = kAccentGold,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    starSize: Dp = 13.dp,
) {
    if (showValue) {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = IconStar,
                contentDescription = "Rating $rating",
                tint = starColor,
                modifier = Modifier.size(starSize),
            )
            Spacer(Modifier.width(kSpaceXS))
            Text(
                text = rating.toString().take(3),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
        }
    } else {
        Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
            val filled = rating.toInt().coerceIn(0, 5)
            repeat(5) { index ->
                Icon(
                    imageVector = IconStar,
                    contentDescription = null,
                    tint = if (index < filled) starColor else starColor.copy(alpha = 0.22f),
                    modifier = Modifier.size(starSize),
                )
                Spacer(Modifier.width(kSpaceXS))
            }
        }
    }
}