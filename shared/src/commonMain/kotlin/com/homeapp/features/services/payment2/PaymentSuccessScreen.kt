package com.homeapp.features.services.payment2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXL
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kSurfaceGrey
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.CTAButton
import com.homeapp.data.model.formatUgx

@Composable
fun PaymentSuccessScreen(
    onViewBooking: () -> Unit,
    onRateProvider: () -> Unit,
    viewModel: PaymentSuccessViewModel = viewModel(),
) {
    val booking by viewModel.booking.collectAsState()
    val totalCost = booking?.totalCost ?: 37000
    val providerName = booking?.providerName?.ifBlank { "Provider" } ?: "Provider"
    val serviceType = booking?.serviceType ?: "Service"
    val paymentMethod = booking?.paymentMethod?.ifBlank { "Mobile Money" } ?: "Mobile Money"

    Scaffold(containerColor = kSurface) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.padding(kSpaceMD),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Success icon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(kAccentTeal.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✅", fontSize = 48.sp)
                }

                Spacer(Modifier.height(kSpaceLG))

                Text(
                    "Payment Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(kSpaceSM))

                Text(
                    "${formatUgx(totalCost.toDouble())} paid successfully",
                    style = MaterialTheme.typography.bodyLarge,
                    color = kTextSecondary,
                )

                Spacer(Modifier.height(kSpaceLG))

                // Transaction details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(kSurfaceGrey, kRadiusLG)
                        .padding(kSpaceMD),
                ) {
                    Column {
                        DetailRow("Amount", formatUgx(totalCost.toDouble()))
                        DetailRow("Provider", providerName)
                        DetailRow("Service", serviceType)
                        DetailRow("Transaction ID", viewModel.transactionId)
                        DetailRow("Date", "Today")
                        DetailRow("Payment Method", paymentMethod)
                    }
                }

                Spacer(Modifier.height(kSpaceXL))

                CTAButton(text = "Rate Provider", onClick = onRateProvider, backgroundColor = kPrimaryRed)
                Spacer(Modifier.height(kSpaceSM))
                CTAButton(
                    text = "View Booking",
                    onClick = onViewBooking,
                    backgroundColor = kSurface,
                    textColor = kPrimaryRed,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceXS),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = kTextSecondary)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
