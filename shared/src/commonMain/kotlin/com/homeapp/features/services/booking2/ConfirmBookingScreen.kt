package com.homeapp.features.services.booking2

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.model.serviceCategoryOf
import com.homeapp.core.model.icon
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.theme.kDivider
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.CTAButton
import com.homeapp.data.model.formatUgx

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConfirmBookingScreen(
    onBack: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: ConfirmBookingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val booking by viewModel.booking.collectAsState()

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
                Text("Confirm Booking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(kSpaceMD))

            val booking = booking
            val baseCost = booking?.baseCost ?: 0
            val platformFee = booking?.platformFee ?: 0
            val totalCost = booking?.totalCost ?: 0

            // Provider card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(kPrimaryRedLight, kRadiusLG)
                    .padding(kSpaceMD),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAvatar(
                        name = booking?.providerName?.ifBlank { "Provider" } ?: "Provider",
                        category = serviceCategoryOf(booking?.serviceType.orEmpty()),
                        icon = serviceCategoryOf(booking?.serviceType.orEmpty()).icon,
                        size = 48.dp,
                    )
                    Spacer(Modifier.width(kSpaceSM))
                    Column {
                        Text(booking?.providerName?.ifBlank { "Provider" } ?: "Provider", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(booking?.serviceType ?: "Service", style = MaterialTheme.typography.bodySmall, color = kTextSecondary)
                    }
                }
            }

            Spacer(Modifier.height(kSpaceMD))

            // Summary
            SummaryRow("Service", booking?.serviceType ?: "Service")
            SummaryRow("Provider", booking?.providerName?.ifBlank { "Provider" } ?: "Provider")
            SummaryRow("Location", booking?.locationText?.ifBlank { "Kampala, Uganda" } ?: "Kampala, Uganda")
            SummaryRow("Description", booking?.description?.ifBlank { "—" } ?: "—")

            Spacer(Modifier.height(kSpaceSM))
            Divider(color = kDivider)
            Spacer(Modifier.height(kSpaceSM))

            // Cost breakdown
            SummaryRow("Service cost", formatUgx(baseCost.toDouble()))
            SummaryRow("Brokerage fee", formatUgx(platformFee.toDouble()))
            Spacer(Modifier.height(kSpaceSM))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(formatUgx(totalCost.toDouble()), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = kPrimaryRed)
            }

            Spacer(Modifier.height(kSpaceMD))

            // Payment method
            Text("Payment method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                listOf("Mobile Money", "Wallet", "Card", "Cash").forEach { method ->
                    val selected = method == state.paymentMethod
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.updatePayment(method) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(method, style = MaterialTheme.typography.labelMedium, color = if (selected) kSurface else kPrimaryRed)
                    }
                }
            }

            Spacer(Modifier.height(kSpaceLG))

            Text("Testing only. This starting-price estimate is not a final quote. No money is charged; agree scope and materials directly with the provider.", style = MaterialTheme.typography.bodySmall, color = kTextSecondary)
            CTAButton(text = "Confirm Booking — ${formatUgx(totalCost.toDouble())}", onClick = { viewModel.confirm(onConfirm) }, enabled = booking != null && !state.isBusy)
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceXS),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = kTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
