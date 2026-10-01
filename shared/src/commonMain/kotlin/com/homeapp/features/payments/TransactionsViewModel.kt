package com.homeapp.features.payments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.WalletTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

enum class TransactionFilter(val label: String) {
    ALL("All"),
    CREDITS("Credits"),
    DEBITS("Debits"),
}

class TransactionsViewModel : ViewModel() {

    private val repo = AppContainer.walletRepository

    private val filter = MutableStateFlow(TransactionFilter.ALL)
    val selectedFilter: StateFlow<TransactionFilter> = filter.asStateFlow()

    val transactions: StateFlow<List<WalletTransaction>> =
        repo.observeAllTransactions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectFilter(value: TransactionFilter) = filter.update { value }

    fun applyFilter(all: List<WalletTransaction>): List<WalletTransaction> = when (filter.value) {
        TransactionFilter.ALL -> all
        TransactionFilter.CREDITS -> all.filter { it.isCredit }
        TransactionFilter.DEBITS -> all.filter { !it.isCredit }
    }
}
