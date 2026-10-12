package com.homeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.homeapp.data.database.AndroidDriverFactory
import com.homeapp.data.database.DatabaseProvider

class MainActivity : ComponentActivity() {
    private val locationListener = android.location.LocationListener { location ->
        com.homeapp.features.services.map.UserLocation.update(location.latitude,location.longitude)
    }
    private val locationPermission = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) readLocation() else com.homeapp.features.services.map.UserLocation.clear("Approximate location permission was not granted.")
    }
    private val requestLocationPermission: () -> Unit = {
        com.homeapp.features.services.map.UserLocation.explain("Waiting for approximate location permission...")
        locationPermission.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    private fun readLocation() {
        val manager = getSystemService(LOCATION_SERVICE) as android.location.LocationManager
        if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        if (!manager.isLocationEnabled) {
            com.homeapp.features.services.map.UserLocation.clear("Turn on device location and try again.")
            return
        }
        com.homeapp.features.services.map.UserLocation.explain("Waiting for an approximate device location...")
        manager.getProviders(true).forEach { provider ->
            runCatching { manager.getLastKnownLocation(provider)?.takeIf { System.currentTimeMillis()-it.time < 300000 }?.let(locationListener::onLocationChanged) }
        }
        runCatching {
            manager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER,15000L,100f,locationListener,mainLooper)
        }.onFailure { com.homeapp.features.services.map.UserLocation.explain("No network location fix is available. Try again when device location is available.") }
    }
    override fun onDestroy() {
        (getSystemService(LOCATION_SERVICE) as android.location.LocationManager).removeUpdates(locationListener)
        if (com.homeapp.features.services.map.UserLocation.request === requestLocationPermission) com.homeapp.features.services.map.UserLocation.request = { com.homeapp.features.services.map.UserLocation.explain("Location is unavailable until the Android screen is active.") }
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidDriverFactory.setAndroidContext(this)
        DatabaseProvider.initialize()

        com.homeapp.features.services.map.UserLocation.request = requestLocationPermission

        setContent {
            App()
        }
    }
}