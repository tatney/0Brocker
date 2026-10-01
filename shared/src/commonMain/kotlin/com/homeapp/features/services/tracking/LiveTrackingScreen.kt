package com.homeapp.features.services.tracking

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import androidx.compose.ui.graphics.vector.ImageVector
import com.homeapp.core.icons.IconChat
import com.homeapp.core.icons.IconPhone
import com.homeapp.core.icons.IconSend
import com.homeapp.core.icons.IconShieldCheck
import com.homeapp.core.model.serviceCategoryOf
import com.homeapp.core.model.icon
import com.homeapp.core.widgets.AppAvatar
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.theme.kTileGrey
import com.homeapp.core.widgets.CTAButton
import kotlinx.coroutines.delay

@Composable
fun LiveTrackingScreen(
    onBack: () -> Unit,
    onServiceStarted: () -> Unit,
    onChat: () -> Unit,
    viewModel: TrackingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var animProgress by remember { mutableFloatStateOf(0f) }
    var currentETA by remember { mutableStateOf(state.etaMinutes) }

    LaunchedEffect(Unit) {
        while (currentETA > 0) {
            delay(2000)
            currentETA = (currentETA - 1).coerceAtLeast(0)
            animProgress = animProgress + 0.05f
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column {
                    Text(
                        text = if (currentETA > 0) "Provider is on the way" else "Provider has arrived!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (currentETA > 0) {
                        Text("ETA: $currentETA minutes", style = MaterialTheme.typography.bodyMedium, color = kTextSecondary)
                    }
                }
            }

            Text("Test tracking: location, movement and arrival times are simulated.", style = MaterialTheme.typography.bodySmall, color = kTextSecondary)
            // Tracking map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE8F0E8)),
            ) {
                TrackingMapCanvas(progress = animProgress, eta = currentETA)
            }

            // Provider info card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(kSurface)
                    .padding(kSpaceMD),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val providerName = state.providerName
                    val serviceCategory = serviceCategoryOf(state.category)
                    val serviceType = state.category.ifBlank { "Service" }
                    AppAvatar(
                        name = providerName,
                        category = serviceCategory,
                        icon = serviceCategory.icon,
                        size = 52.dp,
                    )
                    Spacer(Modifier.width(kSpaceMD))
                    Column(Modifier.weight(1f)) {
                        Text(providerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(serviceType, style = MaterialTheme.typography.bodyMedium, color = kTextSecondary)
                    }
                    if (currentETA > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ETA", style = MaterialTheme.typography.labelSmall, color = kTextSecondary)
                            Text("${currentETA}min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = kPrimaryRed)
                        }
                    }
                }

                Spacer(Modifier.height(kSpaceSM))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    ActionButton("Call", icon = IconPhone, onClick = {})
                    ActionButton("Chat", icon = IconChat, onClick = onChat)
                    ActionButton("Share", icon = IconSend, onClick = {})
                    ActionButton("Help", icon = IconShieldCheck, onClick = {})
                }

                Spacer(Modifier.height(kSpaceSM))

                if (currentETA <= 0) {
                    CTAButton(text = "Confirm Provider Arrived", onClick = onServiceStarted)
                } else {
                    CTAButton(
                        text = "Cancel Booking",
                        onClick = onBack,
                        backgroundColor = kSurface,
                        textColor = kPrimaryRed,
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(kTileGrey, kRadiusMD)
            .padding(horizontal = kSpaceSM, vertical = kSpaceSM),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TrackingMapCanvas(progress: Float, eta: Int) {
    val textMeasurer = rememberTextMeasurer()
    // Text labels rather than emoji: glyph metrics vary per platform, and an
    // emoji measured into a TextLayout lands at an arbitrary offset in Canvas.
    val customerLabel = remember(textMeasurer) { textMeasurer.measure(AnnotatedString("You")) }
    val providerLabel = remember(textMeasurer) { textMeasurer.measure(AnnotatedString("Pro")) }
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Grid
        val gridColor = Color(0xFFD0DDD0)
        for (i in 0..15) {
            drawLine(gridColor, Offset(w * i / 15f, 0f), Offset(w * i / 15f, h), strokeWidth = 0.5f)
            drawLine(gridColor, Offset(0f, h * i / 15f), Offset(w, h * i / 15f), strokeWidth = 0.5f)
        }

        // Route line
        val routePath = Path().apply {
            moveTo(cx * 0.4f, cy * 1.6f)
            quadraticBezierTo(cx, cy * 0.8f, cx * 1.6f, cy * 0.4f)
        }
        drawPath(routePath, kPrimaryRed.copy(alpha = 0.3f), style = Stroke(width = 3.dp.toPx()))

        // Dashed route ahead
        val dashedPath = Path().apply {
            moveTo(cx * 0.4f + (cx * 1.2f) * progress.coerceAtMost(1f), cy * 1.6f - (cy * 1.2f) * progress.coerceAtMost(1f))
            quadraticBezierTo(cx, cy * 0.8f, cx * 1.6f, cy * 0.4f)
        }
        drawPath(dashedPath, kPrimaryRed, style = Stroke(width = 3.dp.toPx()))

        // Customer (destination) marker
        drawCircle(Color.Blue, 10.dp.toPx(), Offset(cx * 0.4f, cy * 1.6f))
        drawCircle(kSurface, 5.dp.toPx(), Offset(cx * 0.4f, cy * 1.6f))
        with(customerLabel) {
            drawText(this, topLeft = Offset(cx * 0.4f - size.width / 2f, cy * 1.6f - 30.dp.toPx()))
        }

        // Provider marker (moves along route)
        val providerX = cx * 0.4f + (cx * 1.2f) * progress.coerceAtMost(1f)
        val providerY = cy * 1.6f - (cy * 1.2f) * progress.coerceAtMost(1f)
        drawCircle(kPrimaryRed, 12.dp.toPx(), Offset(providerX, providerY))
        drawCircle(kSurface, 6.dp.toPx(), Offset(providerX, providerY))
        with(providerLabel) {
            drawText(this, topLeft = Offset(providerX - size.width / 2f, providerY - 26.dp.toPx()))
        }
    }
}
