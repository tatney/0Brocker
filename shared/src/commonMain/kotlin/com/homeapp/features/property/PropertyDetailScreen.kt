package com.homeapp.features.property

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.model.assetIcon
import com.homeapp.core.model.assetStyle
import com.homeapp.core.model.colors
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.widgets.AppStateIcon
import com.homeapp.core.icons.IconBookmark
import com.homeapp.core.icons.IconChat
import com.homeapp.core.icons.IconLocationPin
import com.homeapp.core.icons.IconPhone
import com.homeapp.core.map.MapContract
import com.homeapp.core.map.MapPoint
import com.homeapp.core.map.MapUi
import com.homeapp.core.map.StaticMapPlaceholder
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kTealLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.OutlineButton
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.TagBadge
import com.homeapp.data.model.Property

/** Full-screen property detail rendered via navigation. */
@Composable
fun PropertyDetailScreen(
    onBack: () -> Unit,
    onOpenThread: (Long) -> Unit = {},
    mapContract: MapContract = StaticMapPlaceholder,
    viewModel: PropertyDetailViewModel = viewModel(),
) {
    val property by viewModel.property.collectAsState()
    val saved by viewModel.isSaved.collectAsState()
    var showContact by remember { mutableStateOf(false) }
    if (showContact) AlertDialog(onDismissRequest = { showContact = false }, title = { Text("Direct owner contact") },
        text = { Text("No owner phone number has been supplied. You can test a direct conversation on this device; messages are not delivered to a real owner in v1.") },
        confirmButton = { TextButton(onClick = { showContact = false; viewModel.messageOwner(onOpenThread) }) { Text("Test conversation") } },
        dismissButton = { TextButton(onClick = { showContact = false }) { Text("Close") } })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (property != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                        OutlineButton(
                            text = "Contact",
                            onClick = { showContact = true },
                            icon = IconPhone,
                            modifier = Modifier.weight(1f),
                        )
                        CTAButton(
                            text = "Message owner",
                            onClick = { viewModel.messageOwner(onOpenThread) },
                            icon = IconChat,
                            modifier = Modifier.weight(1.4f),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val p = property
        if (p == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading listing…", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = kSpaceSM),
        ) {
            item { HeroHeader(p, saved, viewModel::onToggleFavorite, onBack) }
            item { PriceBanner(p) }
            item { AmenitiesSection(p) }
            item { DescriptionSection(p) }
            item { MapSection(p, mapContract) }
            item { LandlordCard(p) }
        }
    }
}

@Composable
private fun HeroHeader(
    property: Property,
    saved: Boolean,
    onToggleSave: () -> Unit,
    onBack: () -> Unit,
) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(kTealLight, MaterialTheme.colorScheme.surface),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            AppStateIcon(
                icon = property.assetIcon,
                tint = property.assetStyle.colors.accent,
                size = 104.dp,
                modifier = Modifier.padding(top = kSpaceLG),
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = kSpaceMD, bottom = kSpaceMD),
                horizontalArrangement = Arrangement.spacedBy(kSpaceSM),
            ) {
                TagBadge(
                    text = property.title.substringAfter(" in ", missingDelimiterValue = property.bedrooms),
                )
            }
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = kSpaceSM, top = kSpaceSM)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        ) {
            Icon(imageVector = IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(
            onClick = onToggleSave,
            modifier = Modifier
                .padding(end = kSpaceSM, top = kSpaceSM)
                .align(Alignment.TopEnd)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        ) {
            Icon(
                imageVector = IconBookmark,
                contentDescription = "Save",
                tint = if (saved) kPrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PriceBanner(property: Property) {
        Column(modifier = Modifier.padding(horizontal = kSpaceMD).padding(top = kSpaceSM)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = property.formatPrice(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = kPrimaryRed,
            )
            Spacer(Modifier.width(kSpaceSM))
            TagBadge(text = property.priceUnitLabel(), foreground = kTextSecondary, background = MaterialTheme.colorScheme.surfaceVariant)
        }
        Spacer(Modifier.height(kSpaceSM))
        Text(
            text = property.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(kSpaceXS))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = IconLocationPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(kSpaceXS))
            Text(
                text = "${property.location}, ${property.city}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(kSpaceSM))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (property.isVerified) {
                    TagBadge(text = "Sample checked", foreground = kAccentTeal)
                }
            }
        }
        Spacer(Modifier.height(kSpaceSM))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarRating(rating = property.rating, showValue = true)
            Spacer(Modifier.width(kSpaceXS))
            Text(
                text = "· ${property.bedrooms}",
                style = MaterialTheme.typography.bodyMedium,
                color = kTextSecondary,
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceXS),
    )
}

@Composable
private fun AmenitiesSection(property: Property) {
    Box(modifier = Modifier.padding(top = kSpaceMD)) {
        Column {
            SectionTitle("Highlights")
            val amenities = property.amenities()
            AppCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = kSpaceMD),
        ) {
            Column(Modifier.padding(kSpaceMD), verticalArrangement = Arrangement.spacedBy(kSpaceMD)) {
                amenities.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                            row.forEach { (label, value) ->
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = value,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = kTextSecondary,
                                    )
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DescriptionSection(property: Property) {
    Column(modifier = Modifier.padding(top = kSpaceMD)) {
        SectionTitle("About this ${if (property.isRent()) "rental" else "property"}")
        Text(
            text = "Located at " + property.location + ", " + property.city + ". Confirm the condition, deposit, measurements and ownership documents directly with the owner before committing. V1 sample listings and map locations are illustrative.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = kSpaceMD),
        )
    }
}

@Composable
private fun MapSection(property: Property, mapContract: MapContract) {
    Column(modifier = Modifier.padding(top = kSpaceMD)) {
        SectionTitle("Location")
        AppCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = kSpaceMD),
            shape = kRadiusXL,
        ) {
            Box(modifier = Modifier.height(200.dp)) {
                mapContract.Draw(
                    ui = MapUi(
                        points = listOf(MapPoint("${property.location}, ${property.city}")),
                        centerLabel = "${property.location}, ${property.city}",
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun LandlordCard(property: Property) {
    Column(modifier = Modifier.padding(top = kSpaceMD)) {
        SectionTitle("Owner contact")
        AppCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = kSpaceMD),
            shape = kRadiusLG,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(kSpaceMD),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppAvatar(
                    name = property.ownerName.ifBlank { "Property owner" },
                    size = 48.dp,
                )
                Spacer(Modifier.width(kSpaceSM))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = property.ownerName.ifBlank { "Property owner" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Direct contact � no intermediary",
                        style = MaterialTheme.typography.labelSmall,
                        color = kTextSecondary,
                    )
                }
                Text(
                    text = if (property.ownerDeclared) "Owner declared" else "Sample listing",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = kAccentTeal,
                )
            }
        }
        Spacer(Modifier.height(kSpaceMD))
    }
}
