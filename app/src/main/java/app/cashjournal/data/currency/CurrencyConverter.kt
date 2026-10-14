package app.cashjournal.data.currency

import kotlin.math.pow
import kotlin.math.roundToLong

object CurrencyConverter {

    /**
     * Converts an amount from source currency minor units to target currency minor units
     * using the exchange rates table.
     *
     * @param amountMinor Source amount in minor units (e.g. cents, whole rupiahs).
     * @param sourceCode ISO currency code of source currency (e.g. "IDR").
     * @param targetCode ISO currency code of target currency (e.g. "USD").
     * @param baseCode Base currency of the rates table (e.g. "IDR" or "USD").
     * @param rates Map of currency code to rate relative to baseCode.
     * @param sourceDecimals Decimal places of source currency (e.g. IDR=0, USD=2).
     * @param targetDecimals Decimal places of target currency (e.g. IDR=0, USD=2).
     */
    fun convertMinorUnits(
        amountMinor: Long,
        sourceCode: String,
        targetCode: String,
        baseCode: String,
        rates: Map<String, Double>,
        sourceDecimals: Int,
        targetDecimals: Int
    ): Long {
        if (sourceCode.equals(targetCode, ignoreCase = true)) {
            return amountMinor
        }

        // Cross-rate calculation relative to baseCode
        val rateSource = if (sourceCode.equals(baseCode, ignoreCase = true)) 1.0 else (rates[sourceCode.uppercase()] ?: return amountMinor)
        val rateTarget = if (targetCode.equals(baseCode, ignoreCase = true)) 1.0 else (rates[targetCode.uppercase()] ?: return amountMinor)

        if (rateSource <= 0.0) return amountMinor

        // 1 Source Unit in Target Units = rateTarget / rateSource
        val crossRate = rateTarget / rateSource

        val sourceFactor = 10.0.pow(sourceDecimals.toDouble())
        val targetFactor = 10.0.pow(targetDecimals.toDouble())

        val sourceMajor = amountMinor.toDouble() / sourceFactor
        val targetMajor = sourceMajor * crossRate
        return (targetMajor * targetFactor).roundToLong()
    }
}
