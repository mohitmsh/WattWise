package edu.monash.wattwise.data.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import edu.monash.wattwise.constants.MapSettings
import edu.monash.wattwise.data.model.MapPlace

/**
 * Reads where the device is from Android's LocationManager. The map screen
 * asks the user for the location permission before this class is used.
 */
class DeviceLocationProvider(private val context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    /**
     * Asks Android for the device's current position once, then calls
     * [onResult] on the main thread. The result is null when Location is
     * switched off or no position could be found.
     */
    // The permission is requested on the map screen before this function is called.
    @SuppressLint("MissingPermission")
    fun requestLocation(onResult: (MapPlace?) -> Unit) {
        val provider = bestProvider()
        if (provider == null) {
            Log.d(TAG, "Location is switched off on this device")
            onResult(null)
            return
        }
        LocationManagerCompat.getCurrentLocation(
            locationManager,
            provider,
            // No cancel signal is needed; the type is named because the function has two versions.
            null as CancellationSignal?,
            ContextCompat.getMainExecutor(context),
        ) { location: Location? ->
            if (location == null) {
                Log.d(TAG, "No position was returned by the $provider provider")
                onResult(null)
            } else {
                Log.d(TAG, "Device location from the $provider provider: ${location.latitude}, ${location.longitude}")
                onResult(MapPlace(MapSettings.MY_LOCATION_NAME, location.latitude, location.longitude))
            }
        }
    }

    /**
     * The provider to ask: GPS is the most accurate, and the network provider
     * (Wi-Fi and mobile towers) is the fallback. Null when both are switched off.
     */
    private fun bestProvider(): String? = when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        else -> null
    }

    private companion object {
        const val TAG = "DEVICE_LOCATION"
    }
}
