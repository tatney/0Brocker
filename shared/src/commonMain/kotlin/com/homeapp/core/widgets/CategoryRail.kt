package com.homeapp.core.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homeapp.core.model.colors
import com.homeapp.core.model.icon
import com.homeapp.core.theme.AppRadii
import com.homeapp.core.theme.AppSize
import com.homeapp.core.theme.AppSpacing
import com.homeapp.data.model.ServiceCategory

/**
 * A single service-category affordance, coloured by the category's own accent.
 *
 * The chip previously took ad-hoc colours and an emoji, which is why categories
 * looked inconsistent between the home hub, the services hub and the map filter.
 * Icon and colour now both come from [com.homeapp.core.model.CategoryVisuals],
 * so a category looks the same everywhere it appears.
 */
@Composable
fun CategoryChip(
    category: ServiceCategory,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    size: Dp = AppSize.categoryCircle,
    onClick: (() -> Unit)? = null,
) {
    val colors = category.colors
    ServiceChip(
        label = category.label,
        modifier = modifier,
        icon = category.icon,
        containerColor = colors.container,
        iconColor = colors.accent,
        selected = selected,
        size = size,
        onClick = onClick,
    )
}

/**
 * Horizontally scrolling row of circular category chips.
 *
 * Built on [LazyRow] rather than a `Column` inside a `Row`, which is what made
 * the original hub wrap vertically and clip most of the categories — Painting
 * was pushed off-screen and never reached.
 *
 * @param categories categories to offer; defaults to every category in the enum.
 * @param selected currently active category.
 * @param onSelect invoked with the tapped category.
 */
@Composable
fun CategoryIconRail(
    selected: ServiceCategory,
    onSelect: (ServiceCategory) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<ServiceCategory> = ServiceCategory.entries,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.gutter),
) {
    val listState = rememberLazyListState()
    val selectedIndex = categories.indexOf(selected)

    // Keep the active category on screen when selection changes from elsewhere
    // (e.g. a map filter being reset).
    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) listState.animateScrollToItem(selectedIndex)
    }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.chip),
    ) {
        items(items = categories, key = { it.name }) { category ->
            CategoryChip(
                category = category,
                selected = category == selected,
                onClick = { onSelect(category) },
            )
        }
    }
}

/**
 * Pill-shaped category filter with a leading glyph.
 *
 * Used where a full label has to stay readable — the map's filter bar and the
 * browse-mode header — rather than the circular rail's short labels.
 */
@Composable
fun CategoryPill(
    category: ServiceCategory,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = category.colors
    val background = if (selected) colors.accent else colors.container
    val foreground = if (selected) Color.White else colors.onContainer

    Surface(
        modifier = modifier,
        shape = AppRadii.chip,
        color = background,
        border = if (selected) null else BorderStroke(1.dp, colors.accent.copy(alpha = 0.24f)),
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = AppSpacing.item + AppSpacing.tight, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = foreground,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(AppSpacing.tight + 2.dp))
            Text(
                text = category.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = foreground,
                maxLines = 1,
            )
        }
    }
}

/** Scrolling row of [CategoryPill]s, for the map filter and browse header. */
@Composable
fun CategoryPillRow(
    selected: ServiceCategory?,
    onSelect: (ServiceCategory) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<ServiceCategory> = ServiceCategory.entries,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.gutter),
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.chip),
    ) {
        items(items = categories, key = { it.name }) { category ->
            CategoryPill(
                category = category,
                selected = category == selected,
                onClick = { onSelect(category) },
            )
        }
    }
}

/**
 * Small coloured dot + label used for live availability.
 *
 * Reads its palette from `ProviderStatus.colors` so the map canvas, the preview
 * sheet and the provider list all show the same colour for "Busy".
 */
@Composable
fun AvailabilityDot(
    accent: Color,
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
) {
    Spacer(
        modifier = modifier
            .size(size)
            .background(accent, CircleShape),
    )
}

/**
 * A stat block: large value, small label, optional glyph.
 *
 * Replaces the four near-identical hand-rolled stat rows on the property and
 * payments hubs.
 */
@Composable
fun MetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null,
) {
    val clickModifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Surface(
        modifier = clickModifier,
        shape = AppRadii.tile,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.cardInner),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Surface(
                    shape = AppRadii.control,
                    color = accent.copy(alpha = 0.12f),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier
                            .padding(AppSpacing.item)
                            .size(20.dp),
                    )
                }
                Spacer(Modifier.width(AppSpacing.chip + AppSpacing.tight))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
