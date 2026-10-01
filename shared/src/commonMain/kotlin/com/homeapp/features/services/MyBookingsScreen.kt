package com.homeapp.features.services

import com.homeapp.core.time.formatRelativeAge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.data.AppContainer
import com.homeapp.data.model.LegacyBooking
import com.homeapp.core.icons.IconCheck
import com.homeapp.core.icons.IconClock
import com.homeapp.core.icons.IconClose
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kStatusSuccess
import com.homeapp.core.theme.kStatusWarning
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.theme.AppSpacing
import com.homeapp.core.widgets.AppCard
import com.homeapp.core.widgets.AppIconTile
import com.homeapp.core.widgets.AppTopBar
import com.homeapp.core.widgets.EmptyState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MyBookingsViewModel : ViewModel() {
    val bookings: StateFlow<List<LegacyBooking>> =
        AppContainer.serviceRepository.observeAllBookings()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun MyBookingsScreen(
    onBack: () -> Unit = {},
    viewModel: MyBookingsViewModel = viewModel { MyBookingsViewModel() },
) {
    val bookings by viewModel.bookings.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.screenEnd),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.item),
    ) {
        item {
            // This screen used to be a tab, so it had no way back. It is a
            // pushed route now, so it owns its own top bar.
            AppTopBar(
                title = "My Bookings",
                subtitle = "${bookings.size} booking${if (bookings.size == 1) "" else "s"}",
                onBack = onBack,
            )
        }
        if (bookings.isEmpty()) {
            item {
                EmptyState(
                    title = "No bookings yet",
                    subtitle = "Book a home service and it will show up here.",
                )
            }
        } else {
            items(bookings, key = { it.id }) { booking ->
                BookingRow(booking)
            }
        }
    }
}

@Composable
private fun BookingRow(booking: LegacyBooking) {
    val status = bookingStatusStyle(booking.status)

    AppCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(kSpaceMD),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconTile(
                icon = status.icon,
                tint = status.accent,
                container = status.accent.copy(alpha = 0.14f),
                size = 40.dp,
                shape = CircleShape,
            )
            Spacer(Modifier.width(kSpaceSM))
            Column(Modifier.weight(1f)) {
                Text(
                    text = booking.serviceName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = status.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(kSpaceXS))
                    Text(
                        text = "Booking #${booking.id}",
                        style = MaterialTheme.typography.labelSmall,
                        color = kTextSecondary,
                    )
                }
            }
            Text(
                text = formatRelativeAge(booking.bookedAtEpoch),
                style = MaterialTheme.typography.labelSmall,
                color = kTextSecondary,
            )
        }
    }
}

/**
 * Icon/label/accent for a legacy booking status.
 *
 * This replaces a per-status emoji circle, where "completed" and "confirmed"
 * differed only by an unlabelled glyph and a near-identical pale background.
 */
private data class BookingStatusStyle(
    val icon: ImageVector,
    val label: String,
    val accent: Color,
)

private fun bookingStatusStyle(status: String): BookingStatusStyle = when (status.uppercase()) {
    "CONFIRMED" -> BookingStatusStyle(IconClock, "Confirmed", kStatusSuccess)
    "COMPLETED" -> BookingStatusStyle(IconCheck, "Completed", kStatusSuccess)
    "CANCELLED" -> BookingStatusStyle(IconClose, "Cancelled", kPrimaryRed)
    "PENDING" -> BookingStatusStyle(IconClock, "Pending", kStatusWarning)
    else -> BookingStatusStyle(IconClock, status.ifBlank { "Unknown" }, kTextSecondary)
}
