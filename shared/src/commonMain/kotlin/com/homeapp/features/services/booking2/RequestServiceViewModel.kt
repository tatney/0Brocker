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

    private val requestKey = savedStateHandle.get<String>("requestKey") ?: kotlin.uuid.Uuid.random().toString().also { savedStateHandle["requestKey"] = it }
    private var createdBookingId: Long? = null
    private var uploadedPhotos = 0

    fun pickPhotos() { com.homeapp.data.cloud.ServicePhotoPicker.pick { photos, error ->
        _uiState.update { it.copy(photos = (it.photos + photos).take(5), error = error) }
    } }
    fun removePhoto(index: Int) { if (createdBookingId == null) _uiState.update { it.copy(photos = it.photos.filterIndexed { i, _ -> i != index }) } }

    init {
        val id = providerId
        if (id != null) {
            viewModelScope.launch {
                val p = AppContainer.marketplaceRepository.observeProviderById(id).first()
                _provider.value = p

            }
        }
    }

    fun acceptFee(value: Boolean) = _uiState.update { it.copy(feeAccepted = value) }

    fun updateDescription(value: String) = _uiState.update { it.copy(description = value.take(2000)) }
    fun updateSubType(value: String) = _uiState.update { it.copy(serviceSubType = value) }
    fun updateLocation(value: String) = _uiState.update { it.copy(locationText = value.take(100)) }
    fun updateUrgency(value: String) = _uiState.update { it.copy(urgency = value) }
    fun updateScheduledDate(value: String) = _uiState.update { it.copy(scheduledDate = value) }
    fun updateScheduledTime(value: String) = _uiState.update { it.copy(scheduledTime = value) }

    fun submit(onCreated: (Long) -> Unit) {
        val state = _uiState.value
        if (state.isBusy) return
        if (state.description.trim().length < 5 || state.locationText.isBlank()) {
            _uiState.update { it.copy(error = "Describe the issue and provide a location") }
            return
        }
        val scheduled = runCatching { bookingSchedule(state.urgency, state.scheduledDate, state.scheduledTime, currentTimeEpochSeconds()) }
            .getOrElse { _uiState.update { it.copy(error = "Enter a valid future date (YYYY-MM-DD) and time (HH:mm), Kampala time") }; return }
        if (_provider.value == null) { _uiState.update { it.copy(error = "No eligible technician. Independent qualification is required.") }; return }
        if (!state.feeAccepted) { _uiState.update { it.copy(error = "Acknowledge the diagnostic fee and repair exclusions") }; return }
        val location = com.homeapp.features.services.map.UserLocation.point.value ?: run { _uiState.update { it.copy(error = "Share your location before requesting a technician") }; return }
        _uiState.update { it.copy(isBusy = true, error = null) }
        val provider = _provider.value
        viewModelScope.launch {
            val now = currentTimeEpochSeconds()
            val customerId = (AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id
                ?: run { _uiState.update { it.copy(isBusy = false, error = "Sign in to book a service") }; return@launch }
            val bookingId = createdBookingId ?: runCatching { AppContainer.marketplaceRepository.createBooking(
                Booking(
                    id = 0,
                    customerId = customerId,
                    providerId = providerId ?: 0,
                    providerName = provider?.name ?: "",
                    providerEmoji = provider?.emoji ?: "",
                    serviceType = provider?.category ?: "",
                    workType = state.serviceSubType,
                    requestKey = requestKey,
                    description = state.description,
                    status = BookingStatus.SEARCHING,
                    locationText = state.locationText,
                    lat = location.lat,
                    lng = location.lng,
                    baseCost = provider?.priceFrom ?: 0,
                    platformFee = 0,
                    totalCost = provider?.priceFrom ?: 0,
                    scheduledAt = if (state.urgency == "NOW") 0 else scheduled,
                    createdAtEpoch = now,
                )
            )
            }.getOrElse { failure -> _uiState.update { it.copy(isBusy = false, error = failure.message ?: "Booking blocked") }; return@launch }
            createdBookingId = bookingId
            runCatching {
                for (photo in state.photos.drop(uploadedPhotos)) {
                    AppContainer.marketplaceRepository.attachPhoto(bookingId, photo)
                    uploadedPhotos++
                }
            }.onFailure { failure ->
                _uiState.update { it.copy(isBusy = false, error = "Request #$bookingId saved. " + (failure.message ?: "Photo upload failed") + " Tap again to retry remaining photos.") }; return@launch
            }
            _uiState.update { it.copy(isBusy = false) }
            onCreated(bookingId)
        }
    }
}
