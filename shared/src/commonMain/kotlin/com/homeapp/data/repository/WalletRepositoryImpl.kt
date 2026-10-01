package com.homeapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.homeapp.data.model.WalletAccount
import com.homeapp.data.model.WalletTransaction
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Wallet_transactions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class WalletRepositoryImpl(
    private val db: HomeAppDatabase,
) : WalletRepository {

    private val queries get() = db.homeAppDatabaseQueries

    override fun observePrimaryAccount(): Flow<WalletAccount?> =
        queries.selectPrimaryAccount().asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.let {
                WalletAccount(
                    id = it.id,
                    userId = it.user_id,
                    balancePaise = it.balance_paise,
                    currency = it.currency,
                    cardLast4 = it.card_last4,
                    cardType = it.card_type,
                    cardName = it.card_name,
                )
            }
        }

    override suspend fun insertAccount(account: WalletAccount) {
        queries.insertAccount(
            id = account.id,
            user_id = account.userId,
            balance_paise = account.balancePaise,
            currency = account.currency,
            card_last4 = account.cardLast4,
            card_type = account.cardType,
            card_name = account.cardName,
        )
    }

    override suspend fun updateBalance(accountId: Long, balancePaise: Long) {
        queries.updateAccountBalance(balancePaise, accountId)
    }

    override suspend fun transactionsCount(): Long =
        queries.transactionCount().executeAsOne()

    override fun observeRecentTransactions(limit: Long): Flow<List<WalletTransaction>> =
        queries.recentTransactions(limit).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeAllTransactions(): Flow<List<WalletTransaction>> =
        queries.selectAllTransactions().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun insertTransaction(transaction: WalletTransaction) {
        queries.insertTransaction(
            id = transaction.id,
            account_id = transaction.accountId,
            title = transaction.title,
            meta = transaction.meta,
            amount_paise = transaction.amountPaise,
            is_credit = if (transaction.isCredit) 1L else 0L,
            created_at_epoch = transaction.createdAtEpoch,
        )
    }

    private fun Wallet_transactions.toDomain() = WalletTransaction(
        id = id,
        accountId = account_id,
        title = title,
        meta = meta,
        amountPaise = amount_paise,
        isCredit = is_credit == 1L,
        createdAtEpoch = created_at_epoch,
    )
}
