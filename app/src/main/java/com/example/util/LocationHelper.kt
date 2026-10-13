package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

enum class LocationStatus {
    REAL_LOCATION,
    LAST_KNOWN_LOCATION,
    LOCATION_UNAVAILABLE
}

data class GpsLocationResult(
    val latitude: Double?,
    val longitude: Double?,
    val address: String,
    val status: LocationStatus,
    val accuracyMeters: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isReal: Boolean get() = status == LocationStatus.REAL_LOCATION
    val hasCoordinates: Boolean get() = latitude != null && longitude != null
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    suspend fun getCurrentLocation(context: Context): GpsLocationResult {
        if (!hasLocationPermission(context)) {
            return GpsLocationResult(
                latitude = null,
                longitude = null,
                address = "Location Unavailable (Permission not granted)",
                status = LocationStatus.LOCATION_UNAVAILABLE
            )
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            var isLastKnown = false
            val location: Location? = suspendCancellableCoroutine { continuation ->
                try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationTokenSource.token
                    ).addOnSuccessListener { loc ->
                        if (loc != null) {
                            if (continuation.isActive) continuation.resume(loc)
                        } else {
                            // Fallback to last known location if active fix isn't returned immediately
                            fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                                if (lastLoc != null) {
                                    isLastKnown = true
                                    if (continuation.isActive) continuation.resume(lastLoc)
                                } else {
                                    if (continuation.isActive) continuation.resume(null)
                                }
                            }.addOnFailureListener {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }
                    }.addOnFailureListener {
                        // Attempt fallback to last known
                        fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                            if (lastLoc != null) {
                                isLastKnown = true
                                if (continuation.isActive) continuation.resume(lastLoc)
                            } else {
                                if (continuation.isActive) continuation.resume(null)
                            }
                        }.addOnFailureListener {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                } catch (e: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                }

                continuation.invokeOnCancellation {
                    cancellationTokenSource.cancel()
                }
            }

            if (location != null) {
                val address = resolveAddress(context, location.latitude, location.longitude)
                GpsLocationResult(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    status = if (isLastKnown) LocationStatus.LAST_KNOWN_LOCATION else LocationStatus.REAL_LOCATION,
                    accuracyMeters = location.accuracy,
                    timestamp = location.time
                )
            } else {
                // NEVER fabricate fake coordinates!
                GpsLocationResult(
                    latitude = null,
                    longitude = null,
                    address = "Location Unavailable (GPS signal not acquired)",
                    status = LocationStatus.LOCATION_UNAVAILABLE
                )
            }
        } catch (e: Exception) {
            GpsLocationResult(
                latitude = null,
                longitude = null,
                address = "Location Unavailable (${e.message ?: "Sensor error"})",
                status = LocationStatus.LOCATION_UNAVAILABLE
            )
        }
    }

    private suspend fun resolveAddress(context: Context, lat: Double, lng: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addressList = geocoder.getFromLocation(lat, lng, 1)
                if (!addressList.isNullOrEmpty()) {
                    val addr = addressList[0]
                    val thoroughfare = addr.thoroughfare ?: addr.subLocality ?: ""
                    val locality = addr.locality ?: addr.adminArea ?: ""
                    val postal = addr.postalCode ?: ""
                    listOf(thoroughfare, locality, postal).filter { it.isNotBlank() }.joinToString(", ")
                } else {
                    "Coordinates: %.4f, %.4f".format(Locale.US, lat, lng)
                }
            } catch (e: Exception) {
                "Coordinates: %.4f, %.4f".format(Locale.US, lat, lng)
            }
        }
    }
}
