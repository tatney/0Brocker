package com.homeapp.features.property

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconCheck
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kStatusSuccess
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppStateIcon
import com.homeapp.core.widgets.AppTopBar
import com.homeapp.core.widgets.CTAButton

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostPropertyScreen(
    onBack: () -> Unit = {},
    onDone: () -> Unit = {},
    viewModel: PostPropertyViewModel = viewModel { PostPropertyViewModel() },
) {
    val state by viewModel.ui.collectAsState()

    if (state.isDone) {
        PostSuccess(onDone = onDone)
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // This screen was previously a tab with no route, so it had no way back.
        // The bar sits outside the padded form so it can run full-bleed.
        AppTopBar(
            title = "Post a Property",
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = kSpaceMD),
        ) {
            Spacer(Modifier.height(kSpaceXS))
            Text(
                text = "List directly as the owner. No brokers, agents or resale leads.",
                style = MaterialTheme.typography.bodyMedium,
                color = kTextSecondary,
            )

        Spacer(Modifier.height(kSpaceLG))
        FieldLabel("Listing type")
        Spacer(Modifier.height(kSpaceSM))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
            ListingTypeOption.entries.forEach { option ->
                val selected = option == state.listingType
                Box(
                    modifier = Modifier
                        .clip(kRadiusMD)
                        .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                        .clickable { viewModel.onListingTypeChange(option) }
                        .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = option.icon,
                            contentDescription = null,
                            tint = if (selected) kSurface else kPrimaryRed,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(kSpaceSM))
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) kSurface else kPrimaryRed,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(kSpaceLG))
        FieldLabel("Title")
        Spacer(Modifier.height(kSpaceSM))
        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 2 BHK in Kololo", color = kTextSecondary) },
            shape = kRadiusMD,
            colors = fieldColors(),
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(kSpaceMD))
        FieldLabel("Location")
        Spacer(Modifier.height(kSpaceSM))
        OutlinedTextField(
            value = state.location,
            onValueChange = viewModel::onLocationChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 9th Street, Kololo", color = kTextSecondary) },
            shape = kRadiusMD,
            colors = fieldColors(),
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(kSpaceMD))
        FieldLabel("City")
        Spacer(Modifier.height(kSpaceSM))
        OutlinedTextField(
            value = state.city,
            onValueChange = viewModel::onCityChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = kRadiusMD,
            colors = fieldColors(),
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(kSpaceMD))
        FieldLabel("Price (UGX; rent per month, sale total)")
        Spacer(Modifier.height(kSpaceSM))
        OutlinedTextField(
            value = state.priceText,
            onValueChange = viewModel::onPriceChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("e.g. 18500", color = kTextSecondary) },
            prefix = { Text("UGX ", fontWeight = FontWeight.Bold, color = kPrimaryRed) },
            shape = kRadiusMD,
            colors = fieldColors(),
            textStyle = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(kSpaceLG))
        FieldLabel("Configuration")
        Spacer(Modifier.height(kSpaceSM))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
            bedroomOptions.forEach { bedroom ->
                val selected = bedroom == state.bedrooms
                Box(
                    modifier = Modifier
                        .clip(kRadiusMD)
                        .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                        .clickable { viewModel.onBedroomsChange(bedroom) }
                        .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                ) {
                    Text(
                        text = bedroom,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) kSurface else kPrimaryRed,
                    )
                }
            }
        }

        Spacer(Modifier.height(kSpaceMD))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = state.ownerDeclared, onCheckedChange = viewModel::onOwnerDeclared)
            Text("I own this property. I am not listing as a broker or intermediary.", style = MaterialTheme.typography.bodyMedium)
        }
        Text("Owner declaration is not identity or title verification. New listings remain unverified.", style = MaterialTheme.typography.bodySmall, color = kTextSecondary)
        state.error?.let { msg ->
            Spacer(Modifier.height(kSpaceSM))
            Text(text = msg, color = kPrimaryRed, style = MaterialTheme.typography.labelMedium)
        }

        Spacer(Modifier.height(kSpaceLG))
        CTAButton(
            text = "Post listing",
            onClick = { viewModel.postProperty() },
            enabled = state.canSubmit,
        )
        Spacer(Modifier.height(kSpaceMD))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = kPrimaryRed,
    cursorColor = kPrimaryRed,
)

@Composable
private fun PostSuccess(onDone: () -> Unit = {}) {
    Box(
        modifier = Modifier.fillMaxSize().padding(kSpaceMD),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppStateIcon(icon = IconCheck, tint = kStatusSuccess)
            Spacer(Modifier.height(kSpaceMD))
            Text(
                text = "Listing posted!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            Text(
                text = "Your listing is saved on this test device. Verification is pending; it is not published online.",
                style = MaterialTheme.typography.bodyLarge,
                color = kTextSecondary,
            )
            Spacer(Modifier.height(kSpaceLG))
            // This screen is a pushed route now, so the success state needs a
            // way out; previously it was a tab the user could just leave.
            CTAButton(
                text = "Back to Property",
                onClick = onDone,
            )
        }
    }
}
