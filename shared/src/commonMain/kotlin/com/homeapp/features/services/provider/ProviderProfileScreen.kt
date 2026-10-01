package com.homeapp.features.services.provider

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.model.colors
import com.homeapp.core.model.serviceCategory
import com.homeapp.core.widgets.AppStateIcon
import com.homeapp.core.icons.IconStar
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTealLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.StarRating
import com.homeapp.core.widgets.TagBadge

@Composable
fun ProviderProfileScreen(
    onBack: () -> Unit,
    onRequestService: () -> Unit,
    onScheduleService: () -> Unit,
    viewModel: ProviderProfileViewModel = viewModel(),
) {
    val provider by viewModel.provider.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        val p = provider
        if (p == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Loading provider...", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Hero
            item {
                Box {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Brush.linearGradient(listOf(kTealLight, MaterialTheme.colorScheme.surface))),
                        contentAlignment = Alignment.Center,
                    ) {
                        AppStateIcon(
                            icon = p.categoryIcon,
                            tint = p.serviceCategory.colors.accent,
                            size = 96.dp,
                        )
                    }
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = kSpaceSM, top = kSpaceSM).background(kSurface, CircleShape),
                    ) {
                        Icon(IconBack, contentDescription = "Back", tint = Color.Black)
                    }
                }
            }

            // Identity
            item {
                Column(modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(p.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                        if (p.isVerified) {
                            Spacer(Modifier.width(kSpaceSM))
                            TagBadge(text = "✓ Verified", foreground = kAccentTeal)
                        }
                    }
                    Spacer(Modifier.height(kSpaceXS))
                    Text(p.headline, style = MaterialTheme.typography.bodyLarge, color = kTextSecondary)
                    Spacer(Modifier.height(kSpaceSM))
                    Row(horizontalArrangement = Arrangement.spacedBy(kSpaceMD)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(IconStar, null, tint = kAccentGold, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(kSpaceXS))
                            StarRating(rating = p.rating, showValue = true)
                            Spacer(Modifier.width(kSpaceXS))
                            Text("(${p.reviewsCount})", style = MaterialTheme.typography.labelSmall, color = kTextSecondary)
                        }
                    }
                    Spacer(Modifier.height(kSpaceSM))
                    Row(horizontalArrangement = Arrangement.spacedBy(kSpaceMD)) {
                        InfoChip("${p.jobsCompleted} jobs completed")
                        InfoChip("${p.experienceYears} years experience")
                        InfoChip(p.formatDistance())
                    }
                }
            }

            // Services offered
            item {
                Column(modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM)) {
                    Text("Services Offered", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(kSpaceSM))
                    val serviceList = p.services.ifEmpty {
                        listOf(
                            "General consultation",
                            "Service inspection",
                            "Standard service",
                            "Emergency service",
                            "Follow-up visit",
                        )
                    }
                    serviceList.forEach { service ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceXS),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(service, style = MaterialTheme.typography.bodyMedium)
                            Text(p.formatPrice(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = kPrimaryRed)
                        }
                    }
                }
            }

            // Pricing
            item {
                AppCard(modifier = Modifier.fillMaxWidth().padding(horizontal = kSpaceMD), shape = kRadiusXL) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(kSpaceMD),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(p.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("Starting price", style = MaterialTheme.typography.labelMedium, color = kTextSecondary)
                        }
                        Text("From ${p.formatPrice()}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = kPrimaryRed)
                    }
                }
            }

            // Credentials
            item {
                Column(modifier = Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceSM)) {
                    Text("Credentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(kSpaceSM))
                    val credentials = listOf(
                        "✓ National ID verified" to true,
                        "✓ Business registered" to p.isVerified,
                        "✓ Professional certified" to p.isVerified,
                        "✓ Background checked" to true,
                        "✓ Insurance" to (p.experienceYears > 3),
                    )
                    credentials.forEach { (text, verified) ->
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (verified) kAccentTeal else kTextSecondary,
                            modifier = Modifier.padding(vertical = kSpaceXS),
                        )
                    }
                }
            }

            // CTAs
            item {
                Spacer(Modifier.height(kSpaceMD))
                CTAButton(text = "Request Service", onClick = onRequestService)
                Spacer(Modifier.height(kSpaceSM))
                CTAButton(text = "Schedule Service", onClick = onScheduleService, backgroundColor = kAccentTeal)
                Spacer(Modifier.height(kSpaceLG))
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    TagBadge(text = text, foreground = kTextSecondary)
}
