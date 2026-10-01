package com.homeapp.core.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.theme.kDivider
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusFull
import com.homeapp.core.theme.kTextSecondary

@Composable
private fun ButtonLabel(
    text: String,
    textColor: Color,
    icon: ImageVector?,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Primary call-to-action button: red, full-width, pill-shaped, 52dp tall. */
@Composable
fun CTAButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = kPrimaryRed,
    textColor: Color = Color.White,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    shape: Shape = com.homeapp.core.theme.kRadiusLG,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(height),
        shape = shape,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = kDivider,
            disabledContentColor = kTextSecondary,
        ),
    ) {
        ButtonLabel(text, if (enabled) textColor else MaterialTheme.colorScheme.onSurfaceVariant, icon)
    }
}

/** Secondary outlined button used alongside CTAs. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = com.homeapp.core.theme.kRadiusLG,
    borderColor: Color = kPrimaryRed,
    textColor: Color = kPrimaryRed,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(height),
        shape = shape,
        enabled = enabled,
        border = BorderStroke(width = 1.dp, color = borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor,
            disabledContentColor = kTextSecondary,
        ),
    ) {
        ButtonLabel(text, if (enabled) textColor else MaterialTheme.colorScheme.onSurfaceVariant, icon)
    }
}