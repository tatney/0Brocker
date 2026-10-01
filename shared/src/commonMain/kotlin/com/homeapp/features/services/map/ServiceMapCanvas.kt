package com.homeapp.features.services.map

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeapp.core.icons.IconFitToScreen
import com.homeapp.core.icons.IconMinus
import com.homeapp.core.icons.IconPlus
import com.homeapp.core.icons.IconStar
import com.homeapp.core.model.colors
import com.homeapp.core.model.icon
import com.homeapp.core.model.serviceCategoryOf
import com.homeapp.core.theme.kAccentGold
import com.homeapp.core.theme.kAccentTeal
import com.homeapp.core.theme.kDivider
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kSurface
import com.homeapp.data.model.ServiceProvider
import kotlin.math.log2
import kotlin.math.roundToInt

/**
 * The explore header is an overlay on top of the map rather than a sibling of it,
 * so the map has to treat this much of its own height as hidden. Providers fitted
 * into the full panel would end up centred behind the search bar.
 */
private val kHeaderOcclusion = 196.dp

/**
 * Provider map: OSM raster tiles with the provider pins projected through the
 * same Web Mercator camera.
 *
 * What changed and why:
 *  - the backdrop is real cartography. It used to be a hand-drawn grid with a
 *    radar sweep, which looked like a map but had no relationship to geography —
 *    a provider could sit in the middle of a lake;
 *  - [TileMath] and [TileLayer] own the projection and tile loading, so pins and
 *    tiles cannot drift apart the way two independent projections would;
 *  - pan, pinch-zoom and double-tap zoom work, and ± buttons plus a recentre
 *    control were added for pointer-only devices;
 *  - fitting accounts for the header overlay via [MapCamera.insetTopPx];
 *  - the offline fallback is a static wash rather than the animated sweep, so a
 *    failed tile load costs no frames.
 */
@Composable
fun ServiceMapCanvas(
    providers: List<ServiceProvider>,
    selectedProvider: ServiceProvider?,
    onProviderMarkerClick: (ServiceProvider) -> Unit,
    modifier: Modifier = Modifier,
    userLocation: GeoPoint? = null,
) {
    val density = LocalDensity.current
    val points = remember(providers) { providers.map { GeoPoint(it.lat, it.lng) } }

    // The user is framed along with the providers, otherwise someone standing
    // away from every provider sees a map with no reference for where they are.
    val fitPoints = remember(points, userLocation) {
        if (userLocation != null) points + userLocation else points
    }

    // Only the set of coordinates should trigger a refit. Keying on the whole
    // provider list would reset the user's pan and zoom every time a provider's
    // availability or rating changed underneath them.
    val fitKey = remember(fitPoints) {
        fitPoints.map { "${it.lat},${it.lng}" }.sorted().joinToString("|")
    }

    BoxWithConstraints(modifier = modifier) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val insetTopPx = with(density) { kHeaderOcclusion.toPx() }
        val touchPx = with(density) { 48.dp.toPx() }
        val userDotPx = with(density) { 22.dp.toPx() }

        var camera by remember { mutableStateOf<MapCamera?>(null) }

        // Declared first so the refit below wins when both fire on first layout.
        // A pure resize keeps the centre and zoom; only new geography refits.
        LaunchedEffect(widthPx, heightPx, insetTopPx) {
            val existing = camera
            camera = if (existing != null) {
                existing.copy(widthPx = widthPx, heightPx = heightPx, insetTopPx = insetTopPx)
            } else {
                MapCamera.fitting(fitPoints, widthPx, heightPx, insetTopPx = insetTopPx)
            }
        }
        LaunchedEffect(fitKey, widthPx, heightPx, insetTopPx) {
            if (fitKey.isNotEmpty()) {
                camera = MapCamera.fitting(fitPoints, widthPx, heightPx, insetTopPx = insetTopPx)
            }
        }

        val current = camera

        if (current == null) {
            // No camera yet (first layout, or genuinely nothing to frame). The
            // user location alone is enough to frame, so this only blanks when
            // there is no data *and* no location to fall back on.
            if (fitPoints.isEmpty()) {
                Box(Modifier.fillMaxSize().background(kSurface))
            }
        } else {
            // Gestures live on the tile layer, not the parent. On the parent they
            // would compete with the markers' own click handling and swallow taps.
            TileLayer(
                camera = current,
                modifier = Modifier
                    .fillMaxSize()
                    // Deliberately keyed on Unit. Keying on the zoom would restart
                    // this block the instant a pinch changed it, cancelling the
                    // gesture that caused the change. The block reads the `camera`
                    // state at gesture time, so it never needs re-keying.
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoomChange, _ ->
                            val from = camera ?: return@detectTransformGestures
                            var next = from

                            // Zoom is snapped to whole levels: a fractional zoom
                            // would rescale a 256px tile by 2^f and go soft.
                            val targetZoom = (from.zoom + log2(zoomChange).roundToInt())
                                .coerceIn(TileMath.MIN_ZOOM, TileMath.MAX_ZOOM)
                            if (targetZoom != from.zoom) {
                                // Keep whatever sits under the fingers pinned there.
                                val anchor = from.unproject(centroid)
                                val zoomed = from.atZoom(targetZoom)
                                val drifted = zoomed.project(anchor)
                                next = zoomed.panned(
                                    Offset(centroid.x - drifted.x, centroid.y - drifted.y),
                                )
                            }
                            camera = next.panned(pan)
                        }
                    },
            )

            providers.forEach { provider ->
                val anchor = current.project(GeoPoint(provider.lat, provider.lng))
                ProviderMarker(
                    provider = provider,
                    selected = selectedProvider?.id == provider.id,
                    onClick = { onProviderMarkerClick(provider) },
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (anchor.x - touchPx / 2f).roundToInt(),
                            y = (anchor.y - touchPx / 2f).roundToInt(),
                        )
                    },
                )
            }

            if (userLocation != null) {
                val anchor = current.project(userLocation)
                UserLocationDot(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (anchor.x - userDotPx / 2f).roundToInt(),
                            y = (anchor.y - userDotPx / 2f).roundToInt(),
                        )
                    },
                )
            }

            MapControls(
                onZoomIn = { camera = current.atZoom(current.zoom + 1) },
                onZoomOut = { camera = current.atZoom(current.zoom - 1) },
                onRefit = {
                    camera = MapCamera.fitting(fitPoints, widthPx, heightPx, insetTopPx = insetTopPx)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 84.dp),
            )

            TileAttribution(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 12.dp),
            )
        }
    }
}

