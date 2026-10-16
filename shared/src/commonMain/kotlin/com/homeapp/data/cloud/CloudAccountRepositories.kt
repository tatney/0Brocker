package com.homeapp.data.cloud

import com.homeapp.data.AppContainer
import com.homeapp.data.model.*
import com.homeapp.data.repository.*
import kotlinx.coroutines.flow.*

/** Recorded balances are read-only until a payment/reconciliation provider is chosen. */
class CloudWalletRepository: WalletRepository {
    override suspend fun ensurePreviewAccount() = Unit
    override suspend fun recordPreviewTransfer(accountId: Long,amount: Long,isCredit: Boolean,title: String,meta: String) {error("Live payments are deferred. No money was moved.")}
    override fun observePrimaryAccount() = cloudFlow<WalletAccount?>(null) {
        val me=(AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id?:0
        SupabaseGateway.read("wallet_accounts?select=*").rows().firstOrNull()?.let {r->WalletAccount(r.long("id"),me,r.long("balance_minor"),r.string("currency","UGX"),r.string("card_last4"),r.string("card_type"),r.string("card_name"))}
    }
    override suspend fun insertAccount(account: WalletAccount) {error("Payment reconciliation is not connected")}
    override suspend fun updateBalance(accountId: Long,balancePaise: Long) {error("Payment reconciliation is not connected")}
    override fun observeAllTransactions() = cloudFlow(emptyList<WalletTransaction>()) {SupabaseGateway.read("wallet_transactions?select=*&order=created_at.desc").rows().map {r->WalletTransaction(r.long("id"),r.long("account_id"),r.string("title"),r.string("meta"),r.long("amount_minor"),r.bool("is_credit"),r.epoch("created_at"))}}
    override fun observeRecentTransactions(limit: Long) = observeAllTransactions().map {it.take(limit.toInt())}
    override suspend fun insertTransaction(transaction: WalletTransaction) {error("Payment reconciliation is not connected")}
    override suspend fun transactionsCount() = SupabaseGateway.read("wallet_transactions?select=id").rows().size.toLong()
}
class CloudUserRepository: UserRepository {
    override fun observeAll() = AppContainer.authRepository.observeSession().map {auth->listOfNotNull((auth as? AuthState.SignedIn)?.user)}
    override suspend fun count() = observeAll().first().size.toLong()
    override suspend fun insert(user: User) {error("Create accounts through Supabase authentication")}
}
