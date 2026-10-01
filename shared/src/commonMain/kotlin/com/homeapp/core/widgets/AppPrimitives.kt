package com.homeapp.core.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.icons.IconBack
import com.homeapp.core.icons.IconChevronRight
import com.homeapp.core.model.colors
import com.homeapp.core.theme.AppRadii
import com.homeapp.core.theme.AppSize
import com.homeapp.core.theme.AppSpacing
import com.homeapp.data.model.ProviderStatus
import com.homeapp.data.model.ServiceCategory

/**
 * The app's one top bar.
 *
 * Every module previously hand-rolled its own header, which is why the title
 * position, back affordance and height drifted between hubs. Screens pass a
 * title and optional trailing actions and get an identical bar.
 *
 * @param onBack when non-null, renders the back affordance and wires it up.
 * @param actions trailing content, laid out right-aligned.
 */
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSize.topBar)
                .padding(horizontal = AppSpacing.gutter),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(AppSize.touchTarget)
                        .clip(CircleShape)
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = IconBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(AppSize.touchTarget - 12.dp),
                    )
                }
                Spacer(Modifier.width(AppSpacing.tight))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (actions != null) {
                Spacer(Modifier.width(AppSpacing.item))
                actions()
            }
        }
    }
}

/**
 * Large page heading used at the top of each hub: title, supporting line and an
 * optional trailing action.
 */
@Composable
fun HubHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.gutter),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Spacer(Modifier.height(AppSpacing.tight))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (action != null) {
            Spacer(Modifier.width(AppSpacing.item))
            action()
        }
    }
}

/**
 * Standard navigable row: leading glyph, title, optional subtitle, optional
 * trailing content and an optional chevron.
 *
 * The workhorse for settings, transaction lists, booking rows and menu entries,
 * all of which previously re-implemented the same spacing by hand.
 */
@Composable
fun AppListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconBackground: Color = iconTint.copy(alpha = 0.12f),
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Row(
        modifier = clickModifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.gutter, vertical = AppSpacing.item + AppSpacing.tight),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(AppSpacing.item + AppSpacing.tight))
        } else if (icon != null) {
            Surface(shape = AppRadii.control, color = iconBackground) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .padding(AppSpacing.item)
                        .size(20.dp),
                )
            }
            Spacer(Modifier.width(AppSpacing.item + AppSpacing.tight))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(AppSpacing.item))
            trailing()
        }
        if (showChevron) {
            Spacer(Modifier.width(AppSpacing.tight))
            Icon(
                imageVector = IconChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * Circular avatar for a provider or user.
 *
 * Resolution order is [icon], then [emoji], then the person's initial. Personal
 * avatars (the account owner, a chat contact) keep their stored [emoji]; a
 * provider gets their trade [icon] instead, because a wrench emoji says nothing
 * about a specific person the way an avatar is supposed to.
 */
@Composable
fun AppAvatar(
    name: String,
    modifier: Modifier = Modifier,
    category: ServiceCategory? = null,
    emoji: String? = null,
    icon: ImageVector? = null,
    size: Dp = AppSize.avatarMd,
    ringColor: Color? = null,
) {
    val accent = category?.colors?.accent ?: MaterialTheme.colorScheme.primary
    val initial = remember(name) { name.trim().take(1).uppercase() }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.14f))
            .then(
                if (ringColor != null) Modifier.border(2.dp, ringColor, CircleShape) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(size * 0.5f),
            )

            emoji != null -> Text(text = emoji, fontSize = (size.value * 0.46f).sp)
            else -> Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent,
                fontSize = (size.value * 0.38f).sp,
            )
        }
    }
}

/**
 * Tinted rounded tile wrapping a single icon.
 *
 * This is the replacement for decorative glyph-sized `Text` calls (`fontSize =
 * 64.sp` and friends) that used to carry meaning, and for the circular
 * `Box(background) { Text(emoji) }` tiles scattered across hubs.
 *
 * @param size tile edge length; the icon is drawn at roughly half of it.
 * @param shape tile shape, defaulting to a soft rounded square.
 */
@Composable
fun AppIconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = tint.copy(alpha = 0.12f),
    size: Dp = AppSize.avatarMd,
    shape: Shape = AppRadii.control,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}

/**
 * Large centred icon for empty and success states.
 *
 * Replaces the oversized single-glyph `Text` that these states used to render,
 * which inherited a text font and scaled inconsistently per platform.
 */
@Composable
fun AppStateIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = tint.copy(alpha = 0.12f),
    size: Dp = 88.dp,
) {
    AppIconTile(
        icon = icon,
        modifier = modifier,
        tint = tint,
        container = container,
        size = size,
        shape = CircleShape,
    )
}

/**
 * Availability pill. Colour and label both come from the status palette so the
 * map marker, the preview sheet and the list row never disagree.
 */
@Composable
fun StatusPill(
    status: ProviderStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val colors = status.colors
    Surface(
        modifier = modifier,
        shape = AppRadii.chip,
        color = colors.container,
        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.28f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.item, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.tight + 2.dp),
        ) {
            AvailabilityDot(accent = colors.accent, size = 7.dp)
            if (showLabel) {
                Text(
                    text = colors.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onContainer,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Page container: applies the standard screen background, places a top bar and
 * hosts one scrolling content column.
 *
 * Adopted by the five hubs in the navigation slice so no screen has to remember
 * the background colour and gutter rules.
 */
@Composable
fun AppPage(
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background),
    ) {
        if (topBar != null) {
            topBar()
            Spacer(Modifier.height(AppSpacing.belowBar))
        }
        content()
    }
}
