package com.homeapp.features.services.map

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconArrowForward
import com.homeapp.core.icons.IconBack
import com.homeapp.core.icons.IconList
import com.homeapp.core.icons.IconMap
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.icons.IconSearch
import com.homeapp.core.icons.IconShieldCheck
import com.homeapp.core.model.colors
import com.homeapp.core.model.serviceCategoryOf
import com.homeapp.core.theme.AppRadii
import com.homeapp.core.theme.AppSpacing
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXL
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.widgets.AppSearchBar
import com.homeapp.core.widgets.CategoryPillRow
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.EmptyState
import com.homeapp.core.widgets.MetricTile
import com.homeapp.core.widgets.OutlineButton
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.StatusPill
import com.homeapp.core.widgets.TagBadge
import com.homeapp.data.model.ServiceCategory
import com.homeapp.data.model.ServiceProvider
import kotlinx.coroutines.launch

/**
 * Provider explore: one screen with a Map mode and a Browse mode over the same
 * filtered provider list.
 *
 * Previously `isMapView` only drove the toggle's highlight — the map rendered
 * unconditionally and Browse showed nothing. The five private widget copies in
 * this file (search bar, category chip, toggle, stat, status badge) have been
 * replaced by the shared design-system components, which is what removed the
 * last `category.emoji` usage on this route.
 *
 * @param initialCategory prefilters the list, so a category chip on a hub can
 *   deep-link straight into a filtered explore view.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceMapScreen(
    onProviderClick: (Long) -> Unit,
    onRequestService: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialCategory: ServiceCategory? = null,
    viewModel: MapViewModel = viewModel { MapViewModel() },
) {
    val uiState by viewModel.uiState.collectAsState()
    val providers by viewModel.providers.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    // Apply the deep-linked prefilter once, when the route delivers it.
    LaunchedEffect(initialCategory) {
        if (initialCategory != null) viewModel.onCategorySelect(initialCategory)
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isMapView) {
            ServiceMapCanvas(
                providers = providers,
                selectedProvider = uiState.selectedProvider,
                onProviderMarkerClick = { viewModel.onProviderSelect(it) },
                modifier = Modifier.fillMaxSize(),
                // Was never passed before, so the location dot could not render.
                userLocation = viewModel.userLocation,
            )
        } else {
            BrowseProviderList(
                providers = providers,
                selectedProvider = uiState.selectedProvider,
                // Select only. Navigating here as well meant the browse row
                // skipped the preview sheet and went straight to the profile,
                // so the two views behaved differently for the same tap.
                onProviderClick = { viewModel.onProviderSelect(it) },
            )
        }

        ExploreHeader(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            selectedCategory = uiState.selectedCategory,
            onCategorySelect = viewModel::onCategorySelect,
            isMapView = uiState.isMapView,
            onToggleView = viewModel::toggleView,
            onBack = onBack,
        )

        // Result count, worded for the active filter.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = kSpaceLG),
            shape = RoundedCornerShape(50),
            color = kSurface,
            shadowElevation = 4.dp,
        ) {
            Text(
                text = if (uiState.selectedCategory == ServiceCategory.ALL) {
                    "${providers.size} providers nearby"
                } else {
                    "${providers.size} ${uiState.selectedCategory.label} providers"
                },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM),
            )
        }
    }

    // Marker / row tap selects the provider and shows the preview sheet; the
    // sheet's primary action is what navigates on to the profile.
    uiState.selectedProvider?.let { provider ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.onProviderSelect(null) },
            sheetState = sheetState,
            containerColor = kSurface,
        ) {
            ProviderPreviewSheet(
                provider = provider,
                onViewProfile = {
                    viewModel.onProviderSelect(null)
                    scope.launch { onProviderClick(provider.id) }
                },
                onRequestService = {
                    viewModel.onProviderSelect(null)
                    scope.launch { onRequestService(provider.id) }
                },
            )
        }
    }
}

/** Back button, Map/Browse toggle, search field and category filter. */
@Composable
private fun ExploreHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: ServiceCategory,
    onCategorySelect: (ServiceCategory) -> Unit,
    isMapView: Boolean,
    onToggleView: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.gutter, vertical = AppSpacing.item),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(kSurface, CircleShape)
                    .size(40.dp),
            ) {
                Icon(IconBack, contentDescription = "Back", tint = Color.Black, modifier = Modifier.size(20.dp))
            }
            ViewToggle(isMapView = isMapView, onToggle = onToggleView)
        }

        Spacer(Modifier.height(AppSpacing.item))

        AppSearchBar(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = "What service do you need?",
            height = 52.dp,
        )

        Spacer(Modifier.height(AppSpacing.item))

        CategoryPillRow(
            selected = selectedCategory,
            onSelect = onCategorySelect,
            contentPadding = PaddingValues(horizontal = 0.dp),
        )
    }
}

