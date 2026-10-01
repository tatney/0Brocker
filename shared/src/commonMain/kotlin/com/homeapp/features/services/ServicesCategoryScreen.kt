package com.homeapp.features.services

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.model.serviceCategory
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.icons.IconStar
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.EmptyState
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.TagBadge
import com.homeapp.data.model.ServiceProfessional

@Composable
fun ServicesCategoryScreen(
    onBack: () -> Unit,
    onServiceClick: (Long) -> Unit,
    viewModel: ServicesCategoryViewModel = viewModel(),
) {
    val professionals by viewModel.professionals.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = kSpaceMD, vertical = kSpaceXS),
        verticalArrangement = Arrangement.spacedBy(kSpaceSM),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = viewModel.category,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (professionals.isEmpty()) {
            item {
                EmptyState(
                    title = "No professionals found",
                    subtitle = "Check back soon for ${viewModel.category} services.",
                )
            }
        } else {
            items(professionals, key = { it.id }) { professional ->
                ProfessionalCard(professional, onClick = { onServiceClick(professional.id) })
            }
        }
    }
}

@Composable
private fun ProfessionalCard(professional: ServiceProfessional, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
                modifier = Modifier.padding(kSpaceMD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(
                name = professional.name,
                category = professional.serviceCategory,
                icon = professional.categoryIcon,
                size = 56.dp,
            )
            Spacer(Modifier.width(kSpaceSM))
            Column(Modifier.weight(1f)) {
                Text(
                    text = professional.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = professional.headline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = kTextSecondary,
                )
                Spacer(Modifier.height(kSpaceXS))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = IconStar,
                        contentDescription = null,
                        tint = kAccentGold,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(kSpaceXS))
                    StarRating(rating = professional.rating, showValue = true)
                    Spacer(Modifier.width(kSpaceSM))
                    TagBadge(text = professional.formatPrice(), foreground = kPrimaryRed)
                }
            }
        }
    }
}
