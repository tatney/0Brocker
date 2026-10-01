package com.homeapp.features.services.booking2

import com.homeapp.core.time.currentTimeEpochSeconds

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.Booking
import com.homeapp.data.model.BookingStatus
import com.homeapp.data.model.ServiceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RequestServiceViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val providerId: Long? = savedStateHandle.get<Long>("providerId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(RequestUiState())
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    private val _provider = MutableStateFlow<ServiceProvider?>(null)
    val provider: StateFlow<ServiceProvider?> = _provider.asStateFlow()

    init {
        val id = providerId
        if (id != null) {
            viewModelScope.launch {
                val p = AppContainer.marketplaceRepository.observeProviderById(id).first()
                _provider.value = p
                if (p != null && _uiState.value.serviceSubType.isBlank()) {
                    _uiState.update { it.copy(serviceSubType = p.category) }
                }
            }
        }
    }

    fun updateDescription(value: String) = _uiState.update { it.copy(description = value.take(200)) }
    fun updateSubType(value: String) = _uiState.update { it.copy(serviceSubType = value) }
    fun updateLocation(value: String) = _uiState.update { it.copy(locationText = value.take(100)) }
    fun updateUrgency(value: String) = _uiState.update { it.copy(urgency = value) }
    fun updateScheduledDate(value: String) = _uiState.update { it.copy(scheduledDate = value) }
    fun updateScheduledTime(value: String) = _uiState.update { it.copy(scheduledTime = value) }

    fun submit(onCreated: (Long) -> Unit) {
        val state = _uiState.value
        if (state.description.isBlank()) {
            _uiState.update { it.copy(error = "Please describe the issue") }
            return
        }
        _uiState.update { it.copy(isBusy = true, error = null) }
        val provider = _provider.value
        viewModelScope.launch {
            val now = currentTimeEpochSeconds()
            val customerId = (AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id ?: 1
            val bookingId = AppContainer.marketplaceRepository.createBooking(
                Booking(
                    id = 0,
                    customerId = customerId,
                    providerId = providerId ?: 0,
                    providerName = provider?.name ?: "",
                    providerEmoji = provider?.emoji ?: "",
                    serviceType = state.serviceSubType.ifBlank { provider?.category ?: "General" },
                    description = state.description,
                    status = BookingStatus.SEARCHING,
                    locationText = state.locationText,
                    lat = 0.3476,
                    lng = 32.5825,
                    baseCost = 35000,
                    platformFee = 2000,
                    totalCost = 37000,
                    scheduledAt = now,
                    createdAtEpoch = now,
                )
            )
            _uiState.update { it.copy(isBusy = false) }
            onCreated(bookingId)
        }
    }
}
