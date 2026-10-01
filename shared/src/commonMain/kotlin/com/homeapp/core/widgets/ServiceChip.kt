package com.homeapp.core.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kTealLight
import com.homeapp.core.theme.kTextSecondary

/**
 * Circular icon chip with a label underneath, used for quick actions and service
 * categories on the home screens.
 *
 * Icon-only by design: this chip previously also accepted an `emoji`, which is
 * how the payments hub ended up with four differently-shaped glyphs for money
 * actions. The `emoji` parameter no longer exists so that cannot regress.
 */
@Composable
fun ServiceChip(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    containerColor: Color = kTealLight,
    iconColor: Color = kPrimaryRed,
    selected: Boolean = false,
    size: Dp = 56.dp,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Column(
        modifier = clickModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(if (selected) iconColor else containerColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else iconColor,
                modifier = Modifier.size(size * 0.44f),
            )
        }
        Spacer(Modifier.height(kSpaceXS))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSurface else kTextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
