package com.homeapp.features.payments

import com.homeapp.core.time.currentTimeEpochSeconds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.WalletAccount
import com.homeapp.data.model.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val quickAmountsPaise: List<Long> = listOf(50000L, 100000L, 200000L, 500000L, 1000000L)

data class AddMoneyUiState(
    val amountPaise: Long = 0L,
    val isBusy: Boolean = false,
    val error: String? = null,
    val isDone: Boolean = false,
) {
    val canSubmit: Boolean
        get() = amountPaise > 0 && !isBusy
}

class AddMoneyViewModel : ViewModel() {

    private val repo = AppContainer.walletRepository

    val account: StateFlow<WalletAccount?> =
        repo.observePrimaryAccount()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _ui = MutableStateFlow(AddMoneyUiState())
    val ui: StateFlow<AddMoneyUiState> = _ui.asStateFlow()

    fun selectAmount(paise: Long) = _ui.update { it.copy(amountPaise = paise, error = null) }
    fun onAmountChange(rupeesText: String) {
        val rupees = rupeesText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        _ui.update { it.copy(amountPaise = rupees * 100L, error = null) }
    }

    fun addMoney() {
        val state = _ui.value
        if (state.amountPaise <= 0) {
            _ui.update { it.copy(error = "Enter an amount") }
            return
        }
        val current = account.value ?: return
        _ui.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            val txId = repo.transactionsCount() + 1
            val now = currentTimeEpochSeconds()
            repo.insertTransaction(
                WalletTransaction(
                    id = txId,
                    accountId = current.id,
                    title = "Added to 0Brocker Wallet",
                    meta = "UPI · Balance top-up",
                    amountPaise = state.amountPaise,
                    isCredit = true,
                    createdAtEpoch = now,
                ),
            )
            repo.updateBalance(current.id, current.balancePaise + state.amountPaise)
            _ui.update { it.copy(isBusy = false, isDone = true) }
        }
    }

    fun dismissError() = _ui.update { it.copy(error = null) }
}
