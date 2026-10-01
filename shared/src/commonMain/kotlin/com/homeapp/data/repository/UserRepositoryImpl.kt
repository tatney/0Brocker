package com.homeapp.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.homeapp.data.model.User
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class UserRepositoryImpl(
    private val db: HomeAppDatabase,
) : UserRepository {

    private val queries get() = db.homeAppDatabaseQueries

    override fun observeAll(): Flow<List<User>> =
        queries.selectAllUsers().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun count(): Long =
        queries.userCount().executeAsOne()

    override suspend fun insert(user: User) {
        queries.insertUser(
            id = user.id,
            full_name = user.fullName,
            phone = user.phone,
            email = user.email,
            avatar_emoji = user.avatarEmoji,
            is_professional = if (user.isProfessional) 1L else 0L,
            password_hash = user.passwordHash,
        )
    }

    private fun Users.toDomain() = User(
        id = id,
        fullName = full_name,
        phone = phone,
        email = email,
        avatarEmoji = avatar_emoji,
        isProfessional = is_professional == 1L,
        passwordHash = password_hash,
    )
}
