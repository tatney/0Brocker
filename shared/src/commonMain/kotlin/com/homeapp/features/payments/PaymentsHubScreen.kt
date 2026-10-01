package com.homeapp.features.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconCreditCard
import com.homeapp.core.icons.IconDownload
import com.homeapp.core.icons.IconQrScan
import com.homeapp.core.icons.IconSend
import com.homeapp.core.icons.IconWallet
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
import com.homeapp.core.theme.kWalletNavy
import com.homeapp.core.theme.kWalletNavyLight
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.SectionHeader
import com.homeapp.core.widgets.ServiceChip
import com.homeapp.data.model.WalletTransaction

@Composable
fun PaymentsHubScreen(
    modifier: Modifier = Modifier,
    onAddMoney: () -> Unit = {},
    onSendMoney: () -> Unit = {},
    onViewAll: () -> Unit = {},
    viewModel: PaymentsViewModel = viewModel { PaymentsViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val account = state.account
    val transactions = state.transactions

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = kSpaceMD, vertical = kSpaceSM),
        verticalArrangement = Arrangement.spacedBy(kSpaceMD),
    ) {
        item {
            AppCard {
                Column(Modifier.padding(kSpaceLG)) {
                    Text(
                        text = "Available Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = kTextSecondary,
                    )
                    Spacer(Modifier.height(kSpaceXS))
                    Text(
                        text = account?.formatBalance() ?: "UGX 0",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        color = kWalletNavy,
                    )
                    Spacer(Modifier.height(kSpaceMD))
                    Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                        ServiceChip(label = "Send", icon = IconSend, size = 52.dp, onClick = onSendMoney)
                        ServiceChip(label = "Request", icon = IconDownload, size = 52.dp)
                        ServiceChip(label = "Add Money", icon = IconWallet, size = 52.dp, onClick = onAddMoney)
                        ServiceChip(label = "Scan & Pay", icon = IconQrScan, size = 52.dp)
                    }
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .background(
                        Brush.linearGradient(listOf(kWalletNavy, kWalletNavyLight)),
                        kRadiusXL,
                    )
                    .padding(kSpaceLG),
                contentAlignment = Alignment.CenterStart,
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = IconCreditCard,
                            contentDescription = null,
                            tint = kSurface,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(kSpaceSM))
                        Text(
                            text = account?.cardName ?: "0Brocker Platinum Card",
                            style = MaterialTheme.typography.titleMedium,
                            color = kSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(Modifier.height(kSpaceSM))
                    Text(
                        text = buildString {
                            append("••••  ••••  ••••  ")
                            append(account?.cardLast4 ?: "0000")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = kSurface.copy(alpha = 0.85f),
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
        item {
            SectionHeader(title = "Recent transactions", actionLabel = "View all", onAction = onViewAll)
        }
        if (transactions.isNotEmpty()) {
            item {
                AppCard {
                    Column(Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceXS)) {
                        transactions.forEach { tx ->
                            TransactionRow(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: WalletTransaction) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceSM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (tx.isCredit) kTealLight else MaterialTheme.colorScheme.surfaceVariant,
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (tx.isCredit) "\u2193" else "\u2191",
                color = if (tx.isCredit) kAccentTeal else kTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
        Spacer(Modifier.width(kSpaceSM))
        Column(Modifier.weight(1f)) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = tx.meta,
                style = MaterialTheme.typography.labelSmall,
                color = kTextSecondary,
            )
        }
        Text(
            text = tx.formatAmount(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (tx.isCredit) kAccentTeal else kPrimaryRed,
        )
    }
}