/** Zoom in / out / refit. Buttons because pinch is not available everywhere. */
@Composable
private fun MapControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onRefit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(shape = RoundedCornerShape(12.dp), color = kSurface, shadowElevation = 3.dp) {
            Column {
                ControlButton(icon = { Icon(IconPlus, null, tint = Color.Black, modifier = Modifier.size(16.dp)) }, onClick = onZoomIn, label = "Zoom in")
                Box(Modifier.size(width = 40.dp, height = 1.dp).background(kDivider))
                ControlButton(icon = { Icon(IconMinus, null, tint = Color.Black, modifier = Modifier.size(16.dp)) }, onClick = onZoomOut, label = "Zoom out")
            }
        }
        ControlButton(
            icon = { Icon(IconFitToScreen, null, tint = Color.Black, modifier = Modifier.size(16.dp)) },
            onClick = onRefit,
            label = "Fit all providers",
            surface = true,
        )
    }
}

@Composable
private fun ControlButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    label: String,
    surface: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .then(if (surface) Modifier.background(kSurface) else Modifier)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}

/**
 * Required by the ODbL when OpenStreetMap data is shown. Not optional: OSM's tile
 * terms require visible attribution, so this stays on screen whenever tiles are.
 */
@Composable
private fun TileAttribution(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = kSurface.copy(alpha = 0.85f),
    ) {
        Text(
            text = TileMath.OSM_ATTRIBUTION,
            fontSize = 9.sp,
            color = kAccentTeal,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        )
    }
}

/**
 * One provider pin: service glyph leading, rating as a trailing badge.
 *
 * Rendered as a Composable rather than drawn into a Canvas so it is hit-testable
 * and exposes an accessibility label.
 */
@Composable
private fun ProviderMarker(
    provider: ServiceProvider,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val category = serviceCategoryOf(provider.category)
    val colors = category.colors
    val status = provider.status.colors
    val label = "${provider.name}, ${category.label}, rated ${provider.formatRating()}"

    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        // Leading mark: the service glyph, tinted by the category accent.
        Surface(
            shape = CircleShape,
            color = if (selected) colors.accent else colors.container,
            border = BorderStroke(
                width = if (selected) 2.dp else 1.5.dp,
                color = if (selected) colors.accent else colors.accent.copy(alpha = 0.45f),
            ),
            shadowElevation = if (selected) 6.dp else 2.dp,
        ) {
            Box(
                modifier = Modifier.size(if (selected) 38.dp else 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else colors.accent,
                    modifier = Modifier.size(if (selected) 20.dp else 17.dp),
                )
            }
        }

        // Secondary: rating badge, anchored bottom-end.
        Surface(
            modifier = Modifier.align(Alignment.BottomEnd),
            shape = RoundedCornerShape(9.dp),
            color = kSurface,
            border = BorderStroke(1.dp, kDivider),
            shadowElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = IconStar,
                    contentDescription = null,
                    tint = kAccentGold,
                    modifier = Modifier.size(8.dp),
                )
                Spacer(Modifier.width(1.dp))
                Text(
                    text = provider.formatRating(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = kPrimaryRed,
                )
            }
        }

        // Availability sliver so status is legible without opening the sheet.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(11.dp)
                .clip(CircleShape)
                .background(status.accent)
                .padding(2.dp)
                .clip(CircleShape)
                .background(kSurface),
        )
    }
}

/** The user's own position. */
@Composable
private fun UserLocationDot(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(kPrimaryRed.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(kPrimaryRed),
        )
    }
}
