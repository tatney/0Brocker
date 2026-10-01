package com.homeapp.features.services

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.model.colors
import com.homeapp.core.model.serviceCategory
import com.homeapp.core.widgets.AppStateIcon
import com.homeapp.core.icons.IconCalendar
import com.homeapp.core.icons.IconStar
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kTealLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.TagBadge
import com.homeapp.data.model.ServiceProfessional

@Composable
fun ServiceDetailScreen(
    onBack: () -> Unit,
    onBookNow: (Long) -> Unit,
    viewModel: ServiceDetailViewModel = viewModel(),
) {
    val professional by viewModel.professional.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val p = professional
        if (p == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Loading professional…", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            item { HeroHeader(p, onBack) }
            item { StatsSection(p) }
            item { DescriptionSection(p) }
            item { PriceCard(p) }
            item {
                Spacer(Modifier.height(kSpaceSM))
                CTAButton(
                    text = "Book now — ${p.formatPrice()}",
                    onClick = { onBookNow(p.id) },
                    icon = IconCalendar,
                )
                Spacer(Modifier.height(kSpaceMD))
            }
        }
    }
}

@Composable
private fun HeroHeader(professional: ServiceProfessional, onBack: () -> Unit) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.linearGradient(listOf(kTealLight, MaterialTheme.colorScheme.surface)),
                ),
            contentAlignment = Alignment.Center,
        ) {
            AppStateIcon(
                icon = professional.categoryIcon,
                tint = professional.serviceCategory.colors.accent,
                size = 96.dp,
            )
        }
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = kSpaceSM, top = kSpaceSM)
                .background(MaterialTheme.colorScheme.surface, CircleShape),
        ) {
            Icon(imageVector = IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun StatsSection(professional: ServiceProfessional) {
    Column(modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM)) {
        Text(
            text = professional.name,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(kSpaceXS))
        Text(
            text = professional.headline,
            style = MaterialTheme.typography.bodyLarge,
            color = kTextSecondary,
        )
        Spacer(Modifier.height(kSpaceSM))
        Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
            TagBadge(text = professional.category, foreground = kAccentTeal)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = IconStar,
                    contentDescription = null,
                        tint = kAccentGold,
                    modifier = Modifier.size(13.dp),
                )
                Spacer(Modifier.width(kSpaceXS))
                StarRating(rating = professional.rating, showValue = true)
            }
        }
        Spacer(Modifier.height(kSpaceSM))
        Text(
            text = "${professional.bookingsCount} bookings completed",
            style = MaterialTheme.typography.bodyMedium,
            color = kTextSecondary,
        )
    }
}

@Composable
private fun DescriptionSection(professional: ServiceProfessional) {
    Column(modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM)) {
        Text(
            text = "About ${professional.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(kSpaceXS))
        Text(
            text = "${professional.name} is a verified ${professional.category.lowercase()} professional " +
                "with ${professional.bookingsCount} completed bookings and a ${professional.rating} star rating. " +
                "Book for doorstep service — typically available within 2 hours.",
            style = MaterialTheme.typography.bodyMedium,
            color = kTextSecondary,
        )
    }
}

@Composable
private fun PriceCard(professional: ServiceProfessional) {
    AppCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = kSpaceMD),
        shape = kRadiusXL,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(kSpaceMD),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = professional.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Standard service charge",
                    style = MaterialTheme.typography.labelMedium,
                    color = kTextSecondary,
                )
                Spacer(Modifier.height(kSpaceXS))
                Text(
                    text = professional.formatPrice(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = kPrimaryRed,
                )
            }
        }
    }
}
