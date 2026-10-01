package com.homeapp.features.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.theme.PoppinsFontFamily
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusXL
import com.homeapp.core.theme.kSpaceLG
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.data.AppContainer
import com.homeapp.data.model.AuthState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Brand splash screen; checks the persisted session and routes to the main
 * shell (signed-in + onboarded) or the auth flow (otherwise).
 */
@Composable
fun SplashScreen(onFinished: (Boolean) -> Unit) {
    LaunchedEffect(Unit) {
        delay(2200)
        val state = AppContainer.authRepository.observeSession().first()
        val signedIn = state is AuthState.SignedIn && state.isOnboarded
        onFinished(signedIn)
    }
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(kRadiusXL)
                    .background(kPrimaryRed),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "H",
                    fontFamily = PoppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 44.sp,
                    color = kSurface,
                )
            }
            Spacer(Modifier.height(kSpaceLG))
            Text(
                text = "0Brocker",
                fontFamily = PoppinsFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(kSpaceXS))
            Text(
                text = "Everything about your home",
                style = MaterialTheme.typography.bodyMedium,
                color = kTextSecondary,
            )
        }
    }
}