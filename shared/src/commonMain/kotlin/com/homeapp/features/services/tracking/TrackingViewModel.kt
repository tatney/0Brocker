package com.homeapp.features.services.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackingUiState(
    val providerName: String = "Provider",
    val category: String = "",
    val rating: Double = 4.9,
    val etaMinutes: Int = 12,
    val status: String = "EN_ROUTE",
    val customerLat: Double = 0.3476,
    val customerLng: Double = 32.5825,
    val providerLat: Double = 0.3490,
    val providerLng: Double = 32.5840,
)

class TrackingViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            AppContainer.marketplaceRepository.observeBookingById(bookingId).collect { booking ->
                if (booking != null) {
                    _uiState.update {
                        it.copy(
                            providerName = booking.providerName.ifBlank { "Provider" },
                            category = booking.serviceType,
                        )
                    }
                }
            }
        }
    }
}