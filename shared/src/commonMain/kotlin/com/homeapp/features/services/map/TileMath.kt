package com.homeapp.features.services.map

import androidx.compose.ui.geometry.Offset
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

/**
 * Slippy-map (Web Mercator) tile maths.
 *
 * The map used to draw a decorative grid and scatter pins using a linear
 * equirectangular fit of the provider coordinates. Positions were self-consistent
 * but the backdrop was not geography, so a provider could sit in the middle of
 * what is actually a lake. Real tiles need a spherical Mercator projection and
 * the standard XYZ tile grid, which is what this file implements.
 *
 * Deliberately pure: no Compose, no coroutines, no I/O. Everything here is
 * unit-testable, which is how the projection and zoom selection are verified.
 */
object TileMath {

    /** Edge length of one tile image in pixels at any zoom, before HiDPI scaling. */
    const val TILE_SIZE = 256

    /** Web Mercator is undefined at the poles; this is the standard cutoff. */
    const val MAX_LATITUDE = 85.05112878

    const val MIN_ZOOM = 3
    const val MAX_ZOOM = 19

    /**
     * Default raster tile endpoint.
     *
     * OpenStreetMap's public tile server. Their
     * [usage policy](https://operations.osmfoundation.org/policies/tiles/) requires
     * a descriptive User-Agent and forbids heavy use, so this is fine for
     * development and a modest audience but **must** be swapped for a self-hosted
     * or commercial tile source before real traffic. Only `{z}`, `{x}` and `{y}`
     * are substituted.
     */
    const val OSM_TILE_URL = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"

    /** Attribution required by the ODbL when OSM data is displayed. */
    const val OSM_ATTRIBUTION = "© OpenStreetMap contributors"

    fun clampLatitude(lat: Double): Double = lat.coerceIn(-MAX_LATITUDE, MAX_LATITUDE)

    /** Edge length of the whole world in pixels at [zoom]. */
    fun worldSize(zoom: Int): Double = TILE_SIZE * tileCount(zoom).toDouble()

    /** Number of tiles along one axis at [zoom] (2^z). */
    fun tileCount(zoom: Int): Int = 1 shl zoom.coerceIn(0, 30)

    /**
     * Absolute world-pixel X for [lng] at [zoom].
     *
     * Grows east from -180°. Paired with [worldY] this is the standard slippy-map
     * projection; [MapCamera] turns the result into screen coordinates.
     */
    fun worldX(lng: Double, zoom: Int): Double =
        (lng + 180.0) / 360.0 * worldSize(zoom)

    /** Absolute world-pixel Y for [lat] at [zoom]. Grows *south* from the north edge. */
    fun worldY(lat: Double, zoom: Int): Double {
        val clamped = clampLatitude(lat) * PI / 180.0
        val mercator = ln(tan(PI / 4.0 + clamped / 2.0))
        return (1.0 - mercator / PI) / 2.0 * worldSize(zoom)
    }

    /**
     * The tile containing [point] at [zoom].
     *
     * Clamped to the valid grid. The clamp is not cosmetic: at the very top of
     * the Mercator range `worldY` evaluates to about -1.6e-9 rather than 0, and
     * an unclamped `floor` of that is -1, which addresses a tile that does not
     * exist. Returning an addressable key unconditionally means callers cannot
     * accidentally request `.../0/-1/0.png`.
     */
    fun tileOf(point: GeoPoint, zoom: Int): TileKey {
        val maxIndex = tileCount(zoom) - 1
        return TileKey(
            x = floor(worldX(point.lng, zoom) / TILE_SIZE).toInt().coerceIn(0, maxIndex),
            y = floor(worldY(point.lat, zoom) / TILE_SIZE).toInt().coerceIn(0, maxIndex),
            zoom = zoom,
        )
    }

