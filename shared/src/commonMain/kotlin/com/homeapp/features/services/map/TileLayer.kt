package com.homeapp.features.services.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import coil3.compose.AsyncImage
import kotlin.math.roundToInt

/** Wash behind the tiles, visible while they load and if they never arrive. */
private val kTileFallbackWash = Color(0xFFE8F0E8)
private val kTileFallbackGrid = Color(0xFFD6E2D6)

/**
 * Raster tile backdrop.
 *
 * Deliberately built from one `AsyncImage` per tile rather than a single Canvas.
 * Canvas would need the decoded `ImageBitmap` for every tile pulled out of Coil by
 * hand and drawn with `drawImage`; letting `AsyncImage` own the lifecycle gets
 * memory-cache reuse, request deduplication and cancellation for free, and a
 * dozen-odd tiles is not a composition cost worth optimising.
 *
 * No explicit `ImageLoader` is passed. `coil-network-ktor3` ships a
 * `META-INF/services/coil3.util.FetcherServiceLoaderTarget` entry and
 * `ImageLoader.Builder` calls `addServiceLoaderComponents()`, so the default loader
 * already has the Ktor fetcher that the URL scheme needs. Constructing a private
 * loader here would have been redundant at best, and a second fetcher at worst.
 */
@Composable
fun TileLayer(
    camera: MapCamera,
    modifier: Modifier = Modifier,
    tileUrlTemplate: String = TileMath.OSM_TILE_URL,
    onAnyTileLoaded: () -> Unit = {},
    onAllTilesFailed: () -> Unit = {},
) {
    val density = LocalDensity.current
    val tiles = remember(camera) { camera.visibleTiles() }
    val tileDp = with(density) { TileMath.TILE_SIZE.toFloat().toDp() }

    // Tile results, keyed by zoom/x/y so a re-pan of the same tile is not a new key.
    val results = remember { mutableStateMapOf<String, Boolean>() }

    Box(modifier = modifier) {
        TileFallbackWash(Modifier.fillMaxSize())

        tiles.forEach { placed ->
            key(placed.key.cacheKey) {
                AsyncImage(
                    // A plain String model: Coil keys its caches off the URL, which
                    // is already unique per tile and needs no extra request config.
                    model = placed.key.url(tileUrlTemplate),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    // Tiles are drawn at exactly their native 256px, so any
                    // filtering would only soften them.
                    filterQuality = FilterQuality.None,
                    modifier = Modifier
                        .offset {
                            IntOffset(placed.offset.x.roundToInt(), placed.offset.y.roundToInt())
                        }
                        .size(tileDp),
                    onSuccess = {
                        // Only report the first success: a re-pan should not keep
                        // re-firing the callback on every cached tile.
                        if (results.put(placed.key.cacheKey, true) == null) onAnyTileLoaded()
                    },
                    onError = {
                        if (results.put(placed.key.cacheKey, false) == null && results.values.none { it }) {
                            onAllTilesFailed()
                        }
                    },
                )
            }
        }
    }
}

/**
 * Stand-in for missing tiles.
 *
 * Two jobs. While loading it stops the map flashing as empty white, and with no
 * network it keeps the screen showing *something* useful — the provider pins are
 * still positioned by real coordinates on top of it, so the map degrades to a
 * plain coordinate scatter instead of a blank panel.
 */
@Composable
private fun TileFallbackWash(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(kTileFallbackWash)

        // A coarse grid, enough to read pan and zoom motion by.
        val step = TileMath.TILE_SIZE / 2f
        var x = 0f
        while (x <= size.width) {
            drawLine(kTileFallbackGrid, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
            x += step
        }
        var y = 0f
        while (y <= size.height) {
            drawLine(kTileFallbackGrid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
            y += step
        }
    }
}
