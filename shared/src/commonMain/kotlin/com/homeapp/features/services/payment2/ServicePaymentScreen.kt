package com.homeapp.features.services.payment2

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.icons.IconBroom
import com.homeapp.core.icons.IconCreditCard
import com.homeapp.core.icons.IconQrScan
import com.homeapp.core.icons.IconWallet
import com.homeapp.core.widgets.AppIconTile
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.theme.kTileGrey
import com.homeapp.core.theme.kWalletNavy
import com.homeapp.core.widgets.CTAButton
import com.homeapp.data.model.formatUgx

private val paymentMethods = listOf(
    PaymentMethod("Mobile Money", "MTN / Airtel", IconQrScan),
    PaymentMethod("Card", "Visa / Mastercard", IconCreditCard),
    PaymentMethod("Wallet", "UGX 124,750", IconWallet),
    PaymentMethod("Cash", "Pay on delivery", IconBroom),
)

private data class PaymentMethod(
    val name: String,
    val subtitle: String,
    val icon: ImageVector,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServicePaymentScreen(
    onBack: () -> Unit,
    onPay: () -> Unit,
    viewModel: ServicePaymentViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val booking by viewModel.booking.collectAsState()
    val totalCost = booking?.totalCost ?: 37000

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
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
                Text("Payment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(kSpaceLG))

            // Amount due
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(kWalletNavy, kRadiusXL)
                    .padding(kSpaceLG),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amount Due", style = MaterialTheme.typography.bodyLarge, color = kSurface.copy(alpha = 0.7f))
                    Spacer(Modifier.height(kSpaceXS))
                    Text(
                        formatUgx(totalCost.toDouble()),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = kSurface,
                    )
                    Spacer(Modifier.height(kSpaceXS))
                    Text(
                        "${booking?.providerName?.ifBlank { "Provider" } ?: "Provider"} • ${booking?.serviceType ?: "Service"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = kSurface.copy(alpha = 0.6f),
                    )
                }
            }

            Spacer(Modifier.height(kSpaceLG))

            Text("Select payment method", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(kSpaceSM))

            paymentMethods.forEach { method ->
                val name = method.name
                val selected = name == state.selectedMethod
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = kSpaceXS)
                        .clip(kRadiusMD)
                        .background(if (selected) kPrimaryRedLight else kSurface)
                        .clickable { viewModel.selectMethod(name) }
                        .padding(kSpaceMD),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppIconTile(
                        icon = method.icon,
                        tint = if (selected) kPrimaryRed else kTextSecondary,
                        container = if (selected) kPrimaryRedLight else kTileGrey,
                        size = 44.dp,
                        shape = CircleShape,
                    )
                    Spacer(Modifier.width(kSpaceMD))
                    Column(Modifier.weight(1f)) {
                        Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(method.subtitle, style = MaterialTheme.typography.labelSmall, color = kTextSecondary)
                    }
                    if (selected) {
                        Box(
                            modifier = Modifier.size(22.dp).background(kPrimaryRed, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) { Text("✓", color = kSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            CTAButton(
                text = "Pay ${formatUgx(totalCost.toDouble())}",
                onClick = { viewModel.pay(onPay) },
                enabled = state.selectedMethod.isNotBlank() && !state.isBusy,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}