/** Segmented Map / Browse control. */
@Composable
private fun ViewToggle(isMapView: Boolean, onToggle: () -> Unit) {
    Surface(shape = AppRadii.chip, color = kSurface, shadowElevation = 2.dp) {
        Row {
            ToggleSegment("Map", isMapView, IconMap) { if (!isMapView) onToggle() }
            ToggleSegment("Browse", !isMapView, IconList) { if (isMapView) onToggle() }
        }
    }
}

@Composable
private fun ToggleSegment(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(if (selected) kPrimaryRed else Color.Transparent, AppRadii.chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) kSurface else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(kSpaceXS))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) kSurface else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** The Browse mode list. */
@Composable
private fun BrowseProviderList(
    providers: List<ServiceProvider>,
    selectedProvider: ServiceProvider?,
    onProviderClick: (ServiceProvider) -> Unit,
) {
    if (providers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                title = "No providers found",
                subtitle = "Try a different category or clear your search.",
                icon = IconSearch,
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        // Leave room for the header overlay and the count badge.
        contentPadding = PaddingValues(
            start = AppSpacing.gutter,
            end = AppSpacing.gutter,
            top = 190.dp,
            bottom = kSpaceXL + kSpaceLG,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.card),
    ) {
        items(providers, key = { it.id }) { provider ->
            ProviderListCard(
                provider = provider,
                selected = selectedProvider?.id == provider.id,
                onClick = { onProviderClick(provider) },
            )
        }
    }
}

/** One provider as a browse row. */
@Composable
private fun ProviderListCard(
    provider: ServiceProvider,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val category = serviceCategoryOf(provider.category)
    val colors = category.colors

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = AppRadii.tile,
        color = kSurface,
        shadowElevation = if (selected) 6.dp else 2.dp,
    ) {
        Column(Modifier.padding(AppSpacing.cardInner)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppAvatar(
                    name = provider.name,
                    category = category,
                    icon = provider.categoryIcon,
                    size = 48.dp,
                    ringColor = if (selected) colors.accent else null,
                )
                Spacer(Modifier.width(AppSpacing.item + AppSpacing.tight))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = provider.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (provider.isVerified) {
                            Spacer(Modifier.width(kSpaceXS))
                            Icon(
                                imageVector = IconShieldCheck,
                                contentDescription = "Verified",
                                tint = kAccentTeal,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    Text(
                        text = category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = provider.headline,
                        style = MaterialTheme.typography.bodySmall,
                        color = kTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Icon(
                    imageVector = IconArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.height(AppSpacing.item + AppSpacing.tight))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StarRating(rating = provider.rating, showValue = true)
                Spacer(Modifier.width(AppSpacing.chip))
                StatusPill(provider.status, showLabel = true)
                Spacer(Modifier.weight(1f))
                Text(
                    text = provider.formatPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = kPrimaryRed,
                )
            }
        }
    }
}

/**
 * Provider preview sheet, shown when a marker or list row is tapped.
 *
 * The primary action opens the provider profile; the secondary starts a
 * service request. These previously both navigated to the same place.
 */
@Composable
fun ProviderPreviewSheet(
    provider: ServiceProvider,
    onViewProfile: () -> Unit,
    onRequestService: () -> Unit,
) {
    val category = serviceCategoryOf(provider.category)
    val colors = category.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = kSpaceLG)
            .padding(bottom = kSpaceXL),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppAvatar(
                name = provider.name,
                category = category,
                icon = provider.categoryIcon,
                size = 56.dp,
            )
            Spacer(Modifier.width(kSpaceMD))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (provider.isVerified) {
                        Spacer(Modifier.width(kSpaceXS))
                        TagBadge(text = "Verified", foreground = kAccentTeal, background = colors.container)
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${category.label} · ${provider.headline}",
                    style = MaterialTheme.typography.bodySmall,
                    color = kTextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(kSpaceMD))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.item),
        ) {
            MetricTile(
                value = provider.formatRating(),
                label = "Rating",
                accent = com.homeapp.core.theme.kAccentGold,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                value = "${provider.jobsCompleted}",
                label = "Jobs",
                accent = colors.accent,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(AppSpacing.item))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.item),
        ) {
            MetricTile(
                value = "${provider.distanceKm.toInt()}",
                label = "km away",
                accent = kAccentTeal,
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                value = "${provider.etaMinutes}m",
                label = "Arrival",
                accent = kPrimaryRed,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(kSpaceMD))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Starting price", style = MaterialTheme.typography.labelMedium, color = kTextSecondary)
                Text(
                    text = provider.formatPrice(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = kPrimaryRed,
                )
            }
            StatusPill(provider.status)
        }

        Spacer(Modifier.height(kSpaceMD))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(kSpaceSM),
        ) {
            OutlineButton(
                text = "View Profile",
                onClick = onViewProfile,
                modifier = Modifier.weight(1f),
                height = 48.dp,
                shape = com.homeapp.core.theme.kRadiusMD,
            )
            CTAButton(
                text = "Request Service",
                onClick = onRequestService,
                modifier = Modifier.weight(1f),
                height = 48.dp,
                shape = com.homeapp.core.theme.kRadiusMD,
            )
        }
    }
}
