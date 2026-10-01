package com.homeapp.features.services.map

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Projection tests for the tile map.
 *
 * The point of these is that the tile grid and the marker positions must agree.
 * If [TileMath.worldX]/[worldY] and [MapCamera.project] ever disagree, pins drift
 * away from the streets they belong on, and that is invisible in a unit test
 * suite that only checks "something was drawn".
 */
class TileMathTest {

    private val kampala = GeoPoint(0.3476, 32.5825)
    private val london = GeoPoint(51.5074, -0.1278)

    private fun assertClose(expected: Double, actual: Double, tolerance: Double, message: String = "") {
        assertTrue(
            kotlin.math.abs(expected - actual) <= tolerance,
            "$message expected <$expected> but was <$actual> (tolerance $tolerance)",
        )
    }

    // ── projection ──────────────────────────────────────────────────────────

    @Test
    fun worldSizeDoublesWithZoom() {
        assertEquals(256.0, TileMath.worldSize(0))
        assertEquals(512.0, TileMath.worldSize(1))
        assertEquals(256.0 * 1024, TileMath.worldSize(10))
    }

    @Test
    fun tileCountIsTwoToTheZoom() {
        assertEquals(1, TileMath.tileCount(0))
        assertEquals(2, TileMath.tileCount(1))
        assertEquals(8, TileMath.tileCount(3))
    }

    @Test
    fun primeMeridianIsTheHorizontalCentreOfTheWorld() {
        assertEquals(TileMath.worldSize(3) / 2.0, TileMath.worldX(0.0, 3), 1e-9)
    }

    @Test
    fun equatorIsTheVerticalCentreOfTheWorld() {
        assertEquals(TileMath.worldSize(3) / 2.0, TileMath.worldY(0.0, 3), 1e-9)
    }

    @Test
    fun northernHemisphereIsAboveTheEquator() {
        // worldY grows south, so a positive latitude must be a *smaller* Y.
        assertTrue(TileMath.worldY(51.5, 10) < TileMath.worldY(0.0, 10))
    }

    @Test
    fun latitudeIsClampedToTheMercatorCutoff() {
        // The poles are not representable; without the clamp these return NaN or
        // infinities and every downstream tile index is garbage.
        assertEquals(TileMath.MAX_LATITUDE, TileMath.clampLatitude(90.0))
        assertEquals(-TileMath.MAX_LATITUDE, TileMath.clampLatitude(-90.0))
        assertTrue(TileMath.worldY(90.0, 5).isFinite())
        assertTrue(TileMath.worldY(-90.0, 5).isFinite())
    }

    @Test
    fun worldProjectionRoundTrips() {
        for (zoom in intArrayOf(3, 10, 17)) {
            for (point in listOf(kampala, london, GeoPoint(0.0, 0.0), GeoPoint(-33.9, 151.2))) {
                assertClose(
                    point.lat,
                    TileMath.latitudeOfWorldY(TileMath.worldY(point.lat, zoom), zoom),
                    1e-6,
                    "lat round trip z=$zoom $point",
                )
                assertClose(
                    point.lng,
                    TileMath.longitudeOfWorldX(TileMath.worldX(point.lng, zoom), zoom),
                    1e-6,
                    "lng round trip z=$zoom $point",
                )
            }
        }
    }

    // ── tile indices ────────────────────────────────────────────────────────

    @Test
    fun tileIndexIsZeroZeroOneAtTheWorldOrigin() {
        // North-west corner of the world is the first tile of the grid.
        val origin = TileMath.tileOf(GeoPoint(lat = TileMath.MAX_LATITUDE, lng = -180.0), 0)
        assertEquals(0, origin.x)
        assertEquals(0, origin.y)
    }

