package com.homeapp.features.services

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.icons.IconCheck
import com.homeapp.core.model.categoryIcon
import com.homeapp.core.model.serviceCategory
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kStatusSuccess
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.AppStateIcon
import com.homeapp.core.widgets.CTAButton
import com.homeapp.core.widgets.TagBadge

private val dates = listOf("Today", "Tomorrow", "Day after")
private val times = listOf("Morning (9 AM–12 PM)", "Afternoon (12–3 PM)", "Evening (3–6 PM)")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServiceBookingScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: BookingViewModel = viewModel(),
) {
    val state by viewModel.ui.collectAsState()
    val p = viewModel.professional.collectAsState().value

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (state.isConfirmed) {
            BookingSuccess(onDone)
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = kSpaceMD),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Book service",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (p != null) {
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
                                text = p.name,
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
            Text(
                text = "Select date",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                dates.forEach { date ->
                    val selected = date == state.selectedDate
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.selectDate(date) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) kSurface else kPrimaryRed,
                        )
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))
            Text(
                text = "Preferred time slot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                times.forEach { time ->
                    val selected = time == state.selectedTime
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.selectTime(time) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) kSurface else kPrimaryRed,
                        )
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))
            Text(
                text = "Service address",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            OutlinedTextField(
                value = state.address,
                onValueChange = viewModel::onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Flat / door, street, landmark", color = kTextSecondary) },
                shape = kRadiusMD,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = kPrimaryRed,
                    cursorColor = kPrimaryRed,
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            state.error?.let { msg ->
                Spacer(Modifier.height(kSpaceSM))
                Text(text = msg, color = kPrimaryRed, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.weight(1f))
            CTAButton(
                text = "Confirm booking",
                onClick = { viewModel.confirmBooking(onDone) },
                enabled = state.canSubmit,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}

@Composable
private fun BookingSuccess(onDone: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(kSpaceMD),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppStateIcon(icon = IconCheck, tint = kStatusSuccess)
            Spacer(Modifier.height(kSpaceMD))
            Text(
                text = "Booking confirmed!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            Text(
                text = "Your service professional will contact you shortly to confirm the arrival time.",
                style = MaterialTheme.typography.bodyLarge,
                color = kTextSecondary,
            )
            Spacer(Modifier.height(kSpaceLG))
            CTAButton(text = "Back to services", onClick = onDone)
        }
    }
}
