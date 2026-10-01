package com.homeapp.features.services.booking2

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.model.serviceCategory
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.TagBadge

data class RequestUiState(
    val description: String = "",
    val serviceSubType: String = "",
    val locationText: String = "Kampala, Uganda",
    val urgency: String = "NOW",
    val scheduledDate: String = "",
    val scheduledTime: String = "",
    val isBusy: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = description.isNotBlank() && !isBusy
}

private val subTypes = listOf("Inspection", "Repair", "Installation", "Maintenance", "General")
private val urgencyOptions = listOf("NOW", "Schedule")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RequestServiceScreen(
    onBack: () -> Unit,
    onSubmit: (Long) -> Unit,
    viewModel: RequestServiceViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val provider by viewModel.provider.collectAsState()

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
                Text("Request Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            val p = provider
            if (p != null) {
                Spacer(Modifier.height(kSpaceMD))
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(kSpaceMD), verticalAlignment = Alignment.CenterVertically) {
                        AppAvatar(
                            name = p.name,
                            category = p.serviceCategory,
                            icon = p.categoryIcon,
                            size = 40.dp,
                        )
                        Spacer(Modifier.width(kSpaceSM))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Request from ${p.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(p.category, style = MaterialTheme.typography.labelMedium, color = kTextSecondary)
                        }
                        TagBadge(text = p.formatPrice(), foreground = kPrimaryRed)
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))

            Text("What do you need help with?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM), verticalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                subTypes.forEach { type ->
                    val selected = type == state.serviceSubType
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.updateSubType(type) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(type, style = MaterialTheme.typography.labelMedium, color = if (selected) kSurface else kPrimaryRed)
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::updateDescription,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("Describe the issue in detail...", color = kTextSecondary) },
                shape = kRadiusMD,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = kPrimaryRed, cursorColor = kPrimaryRed),
            )

            Spacer(Modifier.height(kSpaceMD))

            Text("When do you need the service?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))

            FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                urgencyOptions.forEach { opt ->
                    val selected = opt == state.urgency
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.updateUrgency(opt) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(opt, style = MaterialTheme.typography.labelMedium, color = if (selected) kSurface else kPrimaryRed)
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))

            if (state.urgency == "Schedule") {
                OutlinedTextField(value = state.scheduledDate, onValueChange = viewModel::updateScheduledDate,
                    label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(kSpaceSM))
                OutlinedTextField(value = state.scheduledTime, onValueChange = viewModel::updateScheduledTime,
                    label = { Text("Time (HH:mm), Kampala") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(kSpaceMD))
            }
            Text("Service location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))
            OutlinedTextField(
                value = state.locationText,
                onValueChange = viewModel::updateLocation,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Address or location", color = kTextSecondary) },
                shape = kRadiusMD,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = kPrimaryRed, cursorColor = kPrimaryRed),
            )

            state.error?.let {
                Spacer(Modifier.height(kSpaceSM))
                Text(it, color = kPrimaryRed, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(kSpaceMD))

            CTAButton(
                text = "Review request",
                onClick = { viewModel.submit(onSubmit) },
                enabled = state.canSubmit,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}
