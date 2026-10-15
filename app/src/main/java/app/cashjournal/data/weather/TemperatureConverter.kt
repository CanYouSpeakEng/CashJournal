package app.cashjournal.data.weather

import kotlin.math.roundToInt

/**
 * Temperature conversion helper per Build Brief v2 Section 3 & Master Roadmap Item 13:
 * Formats and converts temperatures between Celsius and Fahrenheit.
 * Formula: F = (C * 9 / 5) + 32
 */
object TemperatureConverter {

    fun celsiusToFahrenheit(celsius: Double): Double {
        return (celsius * 9.0 / 5.0) + 32.0
    }

    fun fahrenheitToCelsius(fahrenheit: Double): Double {
        return (fahrenheit - 32.0) * 5.0 / 9.0
    }

    /**
     * Formats canonical Celsius temperature into the user's preferred unit ("C" or "F").
     * Examples:
     * - format(29.0, "C") -> "29°C"
     * - format(29.0, "F") -> "84°F" (29 * 1.8 + 32 = 84.2 -> 84)
     * - format(24.0, "C") -> "24°C"
     * - format(24.0, "F") -> "75°F" (24 * 1.8 + 32 = 75.2 -> 75)
     */
    fun format(celsius: Double, preferredUnit: String): String {
        val isFahrenheit = preferredUnit.equals("F", ignoreCase = true)
        val value = if (isFahrenheit) {
            celsiusToFahrenheit(celsius).roundToInt()
        } else {
            celsius.roundToInt()
        }
        val unitLabel = if (isFahrenheit) "F" else "C"
        return "$value\u00B0$unitLabel"
    }
}
