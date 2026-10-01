package com.homeapp.features.property

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconLocationPin
import androidx.compose.ui.graphics.vector.ImageVector
import com.homeapp.core.icons.IconBuilding
import com.homeapp.core.icons.IconChevronRight
import com.homeapp.core.icons.IconClock
import com.homeapp.core.icons.IconHome
import com.homeapp.core.icons.IconShieldCheck
import com.homeapp.core.icons.IconSparkle
import com.homeapp.core.icons.IconSliders
import com.homeapp.core.icons.IconUpload
import com.homeapp.core.model.assetIcon
import com.homeapp.core.model.assetStyle
import com.homeapp.core.model.colors
import com.homeapp.core.model.icon
import com.homeapp.core.model.PropertyAssetStyle
import com.homeapp.core.widgets.AppIconTile
import com.homeapp.core.icons.IconMap
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryDark
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.AppSearchBar
import com.homeapp.core.widgets.CategoryIconRail
import com.homeapp.core.widgets.SectionHeader
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.TagBadge
import com.homeapp.data.model.Property
import com.homeapp.data.model.ServiceCategory

private val listingTypes = listOf(
    "Rent" to "RENT",
    "Buy" to "BUY",
)

private val assetTypes = listOf(
    "House" to "HOUSE",
    "Land" to "LAND",
    "Car" to "CAR",
)

private val categories = listOf(
    "Residential" to "RESIDENTIAL",
    "Commercial" to "COMMERCIAL",
)

data class PropertyFilterChip(
    val label: String,
    val icon: ImageVector,
    val value: String,
)

@Composable
fun PropertyHubScreen(
    modifier: Modifier = Modifier,
    onPropertyClick: (Long) -> Unit = {},
    onSavedClick: () -> Unit = {},
    onHomeServices: () -> Unit = {},
    onServiceCategoryClick: (ServiceCategory) -> Unit = {},
    onPostProperty: () -> Unit = {},
    onNewListings: () -> Unit = {},
    onPackersMovers: () -> Unit = {},
    viewModel: PropertyViewModel = viewModel { PropertyViewModel() },
) {
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val properties by viewModel.properties.collectAsState()

    var compared by remember { mutableStateOf<List<Property>>(emptyList()) }
    var compareOpen by remember { mutableStateOf(false) }
    if (compareOpen) PropertyComparison(compared) { compareOpen = false }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = kSpaceMD, end = kSpaceMD, top = kSpaceMD, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(kSpaceMD),
    ) {
        item {
            HeroHeader()
        }
        item {
            AppSearchBar(
                value = query,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = "Search houses, land, cars...",
            )
        }
        item { DiscoveryControls(viewModel) }
        if (compared.isNotEmpty()) item {
            Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                TextButton(enabled = compared.size >= 2, onClick = { compareOpen = true }) { Text("Compare " + compared.size + "/3") }
                TextButton(onClick = { compared = emptyList() }) { Text("Clear selection") }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                TextButton(onClick = onPostProperty) { Text("Post as owner") }
                TextButton(onClick = onSavedClick) { Text("Saved listings") }
            }
        }
        item {
            SectionHeader(
                title = "Your matches",
                subtitle = "${properties.size} listing${if (properties.size == 1) "" else "s"} found",
                actionLabel = "View all",
                onAction = viewModel::clearFilters,
            )
        }
        items(properties, key = { it.id }) { property ->
            Column {
                PropertyCard(property, onClick = { onPropertyClick(property.id) })
                val selected = compared.any { it.id == property.id }
                TextButton(enabled = selected || compared.size < 3, onClick = {
                    compared = if (selected) compared.filterNot { it.id == property.id } else compared + property
                }) { Text(if (selected) "Remove from comparison" else "Add to comparison") }
            }
        }
    }
}