    /**
     * The latitude for an absolute world-pixel Y, the exact inverse of [worldY].
     *
     * Used to pan (screen delta back to a centre) and to verify the forward
     * transform round-trips in tests rather than being taken on trust.
     */
    fun latitudeOfWorldY(worldY: Double, zoom: Int): Double {
        val normalized = (worldY / worldSize(zoom)).coerceIn(0.0, 1.0)
        val mercator = PI - 2.0 * PI * normalized
        return 180.0 / PI * atan(0.5 * (exp(mercator) - exp(-mercator)))
    }

    /** The longitude for an absolute world-pixel X, the exact inverse of [worldX]. */
    fun longitudeOfWorldX(worldX: Double, zoom: Int): Double =
        (worldX / worldSize(zoom)) * 360.0 - 180.0
}

/** A single tile in the XYZ grid. */
data class TileKey(val x: Int, val y: Int, val zoom: Int) {

    /** Fills `{z}`, `{x}` and `{y}` in [template]. */
    fun url(template: String = TileMath.OSM_TILE_URL): String =
        template
            .replace("{z}", zoom.toString())
            .replace("{x}", x.toString())
            .replace("{y}", y.toString())

    /** Stable cache key, independent of the template, so sources can coexist. */
    val cacheKey: String get() = "$zoom/$x/$y"
}

/** A tile plus the screen position of its top-left corner. */
data class PlacedTile(val key: TileKey, val offset: Offset)

/**
 * A slippy-map camera: which part of the world is on screen.
 *
 * [zoom] is an **integer** on purpose. Fractional zoom means rescaling a tile
 * image by 2^f on every frame, which softens the raster; snapping to whole zoom
 * levels keeps downloaded 256px tiles crisp and makes the tile grid align exactly
 * with the world-pixel grid the maths assumes.
 */
