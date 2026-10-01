package com.homeapp.data.repository

import com.homeapp.data.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeAll(): Flow<List<User>>
    suspend fun count(): Long
    suspend fun insert(user: User)
}
