package com.homeapp.features.services

import com.homeapp.core.time.currentTimeEpochSeconds

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.LegacyBooking
import com.homeapp.data.model.ServiceProfessional
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookingUiState(
    val professional: ServiceProfessional? = null,
    val selectedDate: String = "",
    val selectedTime: String = "",
    val address: String = "",
    val isBusy: Boolean = false,
    val error: String? = null,
    val isConfirmed: Boolean = false,
) {
    val canSubmit: Boolean
        get() = professional != null && selectedDate.isNotBlank() &&
            selectedTime.isNotBlank() && address.isNotBlank() && !isBusy
}

class BookingViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val serviceId: Long = savedStateHandle.get<Long>("serviceId") ?: 0L

    private val _ui = MutableStateFlow(BookingUiState())
    val ui: StateFlow<BookingUiState> = _ui.asStateFlow()

    val professional: StateFlow<ServiceProfessional?> =
        AppContainer.serviceRepository.observeProfessionalById(serviceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDate(date: String) = _ui.update { it.copy(selectedDate = date, error = null) }
    fun selectTime(time: String) = _ui.update { it.copy(selectedTime = time, error = null) }
    fun onAddressChange(address: String) = _ui.update { it.copy(address = address.take(80), error = null) }

    fun confirmBooking(onDone: () -> Unit) {
        val state = _ui.value
        val p = professional.value ?: return
        if (state.selectedDate.isBlank() || state.selectedTime.isBlank() || state.address.isBlank()) {
            _ui.update { it.copy(error = "Fill in all fields") }
            return
        }
        _ui.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val bookingId = AppContainer.serviceRepository.bookingsCount() + 1
            val booking = LegacyBooking(
                id = bookingId,
                userId = 1,
                professionalId = p.id,
                serviceName = p.category,
                status = "CONFIRMED",
                amountPaise = p.derivedPriceUgx(),
                bookedAtEpoch = currentTimeEpochSeconds(),
            )
            AppContainer.serviceRepository.insertBooking(booking)
            _ui.update { it.copy(isBusy = false, isConfirmed = true) }
            onDone()
        }
    }

    fun dismissError() = _ui.update { it.copy(error = null) }
}
