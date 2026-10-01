package com.homeapp.features.services.map

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** A WGS84 coordinate. */
data class GeoPoint(val lat: Double, val lng: Double)

/**
 * This used to also hold `GeoBounds`, an equirectangular projection used to place
 * markers on the schematic canvas. It has been removed deliberately.
 *
 * Keeping a second, simpler projection around next to a real tile map is a trap:
 * equirectangular is not Web Mercator, so anything drawn with it will not line up
 * with the tiles, and it looks like a perfectly reasonable API to reach for. The
 * map's projection now lives in [TileMath] / [MapCamera], which is the single
 * place that converts coordinates to screen pixels.
 */

/** Mean great-circle distance between two points, in kilometres. */
fun haversineKm(a: GeoPoint, b: GeoPoint): Double {
    val earthRadiusKm = 6371.0
    val dLat = (b.lat - a.lat) * PI / 180.0
    val dLng = (b.lng - a.lng) * PI / 180.0
    val lat1 = a.lat * PI / 180.0
    val lat2 = b.lat * PI / 180.0

    val h = sin(dLat / 2) * sin(dLat / 2) +
        cos(lat1) * cos(lat2) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * earthRadiusKm * asin(sqrt(h.coerceIn(0.0, 1.0)))
}
