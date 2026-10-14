package app.cashjournal.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class LocationProvider(private val context: Context) {

    /**
     * Obtains the last known coarse location from LocationManager.
     * Brief Section 3: Android's LocationManager with coarse location only.
     * Brief Section 17: Coordinates rounded to ~1 km (2 decimal places).
     */
    @SuppressLint("MissingPermission")
    suspend fun getCoarseLocation(): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withContext null

        val providers = locationManager.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            try {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            } catch (_: SecurityException) {
                // Permission not granted or restricted
            }
        }

        bestLocation?.let {
            val roundedLat = (it.latitude * 100.0).roundToInt() / 100.0
            val roundedLon = (it.longitude * 100.0).roundToInt() / 100.0
            Pair(roundedLat, roundedLon)
        }
    }
}