    @Test
    fun tileOfNeverAddressesOutsideTheGrid() {
        val maxIndex = TileMath.tileCount(12) - 1
        val candidates = listOf(kampala, london, GeoPoint(0.0, 0.0), GeoPoint(85.0, 179.9))
        for (zoom in intArrayOf(3, 12, 19)) {
            for (point in candidates) {
                val tile = TileMath.tileOf(point, zoom)
                val limit = TileMath.tileCount(zoom) - 1
                assertTrue(tile.x in 0..limit, "x ${tile.x} out of range at z=$zoom")
                assertTrue(tile.y in 0..limit, "y ${tile.y} out of range at z=$zoom")
            }
        }
        assertTrue(maxIndex > 0)
    }

    @Test
    fun urlSubstitutesAllThreePlaceholders() {
        val url = TileKey(1234, 5678, 14).url()
        assertEquals("https://tile.openstreetmap.org/14/1234/5678.png", url)
    }

    @Test
    fun urlHonoursACustomTemplate() {
        val url = TileKey(1, 2, 3).url("https://tiles.example.com/{z}/{x}/{y}@2x.png")
        assertEquals("https://tiles.example.com/3/1/2@2x.png", url)
    }

    @Test
    fun cacheKeyIgnoresTheTemplate() {
        assertEquals("5/1/2", TileKey(1, 2, 5).cacheKey)
    }

    // ── camera ──────────────────────────────────────────────────────────────

