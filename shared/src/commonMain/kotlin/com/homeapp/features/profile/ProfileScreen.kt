package com.homeapp.features.profile

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconCalendar
import com.homeapp.core.icons.IconPhone
import com.homeapp.core.icons.IconWallet
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.theme.kWalletNavy
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.OutlineButton
import com.homeapp.core.widgets.SectionHeader
import com.homeapp.data.model.Booking

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = viewModel { ProfileViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val user = state.user

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = kSpaceMD, vertical = kSpaceSM),
        verticalArrangement = Arrangement.spacedBy(kSpaceMD),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(kPrimaryRedLight, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = user?.avatarEmoji ?: "🙂", fontSize = 30.sp)
                }
                Spacer(Modifier.width(kSpaceMD))
                Column {
                    Text(
                        text = user?.fullName ?: "Guest",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            IconPhone,
                            contentDescription = null,
                            tint = kTextSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(kSpaceXS))
                        Text(
                            text = user?.email ?: user?.phone ?: "-",
                            style = MaterialTheme.typography.bodyMedium,
                            color = kTextSecondary,
                        )
                    }
                }
            }
        }

        item {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(kSpaceLG)) {
                    Text(
                        text = "Wallet balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = kTextSecondary,
                    )
                    Spacer(Modifier.height(kSpaceXS))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            IconWallet,
                            contentDescription = null,
                            tint = kAccentTeal,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.width(kSpaceSM))
                        Text(
                            text = state.account?.formatBalance() ?: "UGX 0",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = kWalletNavy,
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(title = "My bookings", actionLabel = null, onAction = null)
        }
        if (state.bookings.isEmpty()) {
            item {
                Text(
                    text = "No bookings yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = kTextSecondary,
                )
            }
        } else {
            item {
                AppCard {
                    Column(Modifier.padding(horizontal = kSpaceMD, vertical = kSpaceXS)) {
                        state.bookings.forEach { booking ->
                            BookingRow(booking)
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(kSpaceSM))
            OutlineButton(
                text = "Sign out",
                onClick = onSignOut,
            )
            Spacer(Modifier.height(kSpaceMD))
        }
    }
}

@Composable
private fun BookingRow(booking: Booking) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = kSpaceSM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            IconCalendar,
            contentDescription = null,
            tint = kPrimaryRed,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(kSpaceSM))
        Column(Modifier.weight(1f)) {
            Text(
                text = booking.serviceType,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Booking #${booking.id}",
                style = MaterialTheme.typography.labelSmall,
                color = kTextSecondary,
            )
        }
        Text(
            text = booking.status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = kPrimaryRed,
        )
    }
}
