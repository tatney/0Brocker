package com.homeapp.features.services.payment2

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

data class PaymentUiState(
    val selectedMethod: String = "Mobile Money",
    val isBusy: Boolean = false,
)

class ServicePaymentViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()

    init {
        viewModelScope.launch {
            AppContainer.marketplaceRepository.observeBookingById(bookingId).collect { _booking.value = it }
        }
    }

    fun selectMethod(method: String) = _uiState.update { it.copy(selectedMethod = method) }

    fun pay(onDone: () -> Unit) {
        val b = _booking.value ?: return
        if (b.isPaid || _uiState.value.isBusy || b.status != com.homeapp.data.model.BookingStatus.COMPLETED) return
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            delay(1500)
            AppContainer.marketplaceRepository.updateBookingPayment(bookingId, _uiState.value.selectedMethod)
            _uiState.update { it.copy(isBusy = false) }
            onDone()
        }
    }
}