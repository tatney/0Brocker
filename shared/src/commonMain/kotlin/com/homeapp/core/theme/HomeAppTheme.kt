package com.homeapp.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = kPrimaryRed,
    onPrimary = kSurface,
    primaryContainer = kPrimaryRedLight,
    onPrimaryContainer = kPrimaryRed,
    secondary = kAccentTeal,
    onSecondary = kSurface,
    secondaryContainer = kTealLight,
    onSecondaryContainer = kAccentTeal,
    tertiary = kAccentPurple,
    onTertiary = kSurface,
    tertiaryContainer = kPurpleLight,
    onTertiaryContainer = kAccentPurple,
    error = kStatusDanger,
    onError = kSurface,
    errorContainer = kStatusDangerLight,
    onErrorContainer = kStatusDanger,
    background = kBackground,
    onBackground = kTextPrimary,
    surface = kSurface,
    onSurface = kTextPrimary,
    surfaceVariant = kDivider,
    onSurfaceVariant = kTextSecondary,
    outline = kDivider,
    outlineVariant = kDivider,
)

private val DarkColors = darkColorScheme(
    primary = kPrimaryRed,
    onPrimary = kSurface,
    primaryContainer = kPrimaryRedLight.copy(alpha = 0.2f),
    onPrimaryContainer = kPrimaryRedLight,
    secondary = kAccentTeal,
    onSecondary = kSurface,
    secondaryContainer = kTealLight.copy(alpha = 0.2f),
    onSecondaryContainer = kTealLight,
    tertiary = kAccentPurple,
    tertiaryContainer = kPurpleLight.copy(alpha = 0.2f),
    onTertiaryContainer = kPurpleLight,
    background = kDarkBackground,
    onBackground = kSurface,
    surface = kDarkSurface,
    onSurface = kSurface,
    surfaceVariant = kDarkSurfaceVariant,
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFB9BEC8),
    outline = kDarkSurfaceVariant,
    outlineVariant = kDarkSurfaceVariant,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun HomeAppTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = appTypography(),
        shapes = AppShapes,
        content = content,
    )
}