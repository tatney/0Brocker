package com.homeapp

import android.os.Bundle
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.homeapp.data.database.AndroidDriverFactory
import com.homeapp.data.database.DatabaseProvider

class MainActivity : ComponentActivity() {
    private val photoScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Main)
    private var photoCallback: ((List<com.homeapp.data.cloud.ServicePhoto>, String?) -> Unit)? = null
    private val photoPicker = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetMultipleContents()) { uris ->
        val callback = photoCallback; photoCallback = null
        photoScope.launch {
            val result = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                runCatching {
                    require(uris.size <= 5) { "Choose at most five photos" }
                    uris.map { uri ->
                        val mime=contentResolver.getType(uri) ?: ""
                        require(mime in listOf("image/jpeg","image/png","image/webp")) { "Choose JPEG, PNG or WebP photos" }
                        val bytes=contentResolver.openInputStream(uri)?.use { input ->
                            val buffer=java.io.ByteArrayOutputStream();val chunk=ByteArray(8192)
                            while(true) {val n=input.read(chunk);if(n<0)break;require(buffer.size()+n<=5*1024*1024) {"Photo exceeds 5 MB"};buffer.write(chunk,0,n)}
                            buffer.toByteArray()
                        } ?: error("Cannot read photo")
                        require(bytes.isNotEmpty()) {"Photo is empty"}
                        com.homeapp.data.cloud.ServicePhoto(uri.lastPathSegment?.takeLast(50)?:"Photo",mime,bytes)
                    }
                }
            }
            callback?.invoke(result.getOrDefault(emptyList()),result.exceptionOrNull()?.message)
        }
    }
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
        photoScope.cancel()
        super.onDestroy()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidDriverFactory.setAndroidContext(this)
        val secureSession=SecureCloudSession(this)
        com.homeapp.data.cloud.CloudRuntime.configure(BuildConfig.SUPABASE_URL,BuildConfig.SUPABASE_KEY,BuildConfig.IS_PREVIEW)
        com.homeapp.data.cloud.CloudRuntime.loadSession=secureSession::load
        com.homeapp.data.cloud.CloudRuntime.saveSession=secureSession::save
        com.homeapp.data.cloud.ServicePhotoPicker.pick = { callback -> photoCallback=callback;photoPicker.launch("image/*") }
        com.homeapp.data.cloud.ServicePhotoPicker.saveLocal = { bookingId, photo ->
            val dir=java.io.File(filesDir,"request-photos/$bookingId").apply {mkdirs()}
            val file=java.io.File(dir,java.util.UUID.randomUUID().toString()+if(photo.mime=="image/png") ".png" else if(photo.mime=="image/webp") ".webp" else ".jpg")
            file.writeBytes(photo.bytes);file.absolutePath
        }
        if(BuildConfig.IS_PREVIEW) {
            com.homeapp.features.services.map.UserLocation.update(0.3476,32.5825)
            com.homeapp.features.services.map.UserLocation.explain("Offline test location: Kampala. No live GPS travel.")
        }
        DatabaseProvider.initialize()

        com.homeapp.features.services.map.UserLocation.request = requestLocationPermission

        setContent {
            App()
        }
    }
}