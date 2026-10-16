package com.homeapp.features.services.map

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Platform locations are optional and never replaced by a fictitious user dot. */
object UserLocation {
    private val mutable = MutableStateFlow<GeoPoint?>(null)
    private val notice = MutableStateFlow("Share your location for distances and arrival estimates.")
    val point = mutable.asStateFlow()
    val status = notice.asStateFlow()
    var request: () -> Unit = { explain("Location is unavailable on this platform.") }
    fun explain(message: String) { notice.value = message }
    fun clear(message: String) { mutable.value = null; explain(message) }
    fun update(lat: Double, lng: Double) {
        if (lat in -90.0..90.0 && lng in -180.0..180.0) {
            mutable.value = GeoPoint(lat,lng)
            explain("Using device location. Provider pins are approximate; estimates exclude traffic.")
        }
    }
}
