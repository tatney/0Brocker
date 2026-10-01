package com.homeapp.features.payments

import com.homeapp.core.time.formatRelativeAge

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTealLight
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.EmptyState
import com.homeapp.data.model.WalletTransaction

@Composable
fun TransactionsScreen(
    onBack: () -> Unit,
    viewModel: TransactionsViewModel = viewModel { TransactionsViewModel() },
) {
    val all by viewModel.transactions.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val transactions = remember(selectedFilter, all) { viewModel.applyFilter(all) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = kSpaceMD, vertical = kSpaceSM),
        verticalArrangement = Arrangement.spacedBy(kSpaceSM),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                TransactionFilter.entries.forEach { filter ->
                    val selected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.selectFilter(filter) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) kSurface else kPrimaryRed,
                        )
                    }
                }
            }
        }
        if (transactions.isEmpty()) {
            item {
                EmptyState(
                    title = "No transactions",
                    subtitle = "Your wallet activity will appear here.",
                )
            }
        } else {
            item {
                AppCard {
                    Column(Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceXS)) {
                        transactions.forEach { tx ->
                            FullTransactionRow(tx)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullTransactionRow(tx: WalletTransaction) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceSM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (tx.isCredit) kTealLight else kPrimaryRedLight, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (tx.isCredit) "\u2193" else "\u2191",
                color = if (tx.isCredit) kAccentTeal else kPrimaryRed,
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
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = tx.formatAmount(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (tx.isCredit) kAccentTeal else kPrimaryRed,
            )
            Text(
                text = formatRelativeAge(tx.createdAtEpoch),
                style = MaterialTheme.typography.labelSmall,
                color = kTextSecondary,
            )
        }
    }
}
