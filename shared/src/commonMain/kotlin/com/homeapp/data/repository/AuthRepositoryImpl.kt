package com.homeapp.data.repository

import com.homeapp.core.time.currentTimeEpochSeconds

import com.homeapp.data.model.AuthState
import com.homeapp.data.model.User
import com.homeapp.data.security.PasswordHasher
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Users
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class AuthRepositoryImpl(
    private val db: HomeAppDatabase,
) : AuthRepository {

    private val queries get() = db.homeAppDatabaseQueries

    private val _state = MutableStateFlow(loadInitialState())

    override fun observeSession(): Flow<AuthState> = _state.asStateFlow()

    override suspend fun signIn(email: String, password: String): Result<SignInResult> {
        ensureDemoAccountPassword()
        val normalized = normalize(email)
        val user = queries.selectUserByEmail(normalized).executeAsOneOrNull()?.toDomain()
            ?: return Result.failure(invalidCredentials())

        // All secrets are stored hashed; existing demo account gets its hash
        // filled in during the upgrade path, so compare against the stored hash.
        val expectedHash = PasswordHasher.hash(password, user.email ?: normalized)
        if (user.passwordHash.isNotBlank() && expectedHash != user.passwordHash) {
            return Result.failure(invalidCredentials())
        }

        val onboarded = user.fullName.isNotBlank()
        upsertSessionFor(user, isOnboarded = onboarded)
        _state.value = AuthState.SignedIn(user = user, isOnboarded = onboarded)
        return Result.success(SignInResult(user = user, isOnboarded = onboarded))
    }

    override suspend fun signUp(email: String, password: String): Result<Unit> {
        val normalized = normalize(email)
        if (queries.selectUserByEmail(normalized).executeAsOneOrNull() != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists"))
        }
        val user = createNewUser(normalized, password)
        upsertSessionFor(user, isOnboarded = false)
        _state.value = AuthState.SignedIn(user = user, isOnboarded = false)
        return Result.success(Unit)
    }

    override suspend fun completeOnboarding(fullName: String, avatarEmoji: String) {
        val session = queries.selectActiveSession().executeAsOneOrNull() ?: return
        val email = session.email ?: return
        val user = queries.selectUserByEmail(email).executeAsOneOrNull()?.toDomain() ?: return
        queries.updateUserProfile(full_name = fullName, avatar_emoji = avatarEmoji, id = user.id)
        queries.upsertSession(
            id = 1,
            user_id = user.id,
            phone = user.phone,
            email = user.email,
            otp_code = "",
            otp_expires_epoch = session.otp_expires_epoch,
            is_onboarded = 1,
        )
        _state.value = AuthState.SignedIn(
            user = user.copy(fullName = fullName, avatarEmoji = avatarEmoji),
            isOnboarded = true,
        )
    }

    override suspend fun signOut() {
        queries.clearSessions()
        _state.value = AuthState.SignedOut
    }

    private fun loadInitialState(): AuthState {
        val session = queries.selectActiveSession().executeAsOneOrNull() ?: return AuthState.SignedOut
        val email = session.email ?: return AuthState.SignedOut
        val user = queries.selectUserByEmail(email).executeAsOneOrNull()?.toDomain()
            ?: return AuthState.SignedOut
        return AuthState.SignedIn(user = user, isOnboarded = session.is_onboarded == 1L)
    }

    private fun upsertSessionFor(user: User, isOnboarded: Boolean) {
        queries.clearSessions()
        queries.upsertSession(
            id = 1,
            user_id = user.id,
            phone = user.phone,
            email = user.email,
            otp_code = "",
            otp_expires_epoch = currentTimeEpochSeconds(),
            is_onboarded = if (isOnboarded) 1L else 0L,
        )
    }

    private fun createNewUser(email: String, password: String): User {
        val id = queries.userCount().executeAsOne() + 1
        val user = User(
            id = id,
            fullName = "",
            phone = "",
            email = email,
            avatarEmoji = DEFAULT_AVATAR,
            isProfessional = false,
        )
        queries.insertUser(
            id = user.id,
            full_name = user.fullName,
            phone = user.phone,
            email = user.email,
            avatar_emoji = user.avatarEmoji,
            is_professional = 0,
            password_hash = PasswordHasher.hash(password, email),
        )
        return user
    }

    /**
     * Databases seeded before the email/password migration stored the demo
     * account with an empty password hash. Backfill the known demo password so
     * existing installs keep working.
     */
    private fun ensureDemoAccountPassword() {
        val demo = queries.selectUserByEmail(DEMO_EMAIL).executeAsOneOrNull() ?: return
        if (demo.password_hash.isBlank()) {
            queries.updateUserPassword(
                password_hash = PasswordHasher.hash(DEMO_PASSWORD, DEMO_EMAIL),
                id = demo.id,
            )
        }
    }

    private fun normalize(email: String): String = email.trim().lowercase()

    private fun invalidCredentials() = IllegalArgumentException("Invalid email or password")

    private fun Users.toDomain() = User(
        id = id,
        fullName = full_name,
        phone = phone,
        email = email,
        avatarEmoji = avatar_emoji,
        isProfessional = is_professional == 1L,
        passwordHash = password_hash,
    )

    private companion object {
        const val DEMO_EMAIL = "arjun@homeapp.in"
        const val DEMO_PASSWORD = "password123"
        const val DEFAULT_AVATAR = "🙂"
    }
}
