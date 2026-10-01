package com.homeapp.features.payments

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.theme.kWalletNavy
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.CTAButton
import com.homeapp.data.model.formatUgx
import com.homeapp.data.model.toFormattedString

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SendMoneyScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: SendMoneyViewModel = viewModel { SendMoneyViewModel() },
) {
    val state by viewModel.ui.collectAsState()
    val account by viewModel.account.collectAsState()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        if (state.isDone) {
            SendSuccess(onDone)
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
                    text = "Send Money",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(Modifier.height(kSpaceSM))
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(kSpaceLG)) {
                    Text(
                        text = "Available balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = kTextSecondary,
                    )
                    Spacer(Modifier.height(kSpaceXS))
                    Text(
                        text = account?.formatBalance() ?: "UGX 0",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = kWalletNavy,
                    )
                }
            }

            Spacer(Modifier.height(kSpaceLG))
            Text(
                text = "To (UPI ID or phone)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            OutlinedTextField(
                value = state.recipient,
                onValueChange = viewModel::onRecipientChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. 9876543210@upi", color = kTextSecondary) },
                shape = kRadiusMD,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = kPrimaryRed,
                    cursorColor = kPrimaryRed,
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(kSpaceLG))
            Text(
                text = "Amount (UGX)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            OutlinedTextField(
                value = if (state.amountPaise > 0) (state.amountPaise / 100).toString() else "",
                onValueChange = viewModel::onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Enter amount", color = kTextSecondary) },
                shape = kRadiusMD,
                prefix = { Text("UGX ", fontWeight = FontWeight.Bold, color = kPrimaryRed) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = kPrimaryRed,
                    cursorColor = kPrimaryRed,
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(kSpaceSM))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
                quickAmountsPaise.forEach { paise ->
                    val selected = paise == state.amountPaise
                    Box(
                        modifier = Modifier
                            .clip(kRadiusMD)
                            .background(if (selected) kPrimaryRed else kPrimaryRedLight)
                            .clickable { viewModel.onAmountChange((paise / 100).toString()) }
                            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                    ) {
                        Text(
                            text = "UGX ${(paise / 100).toFormattedString()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (selected) kSurface else kPrimaryRed,
                        )
                    }
                }
            }

            state.error?.let { msg ->
                Spacer(Modifier.height(kSpaceSM))
                Text(text = msg, color = kPrimaryRed, style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.weight(1f))
            CTAButton(
                text = "Send ${formatUgx(state.amountPaise / 100.0)}",
                onClick = { viewModel.send() },
                enabled = state.canSubmit,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}

@Composable
private fun SendSuccess(onDone: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(kSpaceMD),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "✅", fontSize = 64.sp)
            Spacer(Modifier.height(kSpaceMD))
            Text(
                text = "Money sent!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(kSpaceSM))
            Text(
                text = "The amount has been debited from your 0Brocker wallet.",
                style = MaterialTheme.typography.bodyLarge,
                color = kTextSecondary,
            )
            Spacer(Modifier.height(kSpaceLG))
            CTAButton(text = "Back to wallet", onClick = onDone)
        }
    }
}