@Composable
private fun HeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(kRadiusXL)
            .background(
                Brush.linearGradient(
                    listOf(kPrimaryDark, Color(0xFF414B55)),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(kSpaceLG),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconHome,
                    contentDescription = null,
                    tint = kSurface,
                    modifier = Modifier.size(26.dp),
                )
                Spacer(Modifier.width(kSpaceSM))
                Text(
                    text = "Find your next home",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = kSurface,
                    fontSize = 24.sp,
                )
            }
            Spacer(Modifier.height(kSpaceXS))
            Text(
                text = "Spaces to call yours. Kampala & beyond.",
                style = MaterialTheme.typography.bodyMedium,
                color = kSurface.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun QuickActionsRow(
    modifier: Modifier = Modifier,
    onSearchProperty: () -> Unit,
    onPostProperty: () -> Unit,
    onNewListings: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(kSpaceSM),
    ) {
        QuickActionTile(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = IconSliders,
            iconSize = 22.dp,
            title = "Explore homes",
            subtitle = "Buy & Rent,\nResidential & Commercial",
            gradient = Brush.linearGradient(listOf(kPrimaryDark, Color(0xFF414B55))),
            onClick = onSearchProperty,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(kSpaceSM),
        ) {
            QuickActionTile(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                icon = IconUpload,
                iconSize = 20.dp,
                title = "List a property",
                subtitle = "Reach your next buyer",
                gradient = Brush.linearGradient(listOf(Color(0xFF17665E), Color(0xFF258276))),
                onClick = onPostProperty,
            )
            QuickActionTile(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                icon = IconSparkle,
                iconSize = 20.dp,
                title = "New Listings",
                subtitle = "Browse sample properties",
                gradient = Brush.linearGradient(listOf(Color(0xFF565E69), Color(0xFF737D88))),
                onClick = onNewListings,
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    modifier: Modifier,
    icon: ImageVector,
    iconSize: Dp = 20.dp,
    title: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(kSpaceMD),
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = kRadiusMD,
                color = kSurface.copy(alpha = 0.22f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = kSurface,
                        modifier = Modifier.size(iconSize),
                    )
                }
            }
            Icon(
                imageVector = IconChevronRight,
                contentDescription = null,
                tint = kSurface,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp),
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = kSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(kSpaceXS))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = kSurface.copy(alpha = 0.9f),
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

/**
 * Home-services entry point on the property hub.
 *
 * This was an auto-scrolling marquee of emoji strings ("🎨 Painting", …) whose
 * pills all ran the same callback — `onPillClick = { onBannerClick() }` threw
 * away which service was tapped, so every pill just opened the unfiltered map.
 * It is now a typed category rail that deep-links into the prefiltered view, and
 * the emoji are gone from this surface.
 */
@Composable
private fun HomeServicesSection(
    onBannerClick: () -> Unit,
    onCategoryClick: (ServiceCategory) -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(ServiceCategory.ALL.name) }
    val selectedCategory = ServiceCategory.entries.firstOrNull { it.name == selected }
        ?: ServiceCategory.ALL

    Column {
        SectionHeader(
            title = "Home Services",
            subtitle = "Choose a provider directly",
            actionLabel = "Open map",
            onAction = onBannerClick,
            actionIcon = IconMap,
        )
        Spacer(Modifier.height(kSpaceSM))
        CategoryIconRail(
            selected = selectedCategory,
            onSelect = { category ->
                selected = category.name
                onCategoryClick(category)
            },
            contentPadding = PaddingValues(horizontal = 0.dp),
        )
    }
}

@Composable
private fun PackersMoversCard(onClick: () -> Unit) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(kSpaceMD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconTile(
                icon = ServiceCategory.MOVING.icon,
                tint = ServiceCategory.MOVING.colors.accent,
                size = 56.dp,
            )
            Spacer(Modifier.width(kSpaceMD))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Packers & Movers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "House & business shifting services",
                    style = MaterialTheme.typography.bodySmall,
                    color = kTextSecondary,
                )
            }
            Text("→", fontSize = 20.sp, color = kPrimaryRed)
        }
    }
}

@Composable
private fun FilterChips(
    filter: PropertyFilter,
    onListingType: (String?) -> Unit,
    onAssetType: (String?) -> Unit,
    onCategory: (String?) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(kSpaceSM)) {
        FilterRow(
            chips = listingTypes.map { (label, value) ->
                PropertyFilterChip(
                    label,
                    if (value == "RENT") IconClock else IconShieldCheck,
                    value,
                )
            },
            selected = filter.listingType,
            onClick = onListingType,
        )
        if (filter.listingType != null) {
            FilterRow(
                chips = assetTypes.map { (label, value) ->
                    PropertyFilterChip(label, PropertyAssetStyle.of(value).icon, value)
                },
                selected = filter.assetType,
                onClick = onAssetType,
            )
            if (filter.assetType != null) {
                FilterRow(
                    chips = categories.map { (label, value) ->
                        PropertyFilterChip(
                            label,
                            if (value == "RESIDENTIAL") IconHome else IconBuilding,
                            value,
                        )
                    },
                    selected = filter.category,
                    onClick = onCategory,
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    chips: List<PropertyFilterChip>,
    selected: String?,
    onClick: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(kSpaceSM),
    ) {
        chips.forEach { chip ->
            Surface(
                modifier = Modifier.clickable { onClick(if (selected == chip.value) null else chip.value) },
                shape = kRadiusLG,
                color = if (chip.value == selected) kPrimaryRed else MaterialTheme.colorScheme.surface,
                border = if (chip.value == selected) null else BorderStroke(1.dp, kTextSecondary.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(kSpaceXS + 2.dp),
                ) {
                    Icon(
                        imageVector = chip.icon,
                        contentDescription = null,
                        tint = if (chip.value == selected) kSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (chip.value == selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (chip.value == selected) kSurface else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
fun PropertyCard(property: Property, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(kSpaceMD), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIconTile(icon = property.assetIcon, tint = property.assetStyle.colors.accent, size = 64.dp)
                Spacer(Modifier.width(kSpaceMD))
                Column(Modifier.weight(1f)) {
                    Text(property.title, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(kSpaceXS))
                    Text("${property.location}, ${property.city}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(IconChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                TagBadge(text = if (property.isRent()) "For rent" else "For sale", foreground = kPrimaryRed)
                if (property.isVerified) TagBadge(text = "Sample checked", foreground = kAccentTeal)
                Spacer(Modifier.weight(1f))
                StarRating(rating = property.rating, showValue = true, starSize = 12.dp)
            }
            androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                Text(property.formatPrice(), style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(property.priceUnitLabel(), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}