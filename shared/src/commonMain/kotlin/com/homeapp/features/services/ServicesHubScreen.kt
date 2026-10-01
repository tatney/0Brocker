package com.homeapp.features.services

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconArrowForward
import com.homeapp.core.icons.IconCalendar
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.model.serviceCategory
import com.homeapp.core.icons.IconChevronRight
import com.homeapp.core.icons.IconMap
import com.homeapp.core.icons.IconStar
import com.homeapp.core.theme.AppSpacing
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTealLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.widgets.AppTopBar
import com.homeapp.core.widgets.CategoryIconRail
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.SectionHeader
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.TagBadge
import com.homeapp.data.model.ServiceCategory
import com.homeapp.data.model.ServiceProvider

/**
 * Home-services hub.
 *
 * The category affordance previously used a private `ServiceCategory` data class
 * that shadowed the real enum and carried only eight legacy-vocabulary entries,
 * so "Painting" was never offered at all. Categories now come straight from
 * [ServiceCategory] and render through [CategoryIconRail], which is a LazyRow —
 * the old version nested fixed Rows inside a lazy item and could not scroll to
 * the categories that fell off the end.
 */
@Composable
fun ServicesHubScreen(
    modifier: Modifier = Modifier,
    onCategoryClick: (ServiceCategory) -> Unit = {},
    onServiceClick: (Long) -> Unit = {},
    onBookNow: (Long) -> Unit = {},
    onMyBookings: () -> Unit = {},
    onOpenMap: () -> Unit = {},
    viewModel: MarketplaceHubViewModel = viewModel { MarketplaceHubViewModel() },
) {
    val professionals by viewModel.professionals.collectAsState()
    val topProfessional = professionals.firstOrNull()

    var selected by rememberSaveable { mutableStateOf(ServiceCategory.ALL.name) }
    val selectedCategory = ServiceCategory.entries.firstOrNull { it.name == selected }
        ?: ServiceCategory.ALL

    // Tapping a category opens the marketplace explore route, which arrives
    // already filtered — the selection is no longer discarded.
    val openCategory: (ServiceCategory) -> Unit = { category ->
        selected = category.name
        onCategoryClick(category)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.screenEnd),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.section),
    ) {
        item {
            AppTopBar(
                title = "Home Services",
                subtitle = "Direct providers - sample profiles",
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.gutter)
                    .height(120.dp)
                    .background(Brush.linearGradient(listOf(kTealLight, kSurface)), kRadiusXL)
                    .padding(AppSpacing.hero),
            ) {
                Column(modifier = Modifier.align(Alignment.CenterStart)) {
                    Text(
                        text = "A little care for your home",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.height(kSpaceXS))
                    TagBadge(text = "Choose the provider yourself", foreground = kAccentTeal)
                }
            }
        }

        item {
            SectionHeader(
                title = "Browse categories",
                subtitle = "Every service, one tap away",
                actionLabel = "Map",
                onAction = onOpenMap,
                actionIcon = IconMap,
                modifier = Modifier.padding(horizontal = AppSpacing.gutter),
            )
        }

        // All 15 categories, scrollable, each with its own glyph and accent.
        item {
            CategoryIconRail(
                selected = selectedCategory,
                onSelect = openCategory,
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.gutter)
                    .background(MaterialTheme.colorScheme.primary, kRadiusXL)
                    .clickable {
                        // Pre-filter the map by whatever is currently selected.
                        if (selectedCategory == ServiceCategory.ALL) onOpenMap() else openCategory(selectedCategory)
                    }
                    .padding(AppSpacing.cardInner),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (selectedCategory == ServiceCategory.ALL) {
                                "Find Providers on Map"
                            } else {
                                "Find ${selectedCategory.label} Providers"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = kSurface,
                        )
                        Text(
                            text = "Compare nearby service providers",
                            style = MaterialTheme.typography.labelSmall,
                            color = kSurface.copy(alpha = 0.8f),
                        )
                    }
                    Icon(
                        imageVector = IconChevronRight,
                        contentDescription = null,
                        tint = kSurface,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Top rated professionals",
                // This used to be a "More" action wired to an empty lambda. It is
                // now the entry point to My Bookings, which the old tab layout
                // reached through a dedicated tab.
                actionLabel = "My Bookings",
                actionIcon = IconCalendar,
                onAction = onMyBookings,
                modifier = Modifier.padding(horizontal = AppSpacing.gutter),
            )
        }

        items(professionals, key = { it.id }) { professional ->
            ProfessionalRow(
                professional = professional,
                onClick = { onServiceClick(professional.id) },
            )
        }

        if (topProfessional != null) {
            item {
                CTAButton(
                    text = "Book a service",
                    onClick = { onBookNow(topProfessional.id) },
                    modifier = Modifier.padding(horizontal = AppSpacing.gutter),
                )
            }
        }
    }
}

/**
 * A legacy professional row.
 *
 * Previously reused [com.homeapp.core.widgets.ServiceChip] with
 * `selected = true` as the avatar, which painted a solid brand-red disc behind
 * every name. Avatars now go through [AppAvatar] and are tinted by the
 * professional's real category, resolved through the alias table.
 */
@Composable
private fun ProfessionalRow(
    professional: ServiceProvider,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.gutter, vertical = AppSpacing.item),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(
            name = professional.name,
            category = professional.serviceCategory,
            icon = professional.categoryIcon,
        )
        Spacer(Modifier.width(AppSpacing.cardInner))
        Column(Modifier.weight(1f)) {
            Text(
                text = professional.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = professional.headline,
                style = MaterialTheme.typography.bodySmall,
                color = kTextSecondary,
                maxLines = 1,
            )
            Spacer(Modifier.height(kSpaceXS))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconStar,
                    contentDescription = null,
                    tint = com.homeapp.core.theme.kAccentGold,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(kSpaceXS))
                StarRating(rating = professional.rating, showValue = true)
            }
        }
        Icon(
            imageVector = IconArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}
