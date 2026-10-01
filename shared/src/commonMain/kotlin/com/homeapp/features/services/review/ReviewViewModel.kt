package com.homeapp.features.services.review

import com.homeapp.core.time.currentTimeEpochSeconds

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val overallRating: Int = 0,
    val categoryRatings: List<Int> = listOf(0, 0, 0, 0, 0),
    val comment: String = "",
    val isBusy: Boolean = false,
    val isSubmitted: Boolean = false,
)

class ReviewViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0L

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private val _providerId = MutableStateFlow(0L)
    val providerId: StateFlow<Long> = _providerId.asStateFlow()

    init {
        viewModelScope.launch {
            AppContainer.marketplaceRepository.observeBookingById(bookingId).collect { booking ->
                if (booking != null) _providerId.value = booking.providerId
            }
        }
    }

    fun setOverall(rating: Int) = _uiState.update { it.copy(overallRating = rating) }

    fun setCategoryRating(index: Int, rating: Int) = _uiState.update { state ->
        state.copy(categoryRatings = state.categoryRatings.toMutableList().apply {
            if (index in indices) set(index, rating)
        })
    }

    fun updateComment(text: String) = _uiState.update { it.copy(comment = text.take(500)) }

    fun submit(onDone: () -> Unit) {
        val state = _uiState.value
        if (state.overallRating == 0 || state.isBusy || state.isSubmitted) return
        _uiState.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val pid = _providerId.value
            val customerId = (AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id
                ?: run { _uiState.update { it.copy(isBusy = false) }; return@launch }
            val booking = AppContainer.marketplaceRepository.observeBookingById(bookingId).first()
            if (booking == null || booking.customerId != customerId || !booking.isPaid || booking.status == com.homeapp.data.model.BookingStatus.REVIEWED) {
                _uiState.update { it.copy(isBusy = false) }; return@launch
            }
            AppContainer.marketplaceRepository.insertReview(
                Review(
                    id = kotlin.random.Random.nextLong(1_000_000, 9_999_999),
                    bookingId = bookingId,
                    providerId = pid,
                    customerId = customerId,
                    quality = state.categoryRatings.getOrElse(0) { 0 }.takeIf { it > 0 } ?: state.overallRating,
                    professionalism = state.categoryRatings.getOrElse(1) { 0 }.takeIf { it > 0 } ?: state.overallRating,
                    timeliness = state.categoryRatings.getOrElse(2) { 0 }.takeIf { it > 0 } ?: state.overallRating,
                    communication = state.categoryRatings.getOrElse(3) { 0 }.takeIf { it > 0 } ?: state.overallRating,
                    valueRating = state.categoryRatings.getOrElse(4) { 0 }.takeIf { it > 0 } ?: state.overallRating,
                    overall = state.overallRating,
                    comment = state.comment,
                    createdAtEpoch = currentTimeEpochSeconds(),
                )
            )
            AppContainer.marketplaceRepository.updateBookingStatus(bookingId, com.homeapp.data.model.BookingStatus.REVIEWED)
            _uiState.update { it.copy(isBusy = false, isSubmitted = true) }
            onDone()
        }
    }
}
