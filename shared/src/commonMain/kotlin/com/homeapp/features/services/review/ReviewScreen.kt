package com.homeapp.features.services.review

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import com.homeapp.core.model.icon
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.data.model.ServiceCategory
import com.homeapp.core.icons.IconStar
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kIconGrey
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSuccessLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.CTAButton

@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    viewModel: ReviewViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = kSpaceMD),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text("Rate Provider", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(kSpaceMD))

            // Provider card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(kSuccessLight, kRadiusLG)
                    .padding(kSpaceMD),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAvatar(
                        name = "Joseph M.",
                        category = ServiceCategory.PLUMBING,
                        icon = ServiceCategory.PLUMBING.icon,
                        size = 48.dp,
                    )
                    Spacer(Modifier.width(kSpaceSM))
                    Column {
                        Text("Joseph M.", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Plumber • Water Leak Repair", style = MaterialTheme.typography.bodySmall, color = kTextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(kSpaceLG))

            Text("How was your service?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))

            // Overall star rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = IconStar,
                        contentDescription = "$star stars",
                        tint = if (star <= state.overallRating) kAccentGold else kIconGrey,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { viewModel.setOverall(star) },
                    )
                    Spacer(Modifier.width(kSpaceXS))
                }
            }

            if (state.overallRating > 0) {
                Text(
                    text = when (state.overallRating) {
                        1 -> "Poor"
                        2 -> "Fair"
                        3 -> "Good"
                        4 -> "Very Good"
                        5 -> "Excellent"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = kAccentTeal,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            Spacer(Modifier.height(kSpaceLG))

            // Category ratings
            val categories = listOf("Service Quality", "Professionalism", "Timeliness", "Communication", "Value for Money")
            categories.forEachIndexed { index, category ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceSM),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(category, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Row {
                        (1..5).forEach { star ->
                            val rating = state.categoryRatings.getOrElse(index) { 0 }
                            Icon(
                                imageVector = IconStar,
                                contentDescription = null,
                                tint = if (star <= rating) kAccentGold else kIconGrey,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clickable { viewModel.setCategoryRating(index, star) },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))

            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::updateComment,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Tell us about your experience...", color = kTextSecondary) },
                shape = kRadiusMD,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = kPrimaryRed, cursorColor = kPrimaryRed),
            )

            Spacer(Modifier.height(kSpaceLG))

            CTAButton(
                text = "Submit Review",
                onClick = { viewModel.submit(onSubmit) },
                enabled = state.overallRating > 0,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}
