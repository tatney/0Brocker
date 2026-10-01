package com.homeapp.data

import com.homeapp.data.database.DatabaseProvider
import com.homeapp.data.repository.AuthRepository
import com.homeapp.data.repository.AuthRepositoryImpl
import com.homeapp.data.repository.ChatRepository
import com.homeapp.data.repository.ChatRepositoryImpl
import com.homeapp.data.repository.MarketplaceRepository
import com.homeapp.data.repository.MarketplaceRepositoryImpl
import com.homeapp.data.repository.PropertyRepository
import com.homeapp.data.repository.PropertyRepositoryImpl
import com.homeapp.data.repository.ServiceRepository
import com.homeapp.data.repository.ServiceRepositoryImpl
import com.homeapp.data.repository.UserRepository
import com.homeapp.data.repository.UserRepositoryImpl
import com.homeapp.data.repository.WalletRepository
import com.homeapp.data.repository.WalletRepositoryImpl

object AppContainer {

    private val db get() = DatabaseProvider.database

    val userRepository: UserRepository by lazy { UserRepositoryImpl(db) }
    val authRepository: AuthRepository by lazy { AuthRepositoryImpl(db) }
    val propertyRepository: PropertyRepository by lazy { PropertyRepositoryImpl(db) }
    val serviceRepository: ServiceRepository by lazy { ServiceRepositoryImpl(db) }
    val walletRepository: WalletRepository by lazy { WalletRepositoryImpl(db) }
    val chatRepository: ChatRepository by lazy { ChatRepositoryImpl(db) }
    val marketplaceRepository: MarketplaceRepository by lazy { MarketplaceRepositoryImpl(db) }
}