    @Test
    fun cameraCentreLandsInTheMiddleOfTheViewport() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 800f, heightPx = 600f)
        val centre = camera.project(kampala)
        assertClose(400.0, centre.x.toDouble(), 0.01, "centre x")
        assertClose(300.0, centre.y.toDouble(), 0.01, "centre y")
    }

    @Test
    fun cameraProjectionRoundTripsThroughUnproject() {
        val camera = MapCamera(kampala, zoom = 15, widthPx = 640f, heightPx = 480f)
        val original = GeoPoint(0.3601, 32.5901)
        val back = camera.unproject(camera.project(original))
        assertClose(original.lat, back.lat, 1e-6, "lat")
        assertClose(original.lng, back.lng, 1e-6, "lng")
    }

    @Test
    fun northIsUp() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 400f, heightPx = 400f)
        val north = camera.project(GeoPoint(kampala.lat + 0.01, kampala.lng))
        val south = camera.project(GeoPoint(kampala.lat - 0.01, kampala.lng))
        assertTrue(north.y < south.y, "a point further north must have a smaller Y")
    }

    @Test
    fun panningByTheDeltaToTheCentreBringsAPointToTheMiddle() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 500f, heightPx = 500f)
        val target = GeoPoint(0.3510, 32.5900)

        // Panning by `drag` shifts everything on screen by `+drag`, so bringing a
        // point that currently sits at `offset` to the middle means panning by
        // the remaining distance, not by `offset` itself.
        val offset = camera.project(target)
        val panned = camera.panned(
            androidx.compose.ui.geometry.Offset(
                x = camera.widthPx / 2f - offset.x,
                y = camera.heightPx / 2f - offset.y,
            ),
        )
        val recentred = panned.project(target)
        assertClose(panned.widthPx / 2.0, recentred.x.toDouble(), 0.5, "x")
        assertClose(panned.heightPx / 2.0, recentred.y.toDouble(), 0.5, "y")
    }

    @Test
    fun panDragDirectionIsInverted() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 400f, heightPx = 400f)
        // Dragging right must reveal what is to the east, so the centre moves west.
        val panned = camera.panned(androidx.compose.ui.geometry.Offset(100f, 0f))
        assertTrue(panned.center.lng < camera.center.lng, "centre should move west when dragging east")
    }

    @Test
    fun atZoomClampsIntoTheSupportedRange() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 400f, heightPx = 400f)
        assertEquals(TileMath.MAX_ZOOM, camera.atZoom(99).zoom)
        assertEquals(TileMath.MIN_ZOOM, camera.atZoom(0).zoom)
        assertEquals(17, camera.atZoom(17).zoom)
    }

    @Test
    fun visibleTilesCoverTheWholeViewport() {
        val camera = MapCamera(kampala, zoom = 16, widthPx = 1024f, heightPx = 768f)
        val tiles = camera.visibleTiles()

        // 1024/256 = 4 columns, 768/256 = 3 rows, plus the row/col the viewport
        // starts part-way into.
        assertTrue(tiles.isNotEmpty())
        assertTrue(tiles.size >= 12, "expected at least 12 tiles, got ${tiles.size}")

        val left = tiles.minOf { it.offset.x }
        val top = tiles.minOf { it.offset.y }
        val right = tiles.maxOf { it.offset.x + TileMath.TILE_SIZE }
        val bottom = tiles.maxOf { it.offset.y + TileMath.TILE_SIZE }
        assertTrue(left <= 0f, "tiles must start at or before the left edge, was $left")
        assertTrue(top <= 0f, "tiles must start at or before the top edge, was $top")
        assertTrue(right >= 1024f, "tiles must reach the right edge, was $right")
        assertTrue(bottom >= 768f, "tiles must reach the bottom edge, was $bottom")
    }

    @Test
    fun visibleTilesAreAllAtTheCameraZoom() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 900f, heightPx = 700f)
        assertTrue(camera.visibleTiles().all { it.key.zoom == 14 })
    }

    @Test
    fun visibleTilesAreUniqueAndInRange() {
        val camera = MapCamera(kampala, zoom = 12, widthPx = 800f, heightPx = 600f)
        val tiles = camera.visibleTiles()
        assertEquals(tiles.size, tiles.map { it.key }.toSet().size, "duplicate tile requests")
        val limit = TileMath.tileCount(12) - 1
        assertTrue(tiles.all { it.key.x in 0..limit && it.key.y in 0..limit })
    }

    @Test
    fun visibleTilesIsEmptyForADegenerateViewport() {
        val camera = MapCamera(kampala, zoom = 14, widthPx = 0f, heightPx = 0f)
        assertTrue(camera.visibleTiles().isEmpty())
    }

    // ── fitting ─────────────────────────────────────────────────────────────

    @Test
    fun fittingReturnsNullWithNothingToPlot() {
        assertEquals(null, MapCamera.fitting(emptyList(), 800f, 600f))
        assertEquals(null, MapCamera.fitting(listOf(kampala), 0f, 600f))
    }

    @Test
    fun fittingFramesEveryPointInsideTheViewport() {
        val points = listOf(
            GeoPoint(0.3400, 32.5700),
            GeoPoint(0.3600, 32.6000),
            GeoPoint(0.3500, 32.5850),
        )
        val width = 800f
        val height = 600f
        val camera = assertNotNull(MapCamera.fitting(points, width, height))

        for (point in points) {
            val screen = camera.project(point)
            assertTrue(screen.x in 0f..width, "x ${screen.x} outside 0..$width")
            assertTrue(screen.y in 0f..height, "y ${screen.y} outside 0..$height")
        }
    }

    @Test
    fun fittingCentresOnTheData() {
        val points = listOf(GeoPoint(0.34, 32.57), GeoPoint(0.36, 32.60))
        val camera = assertNotNull(MapCamera.fitting(points, 800f, 600f))
        assertClose(0.35, camera.center.lat, 1e-9, "centre lat")
        assertClose(32.585, camera.center.lng, 1e-9, "centre lng")
    }

    @Test
    fun fittingASinglePointStillProducesAUsableCamera() {
        // A lone provider must not divide by zero or fall off the zoom clamp.
        val camera = assertNotNull(MapCamera.fitting(listOf(kampala), 800f, 600f))
        assertTrue(camera.zoom in TileMath.MIN_ZOOM..TileMath.MAX_ZOOM)
        val screen = camera.project(kampala)
        assertClose(400.0, screen.x.toDouble(), 0.01, "single point should be centred")
    }

    @Test
    fun fittingChoosesTheLargestZoomThatStillFits() {        val points = listOf(GeoPoint(0.34, 32.57), GeoPoint(0.36, 32.60))
        val width = 800f
        val height = 600f
        val camera = assertNotNull(MapCamera.fitting(points, width, height))
        val padding = 0.18f

        // One zoom level tighter must no longer fit; otherwise `fitting` is
        // under-zooming and leaving the map needlessly zoomed out.
        val tighter = camera.atZoom(camera.zoom + 1)
        val minX = points.minOf { tighter.project(it).x }
        val maxX = points.maxOf { tighter.project(it).x }
        val minY = points.minOf { tighter.project(it).y }
        val maxY = points.maxOf { tighter.project(it).y }
        val overflowsX = (maxX - minX) > width * (1f - 2f * padding)
        val overflowsY = (maxY - minY) > height * (1f - 2f * padding)
        assertTrue(overflowsX || overflowsY, "zoom ${camera.zoom} should not have room to go tighter")
    }

    // ── header occlusion ────────────────────────────────────────────────────

    @Test
    fun insetCentresOnTheVisibleBandNotTheWholePanel() {
        val camera = assertNotNull(
            MapCamera.fitting(listOf(kampala), widthPx = 800f, heightPx = 600f, insetTopPx = 200f),
        )
        // The centre of the data should sit midway down the *visible* band,
        // (200 + 600) / 2, not the geometric middle of the panel at 300.
        val projected = camera.project(kampala)
        assertClose(400.0, projected.x.toDouble(), 0.01, "x")
        assertClose(400.0, projected.y.toDouble(), 0.01, "y")
    }

    @Test
    fun zeroInsetCentresOnTheWholePanel() {
        val camera = assertNotNull(MapCamera.fitting(listOf(kampala), 800f, 600f))
        assertClose(300.0, camera.project(kampala).y.toDouble(), 0.01, "y")
    }

    @Test
    fun insetReducesTheFittedZoom() {
        val points = listOf(GeoPoint(0.34, 32.57), GeoPoint(0.36, 32.60))
        val withoutInset = assertNotNull(MapCamera.fitting(points, 800f, 600f))
        val withInset = assertNotNull(
            MapCamera.fitting(points, 800f, 600f, insetTopPx = 300f),
        )
        // Less room to draw means a wider view, i.e. a lower zoom.
        assertTrue(
            withInset.zoom <= withoutInset.zoom,
            "inset zoom ${withInset.zoom} should not exceed ${withoutInset.zoom}",
        )
    }

    @Test
    fun insetStillFramesEveryPointInsideTheVisibleBand() {
        val points = listOf(GeoPoint(0.3400, 32.5700), GeoPoint(0.3600, 32.6000))
        val height = 600f
        val inset = 200f
        val camera = assertNotNull(
            MapCamera.fitting(points, widthPx = 800f, heightPx = height, insetTopPx = inset),
        )
        for (point in points) {
            val screen = camera.project(point)
            assertTrue(screen.y >= inset, "y ${screen.y} hidden behind the header (inset $inset)")
            assertTrue(screen.y <= height, "y ${screen.y} below the viewport ($height)")
        }
    }

    @Test
    fun insetNeverFetchesTilesEntirelyBehindTheHeader() {
        val camera = MapCamera(kampala, zoom = 15, widthPx = 800f, heightPx = 600f, insetTopPx = 196f)
        val tiles = camera.visibleTiles()
        assertTrue(tiles.isNotEmpty())
        for (tile in tiles) {
            val bottom = tile.offset.y + TileMath.TILE_SIZE
            assertTrue(
                bottom > camera.insetTopPx,
                "tile ${tile.key.cacheKey} ends at $bottom, entirely behind an inset of ${camera.insetTopPx}",
            )
        }
    }

    @Test
    fun insetStillCoversTheWholeVisibleBand() {
        val camera = MapCamera(kampala, zoom = 15, widthPx = 800f, heightPx = 600f, insetTopPx = 196f)
        val tiles = camera.visibleTiles()
        val top = tiles.minOf { it.offset.y }
        val bottom = tiles.maxOf { it.offset.y + TileMath.TILE_SIZE }
        assertTrue(top <= 196f, "coverage must start at or above the visible band, was $top")
        assertTrue(bottom >= 600f, "coverage must reach the bottom, was $bottom")
    }
}
