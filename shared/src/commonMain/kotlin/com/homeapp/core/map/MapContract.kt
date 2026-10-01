package com.homeapp.core.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.foundation.Canvas
import com.homeapp.core.icons.IconLocationPin
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kPrimaryRed
import androidx.compose.material3.Icon

/**
 * A lightweight, dependency-free map abstraction.
 *
 * Real map SDKs (Google/Apple maps) are intentionally out of scope for Phase 3;
 * screens depend on [MapContract] so a real provider can be swapped in later
 * without touching feature code. The current [StaticMapPlaceholder] renders a
 * stylised radar + pin so the UI/UX is preserved on all platforms.
 */

/** A point of interest on the map. */
data class MapPoint(
    val label: String,
)

/** Thing a screen needs to render a map. */
data class MapUi(
    val points: List<MapPoint> = emptyList(),
    val centerLabel: String = "",
)

/**
 * Plug-in contract for map rendering. Feature screens call [Draw], never a
 * concrete SDK type, keeping providers swappable (canvas/native/web).
 */
interface MapContract {
    @Composable
    fun Draw(
        ui: MapUi,
        modifier: Modifier,
    )
}

/**
 * Default Canvas-based placeholder implementation: an animated radar sweep with
 * a center pin and a subtle grid. No network or SDK dependency.
 */
object StaticMapPlaceholder : MapContract {
    @Composable
    override fun Draw(
        ui: MapUi,
        modifier: Modifier,
    ) {
        var sweep by remember { mutableFloatStateOf(0f) }
        LaunchedEffect(Unit) {
            while (true) {
                delay(16)
                sweep = (sweep + 1.2f) % 360f
            }
        }
        val animated by animateFloatAsState(
            targetValue = sweep,
            animationSpec = tween(durationMillis = 16),
            label = "sweep",
        )

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFEAF3EF)),
        ) {
            val canvasModifier = Modifier.fillMaxSize()
            Canvas(modifier = canvasModifier) {
                val center = Offset(size.width / 2f, size.height / 2f)

                fun ring(radiusFraction: Float, color: Color, alpha: Float) {
                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = size.minDimension * radiusFraction,
                        center = center,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }
                ring(0.28f, kAccentTeal, 0.22f)
                ring(0.46f, kAccentTeal, 0.16f)
                ring(0.64f, kAccentTeal, 0.10f)
                ring(0.82f, kAccentTeal, 0.06f)

                val angle = animated * (PI / 180.0)
                drawLine(
                    color = kAccentTeal.copy(alpha = 0.45f),
                    start = center,
                    end = Offset(
                        center.x + size.minDimension * 0.8f * cos(angle).toFloat(),
                        center.y + size.minDimension * 0.8f * sin(angle).toFloat(),
                    ),
                    strokeWidth = 3.dp.toPx(),
                )

                drawCircle(
                    color = kAccentTeal.copy(alpha = 0.15f),
                    radius = size.minDimension * 0.1f,
                    center = center,
                )
            }

            Icon(
                imageVector = IconLocationPin,
                contentDescription = ui.centerLabel,
                tint = kPrimaryRed,
                modifier = Modifier.align(Alignment.Center).size(40.dp),
            )

            if (ui.centerLabel.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = ui.centerLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}
