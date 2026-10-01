package com.homeapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Ordered steps of the sign-in / onboarding flow. */
enum class AuthStep { SIGN_IN, SIGN_UP, ONBOARDING }

data class AuthUiState(
    val step: AuthStep = AuthStep.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val avatarEmoji: String = DEFAULT_AVATAR,
    val isBusy: Boolean = false,
    val error: String? = null,
) {
    companion object {
        val AVATAR_CHOICES = listOf("🙂", "🧑", "👩", "🧔", "👩‍🦰", "🧑‍🦱")
        const val DEFAULT_AVATAR = "🙂"
    }
}

/** Drives the sign-in → sign-up → onboarding auth flow. */
class AuthViewModel : ViewModel() {

    private val repo = AppContainer.authRepository

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value.trim(), error = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, error = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                step = if (it.step == AuthStep.SIGN_IN) AuthStep.SIGN_UP else AuthStep.SIGN_IN,
                error = null,
            )
        }
    }

    fun signIn(onDone: () -> Unit) {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        _uiState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            repo.signIn(email, state.password)
                .onSuccess { result ->
                    _uiState.update { it.copy(isBusy = false) }
                    if (result.isOnboarded) onDone() else {
                        _uiState.update { it.copy(step = AuthStep.ONBOARDING) }
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isBusy = false, error = e.message ?: "Unable to sign in") }
                }
        }
    }

    fun signUp(onDone: () -> Unit) {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isBlank() || !email.contains("@")) {
            _uiState.update { it.copy(error = "Please enter a valid email address") }
            return
        }
        if (state.password.length < 6) {
            _uiState.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }
        _uiState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            repo.signUp(email, state.password)
                .onSuccess {
                    _uiState.update { it.copy(isBusy = false, step = AuthStep.ONBOARDING) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isBusy = false, error = e.message ?: "Unable to create account") }
                }
        }
    }

    fun backToSignUp() {
        _uiState.update { it.copy(step = AuthStep.SIGN_UP, error = null, isBusy = false) }
    }

    fun backToSignIn() {
        _uiState.update { it.copy(step = AuthStep.SIGN_IN, error = null, isBusy = false) }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(fullName = value.take(24), error = null) }
    }

    fun selectAvatar(emoji: String) {
        _uiState.update { it.copy(avatarEmoji = emoji) }
    }

    /** Completes onboarding; on success navigates to the main app. */
    fun completeOnboarding(onDone: () -> Unit) {
        val name = _uiState.value.fullName.trim()
        if (name.isEmpty()) {
            _uiState.update { it.copy(error = "Please tell us your name") }
            return
        }
        _uiState.update { it.copy(isBusy = true, error = null) }
        viewModelScope.launch {
            repo.completeOnboarding(_uiState.value.fullName.trim(), _uiState.value.avatarEmoji)
            _uiState.update { it.copy(isBusy = false) }
            onDone()
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}