package com.homeapp.features.auth

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.theme.PoppinsFontFamily
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kPrimaryRedLight
import com.homeapp.core.theme.kRadiusLG
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXL
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.CTAButton

/**
 * Root of the sign-in / sign-up + onboarding flow. Renders the current auth
 * step (sign-in, sign-up, profile) based on the view model, then hands off to
 * [onDone].
 */
@Composable
fun AuthFlow(onDone: () -> Unit) {
    val viewModel: AuthViewModel = viewModel { AuthViewModel() }
    val state = viewModel.uiState.collectAsState()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            state.value.error?.let { message ->
                AuthErrorBar(message = message, onDismiss = viewModel::dismissError)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = kSpaceMD),
        ) {
            when (state.value.step) {
                AuthStep.SIGN_IN -> SignInStep(state.value, viewModel, onDone)
                AuthStep.SIGN_UP -> {
                    @OptIn(ExperimentalComposeUiApi::class)
                    BackHandler(onBack = viewModel::backToSignIn)
                    SignUpStep(state.value, viewModel, onDone)
                }
                AuthStep.ONBOARDING -> {
                    @OptIn(ExperimentalComposeUiApi::class)
                    BackHandler(onBack = viewModel::backToSignUp)
                    OnboardingStep(state.value, viewModel) {
                        viewModel.completeOnboarding(onDone)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthTitle(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            fontFamily = PoppinsFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(kSpaceXS))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = kTextSecondary,
        )
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation =
        androidx.compose.ui.text.input.VisualTransformation.None,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder, color = kTextSecondary) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        shape = kRadiusMD,
        visualTransformation = visualTransformation,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = kPrimaryRed,
            focusedLeadingIconColor = kPrimaryRed,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = kPrimaryRed,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
private fun SignInStep(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(kSpaceLG),
    ) {
        AuthTitle(
            title = "Welcome to 0Brocker",
            subtitle = "Sign in with your email and password.",
        )
        Spacer(Modifier.height(kSpaceSM))
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(kRadiusLG)
                .background(kPrimaryRedLight),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "0B",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = kPrimaryRed,
            )
        }
        AuthField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Email address",
            keyboardType = KeyboardType.Email,
            enabled = !state.isBusy,
        )
        AuthField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Password",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.isBusy,
        )
        Text(
            text = "debug internal state -> email:${state.email.length} pwd:${state.password.length}",
            style = MaterialTheme.typography.labelSmall,
            color = kTextSecondary,
        )
        Spacer(Modifier.height(kSpaceSM))
        CTAButton(
            text = "Sign in",
            onClick = { viewModel.signIn(onDone) },
            enabled = state.email.isNotBlank() && state.password.length >= 6 && !state.isBusy,
        )
        DemoHintBanner()
        ToggleLink(
            prompt = "New here? ",
            action = "Create account",
            onClick = viewModel::toggleMode,
        )
    }
}

@Composable
private fun SignUpStep(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(kSpaceLG),
    ) {
        AuthTitle(
            title = "Create your account",
            subtitle = "Choose a strong password to keep your account safe.",
        )
        AuthField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Email address",
            keyboardType = KeyboardType.Email,
            enabled = !state.isBusy,
        )
        AuthField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "Password (min 6 characters)",
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation(),
            enabled = !state.isBusy,
        )
        Spacer(Modifier.height(kSpaceSM))
        CTAButton(
            text = "Create account",
            onClick = { viewModel.signUp(onDone) },
            enabled = state.email.isNotBlank() && state.password.length >= 6 && !state.isBusy,
        )
        DemoHintBanner()
        ToggleLink(
            prompt = "Already have an account? ",
            action = "Sign in",
            onClick = viewModel::toggleMode,
        )
    }
}

@Composable
private fun DemoHintBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(kRadiusLG)
            .background(kPrimaryRedLight)
            .padding(kSpaceSM),
    ) {
        Text(
            text = "V1 test account: tester@0brocker.app / HomeTest!2026",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = kPrimaryRed,
        )
    }
}

@Composable
private fun ToggleLink(prompt: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = kTextSecondary,
        )
        Text(
            text = action,
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(kSpaceXS),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = kPrimaryRed,
        )
    }
}

@Composable
private fun OnboardingStep(
    state: AuthUiState,
    viewModel: AuthViewModel,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = kSpaceXL),
        verticalArrangement = Arrangement.spacedBy(kSpaceLG),
    ) {
        AuthTitle(
            title = "Almost there",
            subtitle = "Tell us a little about yourself so we can personalise 0Brocker.",
        )
        AuthField(
            value = state.fullName,
            onValueChange = viewModel::onNameChange,
            placeholder = "Your full name",
            keyboardType = KeyboardType.Text,
            enabled = !state.isBusy,
        )

        Text(
            text = "Pick an avatar",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(kSpaceSM)) {
            AuthUiState.AVATAR_CHOICES.forEach { emoji ->
                val selected = emoji == state.avatarEmoji
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(if (selected) kPrimaryRedLight else MaterialTheme.colorScheme.surface)
                        .alpha(if (selected) 1f else 0.55f)
                        .clickable(enabled = !state.isBusy) { viewModel.selectAvatar(emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 24.sp)
                }
            }
        }
        Spacer(Modifier.height(kSpaceSM))
        CTAButton(
            text = "Continue",
            onClick = onDone,
            enabled = state.fullName.isNotBlank() && !state.isBusy,
        )
    }
}

@Composable
private fun AuthErrorBar(message: String, onDismiss: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(kPrimaryRed, kRadiusLG)
            .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = kSurface,
            )
            IconButton(onClick = onDismiss) {
                Text("\u2715", color = kSurface, fontWeight = FontWeight.Bold)
            }
        }
    }
}