package com.homeapp.features.services.tracking

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.Booking
import com.homeapp.data.model.BookingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ServiceProgressUiState(
    val booking: Booking? = null,
    val isMarkingCompleted: Boolean = false,
) {
    val activeIndex: Int
        get() = when (booking?.status) {
            BookingStatus.INSPECTION -> 0
            BookingStatus.MATERIALS_REQUIRED -> 2
            BookingStatus.COMPLETED -> 4
            else -> 3
        }
}

class ServiceProgressViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    private val _uiState = MutableStateFlow(ServiceProgressUiState())
    val uiState: StateFlow<ServiceProgressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            AppContainer.marketplaceRepository.observeBookingById(bookingId).collect { booking ->
                _uiState.update { it.copy(booking = booking) }
            }
        }
    }

    fun markCompleted(onDone: () -> Unit) {
        _uiState.update { it.copy(isMarkingCompleted = true) }
        viewModelScope.launch {
            AppContainer.marketplaceRepository.updateBookingStatus(bookingId, BookingStatus.COMPLETED)
            _uiState.update { it.copy(isMarkingCompleted = false) }
            onDone()
        }
    }
}