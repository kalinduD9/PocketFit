package com.kalindu.pocketfit.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import java.util.concurrent.Executor

class LocationHelper(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    //Checks if location services (GPS or Network provider) are enabled on the device.

    fun isLocationEnabled(): Boolean {
        return try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                   locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }


     //Attempts to fetch the current location.
     //Uses modern API 30+ getCurrentLocation.

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(onLocationResult: (Location?) -> Unit) {
        if (!isLocationEnabled()) {
            onLocationResult(null)
            return
        }

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            onLocationResult(null)
            return
        }

        try {
            val cancellationSignal = CancellationSignal()
            val executor: Executor = context.mainExecutor
            locationManager.getCurrentLocation(
                provider,
                cancellationSignal,
                executor
            ) { location ->
                onLocationResult(location)
            }
        } catch (e: Exception) {
            onLocationResult(null)
        }
    }
}
