package com.homeapp.features.services.booking2

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Booking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmUiState(
    val paymentMethod: String = "Mobile Money",
    val isBusy: Boolean = false,
)

class ConfirmBookingViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    private val _uiState = MutableStateFlow(ConfirmUiState())
    val uiState: StateFlow<ConfirmUiState> = _uiState.asStateFlow()

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()

    init {
        viewModelScope.launch {
            AppContainer.marketplaceRepository.observeBookingById(bookingId).collect { _booking.value = it }
        }
    }

    fun updatePayment(method: String) = _uiState.update { it.copy(paymentMethod = method) }

    fun confirm(onConfirmed: () -> Unit) {
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            delay(1000)
            AppContainer.marketplaceRepository.updateBookingPayment(bookingId, _uiState.value.paymentMethod)
            _uiState.update { it.copy(isBusy = false) }
            onConfirmed()
        }
    }
}