package com.homeapp.data.repository

import com.homeapp.data.model.AuthState
import com.homeapp.data.model.User
import kotlinx.coroutines.flow.Flow

/** Outcome of a successful sign-in; tells the UI whether onboarding still needs to run. */
data class SignInResult(
    val user: User,
    val isOnboarded: Boolean,
)

interface AuthRepository {
    /** Emits the current auth state reactively. */
    fun observeSession(): Flow<AuthState>

    /**
     * Signs [email] in with [password]. Returns the signed-in user on success,
     * or a failure (invalid credentials) when the email or password is wrong.
     */
    suspend fun signIn(email: String, password: String): Result<SignInResult>

    /**
     * Creates a new account for [email] with [password] and signs the user in.
     * The new user still needs to complete onboarding before entering the app.
     */
    suspend fun signUp(email: String, password: String): Result<Unit>

    /** Persists the finished onboarding details (name + avatar) for the session user. */
    suspend fun completeOnboarding(fullName: String, avatarEmoji: String)

    /** Clears the active session (log out). */
    suspend fun signOut()
}