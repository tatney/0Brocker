package com.homeapp.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.Booking
import com.homeapp.data.model.User
import com.homeapp.data.model.WalletAccount
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
    val user: User? = null,
    val account: WalletAccount? = null,
    val bookings: List<Booking> = emptyList(),
)

class ProfileViewModel : ViewModel() {

    private val authRepo = AppContainer.authRepository
    private val walletRepo = AppContainer.walletRepository
    private val serviceRepo = AppContainer.marketplaceRepository

    val uiState: StateFlow<ProfileUiState> =
        combine(
            authRepo.observeSession(),
            walletRepo.observePrimaryAccount(),
            serviceRepo.observeAllBookings(),
        ) { auth, account, bookings ->
            val user = (auth as? AuthState.SignedIn)?.user
            ProfileUiState(user = user, account = account, bookings = bookings.filter { it.customerId == user?.id })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())
}
