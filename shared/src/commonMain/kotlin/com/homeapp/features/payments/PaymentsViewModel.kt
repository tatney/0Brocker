package com.homeapp.features.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.WalletAccount
import com.homeapp.data.model.WalletTransaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class PaymentsUiState(
    val account: WalletAccount? = null,
    val transactions: List<WalletTransaction> = emptyList(),
)

class PaymentsViewModel : ViewModel() {

    private val repo = AppContainer.walletRepository

    val uiState: StateFlow<PaymentsUiState> =
        combine(
            repo.observePrimaryAccount(),
            repo.observeRecentTransactions(10),
        ) { account, transactions ->
            PaymentsUiState(account = account, transactions = transactions)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PaymentsUiState())
}