data class MapCamera(
    val center: GeoPoint,
    val zoom: Int,
    val widthPx: Float,
    val heightPx: Float,
    /**
     * Pixels of the top of the viewport hidden behind the floating header.
     *
     * The header is an overlay, so the viewport really is the full height, but
     * the band a user can actually see starts [insetTopPx] down. Without this the
     * map centres providers in the geometric middle of the screen and leaves half
     * of them behind the search bar. Tile requests also start here, so the tiles
     * hidden under the header are never fetched.
     */
    val insetTopPx: Float = 0f,
) {
    init {
        require(zoom in TileMath.MIN_ZOOM..TileMath.MAX_ZOOM) {
            "zoom $zoom outside ${TileMath.MIN_ZOOM}..${TileMath.MAX_ZOOM}"
        }
    }

    /** World-pixel coordinate of the viewport's top-left corner. */
    private val originX: Double get() = TileMath.worldX(center.lng, zoom) - widthPx / 2.0
    private val originY: Double
        get() = TileMath.worldY(center.lat, zoom) - (insetTopPx + heightPx) / 2.0

    /** The vertical band of the viewport that is not occluded. */
    private val visibleTop: Double get() = insetTopPx.toDouble()
    private val visibleBottom: Double get() = heightPx.toDouble()

    /** Projects [point] into screen pixels. North is up. */
    fun project(point: GeoPoint): Offset = Offset(
        x = (TileMath.worldX(point.lng, zoom) - originX).toFloat(),
        y = (TileMath.worldY(point.lat, zoom) - originY).toFloat(),
    )

    /** The coordinate under a screen [offset], for turning a tap into a location. */
    fun unproject(offset: Offset): GeoPoint = GeoPoint(
        lat = TileMath.latitudeOfWorldY(offset.y + originY, zoom),
        lng = TileMath.longitudeOfWorldX(offset.x + originX, zoom),
    )

    /** Pans by a screen-space drag of [drag] pixels. */
    fun panned(drag: Offset): MapCamera {
        if (drag.x == 0f && drag.y == 0f) return this
        // Dragging the map right has to move the *centre* left, hence the negation.
        return copy(
            center = GeoPoint(
                lat = TileMath.latitudeOfWorldY(TileMath.worldY(center.lat, zoom) - drag.y, zoom),
                lng = TileMath.longitudeOfWorldX(TileMath.worldX(center.lng, zoom) - drag.x, zoom),
            ),
        )
    }

    /** A copy at [zoom], preserving the centre. */
    fun atZoom(zoom: Int): MapCamera =
        copy(zoom = zoom.coerceIn(TileMath.MIN_ZOOM, TileMath.MAX_ZOOM))

    /**
     * The tiles needed to cover the viewport.
     *
     * Indices are clamped to the valid world rather than wrapped, so panning past
     * the antimeridian shows blank space rather than tiles from the far side. For
     * a city-level provider map that is the right trade, and it avoids loading a
     * duplicate set of tiles at two screen positions.
     */
    fun visibleTiles(): List<PlacedTile> {
        if (widthPx <= 0f || heightPx <= 0f) return emptyList()

        val size = TileMath.TILE_SIZE
        val maxIndex = TileMath.tileCount(zoom) - 1

        val minX = floor(originX / size).toInt().coerceIn(0, maxIndex)
        val maxX = floor((originX + widthPx) / size).toInt().coerceIn(0, maxIndex)
        // World-space Y of the visible band, skipping the strip behind the header.
        val minY = floor((originY + visibleTop) / size).toInt().coerceIn(0, maxIndex)
        val maxY = floor((originY + visibleBottom) / size).toInt().coerceIn(0, maxIndex)

        val tiles = ArrayList<PlacedTile>((maxX - minX + 1) * (maxY - minY + 1))
        for (ty in minY..maxY) {
            for (tx in minX..maxX) {
                tiles += PlacedTile(
                    key = TileKey(tx, ty, zoom),
                    offset = Offset(
                        x = (tx * size - originX).toFloat(),
                        y = (ty * size - originY).toFloat(),
                    ),
                )
            }
        }
        return tiles
    }

    companion object {
        /**
         * The highest whole zoom level at which every point in [points] still fits
         * inside the viewport, leaving [paddingFraction] of it as margin.
         *
         * Fitting rather than hardcoding a city centre is what lets the map frame
         * whatever data it is handed, including a single provider.
         */
        fun fitting(
            points: List<GeoPoint>,
            widthPx: Float,
            heightPx: Float,
            paddingFraction: Float = 0.18f,
            insetTopPx: Float = 0f,
        ): MapCamera? {
            if (points.isEmpty() || widthPx <= 0f || heightPx <= 0f) return null

            val minLat = points.minOf { TileMath.clampLatitude(it.lat) }
            val maxLat = points.maxOf { TileMath.clampLatitude(it.lat) }
            val minLng = points.minOf { it.lng }
            val maxLng = points.maxOf { it.lng }

            // Fit into the band the user can see, not the whole panel.
            val visibleH = (heightPx - insetTopPx).coerceAtLeast(1f)
            val usableW = widthPx * (1f - 2f * paddingFraction)
            val usableH = visibleH * (1f - 2f * paddingFraction)
            if (usableW <= 0f || usableH <= 0f) return null

            // Mercator stretches vertically towards the poles, so the zoom that
            // fits is whichever axis turns out to be the binding constraint.
            var fitted = TileMath.MIN_ZOOM
            for (z in TileMath.MAX_ZOOM downTo TileMath.MIN_ZOOM) {
                val spanX = TileMath.worldX(maxLng, z) - TileMath.worldX(minLng, z)
                val spanY = TileMath.worldY(minLat, z) - TileMath.worldY(maxLat, z)
                if (spanX <= usableW && spanY <= usableH) {
                    fitted = z
                    break
                }
            }

            return MapCamera(
                center = GeoPoint(lat = (minLat + maxLat) / 2.0, lng = (minLng + maxLng) / 2.0),
                zoom = fitted,
                widthPx = widthPx,
                heightPx = heightPx,
                insetTopPx = insetTopPx,
            )
        }
    }
}
