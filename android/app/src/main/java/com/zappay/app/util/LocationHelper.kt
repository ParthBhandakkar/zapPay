package com.zappay.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
)

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun getLastKnownLocation(onResult: (LocationResult?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    onResult(LocationResult(location.latitude, location.longitude))
                } else {
                    requestCurrentLocation(onResult)
                }
            }.addOnFailureListener { onResult(null) }
        } catch (_: Exception) {
            onResult(null)
        }
    }

    private fun requestCurrentLocation(onResult: (LocationResult?) -> Unit) {
        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token,
            ).addOnSuccessListener { location ->
                if (location != null) {
                    onResult(LocationResult(location.latitude, location.longitude))
                } else {
                    onResult(null)
                }
            }.addOnFailureListener { onResult(null) }
        } catch (_: Exception) {
            onResult(null)
        }
    }
}
