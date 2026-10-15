package app.cashjournal.util

import kotlin.math.roundToInt

/**
 * Utility for normalizing cellular signal strength levels into the 4-dot visual indicator
 * displayed in the status ring cluster.
 */
object SignalStrengthHelper {

    /**
     * Translates a raw signal strength level from any phone system scale into 4 visual dots (0 to 4).
     *
     * @param rawLevel The reported signal level (e.g. 0 to 4 on standard Android, 0 to 5 on Samsung).
     * @param maxScale The maximum scale of the device (typically 4 on AOSP, 5 on Samsung One UI).
     * @return An integer between 0 and 4 representing the number of active signal dots.
     */
    fun translateToFourDots(rawLevel: Int, maxScale: Int = 4): Int {
        if (rawLevel <= 0) return 0
        if (maxScale <= 4) {
            return rawLevel.coerceIn(0, 4)
        }
        val safeScale = maxScale.coerceAtLeast(1)
        val ratio = rawLevel.coerceIn(0, safeScale).toFloat() / safeScale.toFloat()
        return (ratio * 4f).roundToInt().coerceIn(0, 4)
    }
}
