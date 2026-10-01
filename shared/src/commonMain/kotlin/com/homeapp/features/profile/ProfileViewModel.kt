package com.homeapp.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.LegacyBooking
import com.homeapp.data.model.User
import com.homeapp.data.model.WalletAccount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val user: User? = null,
    val account: WalletAccount? = null,
    val bookings: List<LegacyBooking> = emptyList(),
)

class ProfileViewModel : ViewModel() {

    private val authRepo = AppContainer.authRepository
    private val walletRepo = AppContainer.walletRepository
    private val serviceRepo = AppContainer.serviceRepository

    val uiState: StateFlow<ProfileUiState> =
        combine(
            authRepo.observeSession(),
            walletRepo.observePrimaryAccount(),
            serviceRepo.observeAllBookings(),
        ) { auth, account, bookings ->
            val user = (auth as? AuthState.SignedIn)?.user
            ProfileUiState(user = user, account = account, bookings = bookings)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}
