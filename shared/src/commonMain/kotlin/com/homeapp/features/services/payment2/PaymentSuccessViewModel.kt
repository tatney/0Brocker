package com.homeapp.features.services.payment2

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Booking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentSuccessViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    val transactionId: String = "TXN-${bookingId.toString().padStart(6, '0')}"

    private val _booking = MutableStateFlow<Booking?>(null)
    val booking: StateFlow<Booking?> = _booking.asStateFlow()

    init {
        viewModelScope.launch {
            AppContainer.marketplaceRepository.observeBookingById(bookingId).collect { _booking.value = it }
        }
    }
}