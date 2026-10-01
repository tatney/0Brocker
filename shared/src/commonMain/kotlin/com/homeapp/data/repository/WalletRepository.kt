package com.homeapp.data.repository

import com.homeapp.data.model.WalletAccount
import com.homeapp.data.model.WalletTransaction
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observePrimaryAccount(): Flow<WalletAccount?>
    suspend fun insertAccount(account: WalletAccount)
    suspend fun updateBalance(accountId: Long, balancePaise: Long)
    fun observeRecentTransactions(limit: Long): Flow<List<WalletTransaction>>
    fun observeAllTransactions(): Flow<List<WalletTransaction>>
    suspend fun insertTransaction(transaction: WalletTransaction)
    suspend fun transactionsCount(): Long
}
